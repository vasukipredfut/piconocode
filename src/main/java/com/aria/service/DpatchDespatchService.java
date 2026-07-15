package com.aria.service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.BinStockRepository;
import com.aria.dao.DpatchBillOrderItemsRepository;
import com.aria.dao.DpatchDespatchRepository;
import com.aria.dao.DpatchItemsBatchRepository;
import com.aria.dao.OrderRepository;
import com.aria.dao.TRNIndexRepo;
import com.aria.dto.BinResponseForDespatchDto;
import com.aria.dto.BinStockDto;
import com.aria.dto.DespatchPoductsResponseDto;
import com.aria.dto.DpatchDispatchDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.ProductsAndBindDto;
import com.aria.model.BinStock;
import com.aria.model.DpatchBillorderItems;
import com.aria.model.DpatchDespatch;
import com.aria.model.DpatchItemsBatch;
import com.aria.model.Order;
import com.aria.model.Status;
import com.aria.model.TRNIndex;
import com.aria.model.User;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;

@Service
public class DpatchDespatchService {

	private static final DecimalFormat decfor = new DecimalFormat("0.0000");

	@Autowired
	private DpatchDespatchRepository dpatchDespatchRepository;

	@Autowired
	private DpatchItemsBatchRepository dpatchItemsBatchRepository;

	@Autowired
	private BinStockRepository binStockRepository;

	@Autowired
	private DpatchBillOrderItemsRepository dpatchBillOrderItemsRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	TRNIndexRepo trnIndexRepo;

	@Autowired
	BinStockUtil binStockUtil;

	@Autowired
	DateUtil dateUtil;
	@Autowired
    CExpenseService cExpenseService;

