package com.aria.service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dto.AdvancePaymentForPurchaseOrderDto;
import com.aria.dto.PageDataDto;
import com.aria.utils.DateUtil;

@Service
public class AdvancePaymentForPurchaseOrderService {

	private static final DecimalFormat decfor = new DecimalFormat("0.00");

	@Autowired
	private DateUtil dateUtil;

	@Autowired
	private AdvancePaymentForPurchaseOrderRepo advancePaymentForPurchaseOrderRepo;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private PurchaseOrderRepository purchaseOrderRepository;

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private PurchasePaymentRepository purchasePaymentRepository;

	@Autowired
	private ViewAccountsLedgerRepository viewAccountsLedgerRepository;

	@Autowired
	private BankInfoRepository bankInfoRepository;

	@Autowired
	private TRNIndexRepo trnIndexRepo;

	/* Save Advance Payment */
	public ResponseEntity<?> savePayment(AdvancePaymentForPurchaseOrderDto dto, User user) {
		AdvancePaymentForPurchaseOrder model = new AdvancePaymentForPurchaseOrder(dto);
		model.setCreateById(user);
		model.setStatus("Pending");
		model.setActiveFlag(1);
		advancePaymentForPurchaseOrderRepo.save(model);
		return new ResponseEntity<Object>(new Status("200", "Advance Payment saved successfully..."), HttpStatus.OK);

	}

	/* Update Advance Payment */
	public ResponseEntity<?> updatePayment(AdvancePaymentForPurchaseOrderDto dto, User user) {

		Optional<AdvancePaymentForPurchaseOrder> model = advancePaymentForPurchaseOrderRepo.findById(dto.getPurchase_advance_payment_id());
		if (model.isPresent()) {
			if(model.get().getStatus().equalsIgnoreCase("Pending")) {
			AdvancePaymentForPurchaseOrder payment = new AdvancePaymentForPurchaseOrder(dto);

			if (model.get().getRejectedBy() != null) {
				payment.setRejectedBy(null);
				payment.setRejected_date(null);
			}
			advancePaymentForPurchaseOrderRepo.updatePendingAmount(dto.getPendingAmount(), dto.getPurchaseOrderRefId());
			payment.setStatus("Activated");
			payment.setActivateBy(user);
			payment.setActivate_date(dateUtil.getISTDate());

			if(payment.getTrnno() == null || payment.getTrnno().isEmpty()) {
				payment.setTrnno(getTrnNo());
			}

			advancePaymentForPurchaseOrderRepo.save(payment);
			updatePurchasePayment(payment);
			return new ResponseEntity<Object>(new Status("200", "Advance Payment Updated successfully..."), HttpStatus.OK);
		}else {
			return new ResponseEntity<>(new Status("400", "Advance Payment is already updated or invalid action type!"), HttpStatus.BAD_REQUEST);
		}
	}
		return new ResponseEntity<Object>(new Status("200", "Advance Payment Id Doesn't Exist"), HttpStatus.OK);
	}

	/* get All Payment BasedOn Filter */
	public ResponseEntity<?> getAllFilterPayments(Pageable pageable, String poNumber, Long vendorId, String status, String trnNo ,
			Long firmId, Long branch, Long dept, String fromDate, String toDate, User user) {

		Date fromDt = null;
		if (fromDate != null) {
			fromDt = dateUtil.getFilterDate(fromDate);
		}
		Date toDt = null;
		if (toDate != null) {
			toDt = dateUtil.getFilterDate(toDate);
		}
		Page<AdvancePaymentForPurchaseOrder> page = advancePaymentForPurchaseOrderRepo.filterByPOAdvancePayment(poNumber, vendorId, status, trnNo , fromDt, toDt, firmId, dept, branch, pageable);
		if (page.hasContent()) {
			List<AdvancePaymentForPurchaseOrderDto> dtos = page.toList().stream().map(data -> {
				AdvancePaymentForPurchaseOrderDto payment = new AdvancePaymentForPurchaseOrderDto(data);
				Optional<Vendor> vendor = vendorRepository.findById(data.getVendorRefId());
				payment.setVendorName(vendor.get().getName());
				payment.setVendorCity(vendor.get().getCity());
				Optional<PurchaseOrder> po = purchaseOrderRepository.findById(data.getPurchaseOrderRefId());
				payment.setPoNumber(po.get().getPoNumber());
				payment.setPoDate(dateUtil.getDateToStringDDMMYYYY(po.get().getPoDate()));
				payment.setPaidAmount(advancePaymentForPurchaseOrderRepo.sumOfAdvanceAmount(data.getPurchaseOrderRefId()));
				if(payment.getIfsc() == null) {
					Optional<BankInfo> bankInfo = bankInfoRepository.findByVid(payment.getVendorRefId());
					if(bankInfo.isPresent()){
						payment.setAcountNumber(bankInfo.get().getAccno());
						payment.setIfsc(bankInfo.get().getIfsc());
						payment.setBankName(bankInfo.get().getBank());
						payment.setBankBranchName(bankInfo.get().getBranch());
					}
				}else {
					Optional<BankInfo> bankInfo = bankInfoRepository.findByVid(payment.getVendorRefId(), payment.getIfsc().trim(), payment.getAcountNumber());
					if(bankInfo.isPresent()){
						payment.setBankName(bankInfo.get().getBank());
						payment.setBankBranchName(bankInfo.get().getBranch());
					}
				}
				return payment;
			}).collect(Collectors.toList());
			PageDataDto<AdvancePaymentForPurchaseOrderDto> pageData = new PageDataDto<>(dtos, page);
			return new ResponseEntity<PageDataDto<AdvancePaymentForPurchaseOrderDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(page.toList(), HttpStatus.OK);
		}
	}