	@Transactional
	public ResponseEntity<?> saveDispatch(DpatchDispatchDto dpatchDispatchDto, User loggedUser) {
		DpatchDespatch dpatchDespatch = new DpatchDespatch(dpatchDispatchDto);
		dpatchDespatch.setCreatedBy(loggedUser);
		dpatchDespatch.setFirmId(loggedUser.getOrg_id());
		dpatchDespatch.setBranchId(loggedUser.getBranch_id());
		dpatchDespatch.setCreatedBy(loggedUser);
		if (dpatchDespatch.getTrnNo() == null || dpatchDespatch.getTrnNo() == "") {
			String trnNo = getTrnNo();
			dpatchDespatch.setTrnNo(trnNo);
			System.out.println(trnNo);
		}
		Order order = new Order();
		order.setId(dpatchDispatchDto.getOrderId());
		dpatchDespatch.setDpatch_Bill_Ref_Id(order);
		dpatchDespatch.setStatus("pending");
		dpatchDespatch.setTrndate(dateUtil.getStringTODateWk(dpatchDispatchDto.getDeliveryDate()));
		DpatchDespatch result = dpatchDespatchRepository.save(dpatchDespatch);
		if (result != null) {
			List<ProductsAndBindDto> products = dpatchDispatchDto.getProducts();
			for (ProductsAndBindDto prod : products) {
				DpatchBillorderItems dpatchBillOrderItem = dpatchBillOrderItemsRepository
						.findByIdAndProductRefId(dpatchDispatchDto.getOrderId(), prod.getProductId());
				if (dpatchBillOrderItem != null) {
					for (BinStockDto binStockDto : prod.getBinStock()) {
						binStockDto.setLTrnNo(result.getTrnNo());
						Long binName = Long.valueOf(binStockDto.getBinName());
						BinStock binStock = binStockRepository.findByBatchNoAndBinNameByLockStatus(
								binStockDto.getProductId(), binStockDto.getBatchNo(), binName,
								Double.valueOf(binStockDto.getLot()));

						if (binStock != null) {
							BinStock newBinstock = new BinStock();
							newBinstock.setRefId(binStockDto.getProductId());
							newBinstock.setBatchNo(binStockDto.getBatchNo());
							newBinstock.setBatchId(binStockDto.getBatchId());
							newBinstock.setBinId(binStockDto.getBinId());
							newBinstock.setBinName(binStockDto.getBinName());
							newBinstock.setLot(binStockDto.getLot());
							newBinstock.setStkQty(binStockDto.getTakenIssued());
							newBinstock.setSStkQty(binStockDto.getSStkQty());
							newBinstock.setAvgCost(binStockDto.getAvgCost());
							newBinstock.setDept(binStockDto.getDept());
							newBinstock.setBranch(binStockDto.getBranch());
							newBinstock.setLockStatus("Y");
							newBinstock.setLockType("BU");
							newBinstock.setGid(binStockDto.getGid());
							newBinstock.setDelStatus(binStockDto.getDelStatus());
							newBinstock.setBinType(binStockDto.getBinType());
							newBinstock.setCid(binStockDto.getCid());
							newBinstock.setOrderId(binStockDto.getOrderId());
							newBinstock.setPurchaseBillId(binStockDto.getPurchaseBillId());
							newBinstock.setMaterialTransactionId(binStockDto.getMaterialTransactionId());
							newBinstock.setInwarDate(binStockDto.getInwardDate());
							newBinstock.setLocation(binStockDto.getLocation());
							newBinstock.setCampName(binStockDto.getCampName());
							newBinstock.setSeedcolor(binStockDto.getSeedcolor());
							newBinstock.setSeedMoisture(binStockDto.getSeedMoisture());
							if (binStock.getAvgCost() != null) {
								newBinstock.setCost(newBinstock.getStkQty() * binStock.getAvgCost());
							}
							newBinstock.setLTrnNo(binStockDto.getLTrnNo());
							newBinstock.setMrnStatus("Y");
							newBinstock.setAdate(dateUtil.getISTDate());
							BinStock rsBin = binStockRepository.save(newBinstock);

							// Reduce selected Stock
							Double updatedStkQty = binStock.getStkQty() - Double.valueOf(binStockDto.getTakenIssued());
							binStock.setStkQty(Double.valueOf(decfor.format(updatedStkQty)));
							if (binStock.getAvgCost() != null) {
							binStock.setCost(updatedStkQty * binStock.getAvgCost());
							}
							binStockRepository.save(binStock);
						}

					}
				}
			}
			Optional<Order> orders = orderRepository.findById(dpatchDispatchDto.getOrderId());
			orders.get().setActive(1);
			orders.get().setBillNo(dpatchDispatchDto.getBillNo());
			orderRepository.save(orders.get());
		}
		return new ResponseEntity<Status>(new Status("200", "Order Disptach Successfully!!!"), HttpStatus.OK);

	}

	private String getTrnNo() {
		String trnNo = null;
		Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("Desp");
		if (trnIndex.isPresent()) {
			TRNIndex trn = trnIndex.get();
			Long trnlast = trn.getLastno();
			trnNo = trn.getPrefix() + "-" + trn.getLastno();
			Long fstrnlast = trnlast + 1;
			trn.setLastno(fstrnlast);
			TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
			return trnNo;
		} else {
			return trnNo;
		}
	}

	public ResponseEntity<?> deleteDespatch(Long dispatchId) {
		Optional<DpatchDespatch> dpatchDespatch = dpatchDespatchRepository.findById(dispatchId);
		if (dpatchDespatch.isPresent()) {
			dpatchDespatch.get().setActive_Flag(0);
			dpatchDespatchRepository.deleteById(dispatchId);
			return new ResponseEntity<Object>(new Status("200", "Desptach Deleted Successfully.."), HttpStatus.OK);
		} else
			return new ResponseEntity<Object>(new Status("404", "Desptach Order Not Found.."), HttpStatus.OK);
	}