	/* get Payment By Id */
	public ResponseEntity<?> getPaymentById(Long id) {
		Optional<AdvancePaymentForPurchaseOrder> optional = advancePaymentForPurchaseOrderRepo.findById(id);
		if (optional.isPresent()) {
			AdvancePaymentForPurchaseOrderDto dto = new AdvancePaymentForPurchaseOrderDto(optional.get());
			Optional<Vendor> vendor = vendorRepository.findById(dto.getVendorRefId());
			dto.setVendorName(vendor.get().getName());
			dto.setVendorCity(vendor.get().getCity());
			Optional<PurchaseOrder> po = purchaseOrderRepository.findById(dto.getPurchaseOrderRefId());
			dto.setPoNumber(po.get().getPoNumber());
			dto.setPoDate(dateUtil.getDateToStringDDMMYYYY(po.get().getPoDate()));
			dto.setPaidAmount(advancePaymentForPurchaseOrderRepo.sumOfAdvanceAmount(dto.getPurchaseOrderRefId()));
			if(dto.getIfsc() == null) {
				Optional<BankInfo> bankInfo = bankInfoRepository.findByVid(dto.getVendorRefId());
				if(bankInfo.isPresent()){
					dto.setAcountNumber(bankInfo.get().getAccno());
					dto.setIfsc(bankInfo.get().getIfsc());
					dto.setBankName(bankInfo.get().getBank());
					dto.setBankBranchName(bankInfo.get().getBranch());
				}
			}else {
				Optional<BankInfo> bankInfo = bankInfoRepository.findByVid(dto.getVendorRefId(), dto.getIfsc().trim(), dto.getAcountNumber());
				if(bankInfo.isPresent()){
					dto.setBankName(bankInfo.get().getBank());
					dto.setBankBranchName(bankInfo.get().getBranch());
				}
			}
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Advance payment not found with id: " + id);
		}
	}

	/* Change Advance Payment status */
	public ResponseEntity<?> advancePaymentReject(Long id, User user) {
		Optional<AdvancePaymentForPurchaseOrder> optional = advancePaymentForPurchaseOrderRepo.findById(id);
		if (optional.isPresent()) {
			optional.get().setStatus("Pending");
			Double pendingAmount = optional.get().getPendingAmount() + optional.get().getAdvanceAmount();
			optional.get().setPendingAmount(pendingAmount);
			optional.get().setPaymentMode(null);
			optional.get().setPurpose(null);
			optional.get().setAdvancePaymentDate(null);
			optional.get().setRejectedBy(user);
			optional.get().setRejected_date(dateUtil.getISTDate());
			if (optional.get().getCreateById() != null) {
				optional.get().setCreateById(null);
				optional.get().setCreated_date(null);
			}
			if (optional.get().getApprovedBy() != null) {
				optional.get().setApprovedBy(null);
				optional.get().setApproved_date(null);
			}
			if (optional.get().getVerifiedBy() != null) {
				optional.get().setVerifiedBy(null);
				optional.get().setVerified_date(null);
			}
			advancePaymentForPurchaseOrderRepo.save(optional.get());
			updateRejectPurchasePayment(optional.get());
			return new ResponseEntity<Object>(new Status("200", "Advance Payment Rejected Sucessfully..."), HttpStatus.OK);
		} else
			return ResponseEntity.badRequest().body("Advance Payment Id doesn't not exist...");
	}