	public ResponseEntity<?> changeDespatchOrderStatus(Long dispatchId) {
		Optional<DpatchDespatch> order = dpatchDespatchRepository.findById(dispatchId);
		if (order.isPresent()) {
			order.get().setStatus("approved");
			dpatchDespatchRepository.save(order.get());
			return new ResponseEntity<Object>(new Status("200", "Desptach Order Approved..."), HttpStatus.OK);
		} else
			return new ResponseEntity<Object>(new Status("200", "Desptach OrderId is not exist..."), HttpStatus.OK);
	}

	public ResponseEntity<?> dispatchApprove(Long dispatchId, User loggedUser) {
		Optional<DpatchDespatch> dispatch = dpatchDespatchRepository.findById(dispatchId);
		if (dispatch.isPresent()) {
			if(!dispatch.get().getStatus().equalsIgnoreCase("pending")) {
				return new ResponseEntity<Object>(new Status("400", "Desptach Order already Approved..."), HttpStatus.OK);
			}
			dispatch.get().setApprovedBy(loggedUser);
			dispatch.get().setStatus("approved");
			dpatchDespatchRepository.save(dispatch.get());
			List<DpatchBillorderItems> dpatchBillOrderItem = dispatch.get().getDpatch_Bill_Ref_Id().getDispatchItem();
			for (DpatchBillorderItems rs : dpatchBillOrderItem) {
				List<BinStock> binStock = binStockRepository.findByLTrnNo(dispatch.get().getTrnNo(), rs.getProduct().getId());
				for (BinStock bin : binStock) {
					// Updating in stbin
					binStockUtil.updateBinLogDispatch(bin, loggedUser, "I");
					//Updating in cexpenses
					  cExpenseService.updateCexpenseDespatchTransaction(bin, loggedUser,dispatch.get().getDpatch_Bill_Ref_Id().getId());
					// deleting allocated binstock
//					binStockRepository.deleteById(bin.getId());
					  bin.setStkQty(0.0); 
                      binStockRepository.save(bin);
				}
			}
			return new ResponseEntity<Object>(new Status("200", "Desptach Order Approved..."), HttpStatus.OK);
		} else
			return new ResponseEntity<Object>(new Status("404", "Desptach OrderId is not exist..."), HttpStatus.OK);
	}