	public ResponseEntity<?> changeadvancePaymentStatus(Long id, Optional<User> user, String status) {

		Optional<AdvancePaymentForPurchaseOrder> optional = advancePaymentForPurchaseOrderRepo.findById(id);

		if (optional.isPresent()) {

			AdvancePaymentForPurchaseOrder advancePaymentForPurchaseOrder = optional.get();

			if (status.equalsIgnoreCase("approved")) {
				if(advancePaymentForPurchaseOrder.getStatus().equalsIgnoreCase("Verified")) {
				advancePaymentForPurchaseOrder.setStatus("Approved");
				advancePaymentForPurchaseOrder.setApprovedBy(user.get());
				advancePaymentForPurchaseOrder.setApproved_date(dateUtil.getISTDate());
			}else{
				return new ResponseEntity<>(new Status("400", "Advance Payment is already " + status + " or invalid action type!"), HttpStatus.BAD_REQUEST);
			}				
			}

			if (status.equalsIgnoreCase("verified")) {
				if(advancePaymentForPurchaseOrder.getStatus().equalsIgnoreCase("Activated")) {
				advancePaymentForPurchaseOrder.setStatus("Verified");
				advancePaymentForPurchaseOrder.setVerifiedBy(user.get());
				advancePaymentForPurchaseOrder.setVerified_date(dateUtil.getISTDate());
				}else{
					return new ResponseEntity<>(new Status("400", "Advance Payment is already " + status + " or invalid action type!"), HttpStatus.BAD_REQUEST);
				}
			}

			if (status.equalsIgnoreCase("authorized")) {
				if(advancePaymentForPurchaseOrder.getStatus().equalsIgnoreCase("Approved")) {
				advancePaymentForPurchaseOrder.setStatus("Authorized");
				advancePaymentForPurchaseOrder.setAuthorizedBy(user.get());
				advancePaymentForPurchaseOrder.setAuthorized_date(dateUtil.getISTDate());
				updatePurchasePaymentByDetails(advancePaymentForPurchaseOrder, user.get());
				updateAccountLedger(advancePaymentForPurchaseOrder, user.get());
			}else{
				return new ResponseEntity<>(new Status("400", "Advance Payment is already " + status + " or invalid action type!"), HttpStatus.BAD_REQUEST);
			}
			}
			advancePaymentForPurchaseOrderRepo.save(advancePaymentForPurchaseOrder);
			return new ResponseEntity<Object>(new Status("200", "Advance Payment Status Updated Sucessfully ..."), HttpStatus.OK);
		} else
			return ResponseEntity.badRequest().body("Advance Payment Id does not exist...");
	}

	public void updatePurchasePaymentByDetails(AdvancePaymentForPurchaseOrder payment, User user) {

		Optional<PurchasePayment> optional = purchasePaymentRepository.findById(payment.getPurchasePaymentId());
		if (optional.isPresent()) {
			PurchasePayment purchasePayment = optional.get();
			purchasePayment.setPayment_by(user.getId());
			purchasePayment.setPayment_date(payment.getAdvancePaymentDate());
			purchasePayment.setPayment_status(1);
			purchasePayment.setStatus(payment.getStatus());
			purchasePayment.setPercentage(payment.getPercentage());
			purchasePayment.setAmount(payment.getAdvanceAmount());
			purchasePaymentRepository.save(purchasePayment);
		}

	}

	public void updatePurchasePayment(AdvancePaymentForPurchaseOrder payment) {

		Optional<PurchasePayment> optional = purchasePaymentRepository.findById(payment.getPurchasePaymentId());
		if (optional.isPresent()) {
			PurchasePayment purchasePayment = optional.get();
			purchasePayment.setPayment_status(1);
			purchasePayment.setStatus(payment.getStatus());
			purchasePaymentRepository.save(purchasePayment);
		}

	}

	public void updateRejectPurchasePayment(AdvancePaymentForPurchaseOrder payment) {

		Optional<PurchasePayment> optional = purchasePaymentRepository.findById(payment.getPurchasePaymentId());
		if (optional.isPresent()) {
			PurchasePayment purchasePayment = optional.get();
			purchasePayment.setPayment_status(0);
			purchasePayment.setStatus(payment.getStatus());
			purchasePaymentRepository.save(purchasePayment);
		}

	}