	public ResponseEntity<?> dispatchReject(Long dispatchId) {
		Optional<DpatchDespatch> dispatch = dpatchDespatchRepository.findById(dispatchId);
		if (dispatch.isPresent()) {
			// changing order status
			Optional<Order> orders = orderRepository.findById(dispatch.get().getDpatch_Bill_Ref_Id().getId());
			orders.get().setActive(0);
			orders.get().setBillNo(null);
			orders.get().setOrderStatus("pending");
			orderRepository.save(orders.get());

			List<DpatchBillorderItems> dpatchBillOrderItem = orders.get().getDispatchItem();
			for (DpatchBillorderItems rs : dpatchBillOrderItem) {
				List<BinStock> binStock = binStockRepository.findByLTrnNo(dispatch.get().getTrnNo(),
						rs.getProduct().getId());
				for (BinStock bin : binStock) {
					BinStock bins = binStockRepository.findByBatchNoAndBinNameByLockStatus(bin.getRefId(),
							bin.getBatchNo(), bin.getBinName(), bin.getLot());
					// returning stock value to old binStock
					Double updatedStkQty = bin.getStkQty() + Double.valueOf(bins.getStkQty());
					bins.setStkQty(Double.valueOf(decfor.format(updatedStkQty)));
					if (bins.getAvgCost() != null) {
					bins.setCost(updatedStkQty * bins.getAvgCost());
					}
					binStockRepository.save(bins);

					// deleting allocated binstock
					binStockRepository.deleteById(bin.getId());
				}
			}
			// deleting created dispatch
			dpatchDespatchRepository.deleteById(dispatchId);

			return new ResponseEntity<Object>(new Status("200", "Desptach Order Rejected..."), HttpStatus.OK);
		} else
			return new ResponseEntity<Object>(new Status("404", "Desptach OrderId is not exist..."), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllByStatus( Long firmId, Long branchId,  Long orderNumber , String billNo , String disPatchDate , String vendorName , String status, Long type, Pageable pageable) {

		Date disPatchDateCon = null;
		if(disPatchDate != null ){
			disPatchDateCon = dateUtil.getStringTODateWk(disPatchDate);
		}
		if(status!=null) {
		if(status.equalsIgnoreCase("Despatched") )
		{
			status="Approved";
		}}
		Page<DpatchDespatch> page = dpatchDespatchRepository.findAllByStatus( firmId, branchId, orderNumber, billNo, disPatchDateCon , vendorName , status, type, pageable);
		if (page.isEmpty()) {
			return new ResponseEntity<>(page.toList(), HttpStatus.OK);
		}

		List<DpatchDispatchDto> allApprove = page.getContent().stream().map(despatch -> {
			DpatchDispatchDto dto = new DpatchDispatchDto(despatch);
			dto.setDeliveryDate(dateUtil.getDateTOStringWk(despatch.getTrndate()));
			dto.setId(despatch.getId());
			Order billRef = despatch.getDpatch_Bill_Ref_Id();
			if (billRef.getBillNo() != null && !billRef.getBillNo().isEmpty()) {
				dto.setBillNo(billRef.getBillNo());
			}
			if (billRef.getOrderNumber() != null) {
				dto.setOrderNumber(billRef.getOrderNumber());
			}
			if(despatch.getStatus().equalsIgnoreCase("Approved"))
			{
				dto.setStatus("Despatched");
			}

			dto.setOrderId(billRef.getId());
			if (billRef.getVendorId() != null) {
				dto.setVendorCity(billRef.getVendorId().getCity());
				dto.setVendorName(billRef.getVendorId().getName());
			}
			return dto;
		}).collect(Collectors.toList());

		PageDataDto<DpatchDispatchDto> pageData = new PageDataDto<>(allApprove, page);
		return new ResponseEntity<>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> productsBasedOnDispatchId(Long orderId) {
		List<DpatchItemsBatch> dpatchItems = dpatchItemsBatchRepository.findByDpatch_desptach_ref_id(orderId);

		Set<Long> productIds = new HashSet<Long>();
		DespatchPoductsResponseDto des = new DespatchPoductsResponseDto();
		ArrayList<DespatchPoductsResponseDto> response = new ArrayList<DespatchPoductsResponseDto>();

		for (int i = 0; i < dpatchItems.size(); i++) {
			des = new DespatchPoductsResponseDto();
			des.setId(dpatchItems.get(i).getDpatch_Items_Ref_Id().getId());
			des.setProducName(dpatchItems.get(i).getDpatch_Items_Ref_Id().getProduct().getProductName());
			des.setTotal(null);
			des.setLot(null);
			des.setQuantity(dpatchItems.get(i).getQuanity());

			if (!productIds.contains(dpatchItems.get(i).getDpatch_Items_Ref_Id().getId())) {
				ArrayList<BinResponseForDespatchDto> b = new ArrayList<BinResponseForDespatchDto>();
				for (int j = 0; j < dpatchItems.size(); j++) {
					if (dpatchItems.get(i).getDpatch_Items_Ref_Id().getId() == dpatchItems.get(j)
							.getDpatch_Items_Ref_Id().getId()) {
						BinResponseForDespatchDto bin = new BinResponseForDespatchDto();
						bin.setBinName(dpatchItems.get(j).getBinstock_Ref_Id().getBinName() + "");
						bin.setBatchNumber(dpatchItems.get(j).getBinstock_Ref_Id().getBatchNo());
						bin.setQuantity(dpatchItems.get(j).getQuanity());
						b.add(bin);
					}
				}
				productIds.add(dpatchItems.get(i).getDpatch_Items_Ref_Id().getId());
				des.setBin(b);
				response.add(des);
			}
		}
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

}