	public void updateAccountLedger(AdvancePaymentForPurchaseOrder payment, User user) {

		ViewAccountsLedger ledger = new ViewAccountsLedger();

		Optional<PurchaseOrder> optional = purchaseOrderRepository.findById(payment.getPurchaseOrderRefId());
		if (optional.isPresent()) {
			ledger.setPartyId(optional.get().getVendor().getId());
			ledger.setCompany(optional.get().getFirmId());
			ledger.setBranch(user.getBranch_id());
			ledger.setTrnno(payment.getTrnno());
			ledger.setDescription("Advance Payment for " + optional.get().getPoNumber());
		}
		ledger.setTrnType(2L); // 4L changed to 2L
		ledger.setTrnAmount(payment.getAdvanceAmount());
		ledger.setTrnDate(dateUtil.getISTDate());
		viewAccountsLedgerRepository.save(ledger);
	}


	private String getTrnNo() {
		String trnNo = null;
		Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("Advance");
		if (trnIndex.isPresent()) {
			TRNIndex trn = trnIndex.get();
			Long trnlast = trn.getLastno();
			trnNo = trn.getPrefix() + "-" + trn.getLastno();
			Long fstrnlast = trnlast + 1;
			trn.setLastno(fstrnlast);
			trnIndexRepo.saveAndFlush(trn);
			return trnNo;
		} else {
			return trnNo;
		}
	}


	/*
	 * AdvancePaymentForPurchaseOrder payment1 = payment; if
	 * (payment1.getPurchaseOrderRefId() != null) { Long id =
	 * Long.parseLong(payment1.getPurchaseOrderRefId().toString());
	 * Optional<PurchaseOrder> purchase = purchaseOrderRepository.findById(id);
	 * Double po_amount = purchase.get().getPoTotalAmount();
	 * System.out.println("po_AMT = " + purchase.get().getPoTotalAmount()); Double
	 * paid_advance_amount =
	 * advancePaymentForPurchaseOrderRepo.sumOfAdvanceAmount(purchase.get().getId())
	 * ; System.out.println("paid_advance_amount = " + paid_advance_amount); Double
	 * payable_amount = purchase.get().getPoTotalAmount() - paid_advance_amount;
	 * System.out.println("payable_amount = " + payable_amount);
	 * updatePurchasePaymentByDetails(purchase.get()); if (Double.compare(po_amount,
	 * paid_advance_amount) == 0) { closePaymentByDetails(purchase.get()); } else if
	 * (payable_amount == 0.0) { closePaymentByDetails(purchase.get()); } }
	 */
	/*
	 * public void closePaymentByDetails(PurchaseOrder rs) {
	 * 
	 * List<AdvancePaymentForPurchaseOrder> advancePaymentForPurchaseOrders =
	 * advancePaymentForPurchaseOrderRepo
	 * .findByPurchaseOrderAdvancePaymentPendingList(rs.getId());
	 * 
	 * for (AdvancePaymentForPurchaseOrder payment :
	 * advancePaymentForPurchaseOrders) { payment.setPercentage(0.0);
	 * payment.setAdvancePaymentDate(dateUtil.getISTDate());
	 * payment.setAdvanceAmount(0.0); payment.setStatus("Completed");
	 * advancePaymentForPurchaseOrderRepo.save(payment); Optional<PurchasePayment>
	 * optional =
	 * purchasePaymentRepository.findById(payment.getPurchasePaymentId()); if
	 * (optional.isPresent()) { PurchasePayment purchasePayment = optional.get();
	 * purchasePayment.setPayment_date(dateUtil.getISTDate());
	 * purchasePayment.setPayment_status(1); purchasePayment.setPercentage(0.0);
	 * purchasePayment.setAmount(0.0);
	 * purchasePaymentRepository.save(purchasePayment); } }
	 * 
	 * }
	 * 
	 * public void updatePurchasePaymentByDetails(PurchaseOrder rs) {
	 * 
	 * List<PurchasePayment> advancePaymentForPurchaseOrders =
	 * purchasePaymentRepository .getPurchasePaymentListByPoIDANDStatus(rs.getId());
	 * Double remaining_percentage = 0.0; Boolean first = true; for (PurchasePayment
	 * payment : advancePaymentForPurchaseOrders) {
	 * 
	 * Double percentage = purchasePaymentRepository.sumOfPercentages(rs.getId());
	 * Double finalPer = 0.0; if (first) { finalPer = 100.0 - percentage; if
	 * (finalPer < 0) { finalPer = 0.0; } first = false; } else { finalPer =
	 * remaining_percentage - percentage; if (finalPer < 0) { finalPer = 0.0; }
	 * first = false; }
	 * 
	 * if (finalPer == 0.0) { payment.setPayment_status(1);
	 * payment.setPayment_date(dateUtil.getISTDate()); payment.setPercentage(0.0); }
	 * else { payment.setPercentage(finalPer); payment.setPayment_status(0); }
	 * purchasePaymentRepository.save(payment); } }
	 */

}
