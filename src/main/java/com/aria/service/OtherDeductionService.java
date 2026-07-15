package com.aria.service;

import com.aria.dao.BinStockRepository;
import com.aria.dao.BranchRepository;
import com.aria.dao.EmastDailyRepository;
import com.aria.dao.GstTaxCategoryRepository;
import com.aria.dao.MessVoucherRepository;
import com.aria.dao.OrganisationRepository;
import com.aria.dao.OtherDeductionRepo;
import com.aria.dao.ProductMasterRepository;
import com.aria.dao.SalesvraRepo;
import com.aria.dao.SalesvraitemsRepo;
import com.aria.dao.TRNIndexRepo;
import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.BinStock;
import com.aria.model.Branch;
import com.aria.model.GstTaxCategory;
import com.aria.model.MessVoucher;
import com.aria.model.Organisation;
import com.aria.model.OtherDeduction;
import com.aria.model.ProductMaster;
import com.aria.model.Salesvra;
import com.aria.model.Salesvraitems;
import com.aria.model.Status;
import com.aria.model.TRNIndex;
import com.aria.model.User;
import com.aria.model.VoucherEntry;
import com.aria.model.VoucherItems;
import com.aria.utils.BinStockUtil;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

@Service
public class OtherDeductionService {

	@Autowired
	OtherDeductionRepo otherDeductionRepo;

	@Autowired
	BinStockRepository binStockRepo;

	@Autowired
	BinStockUtil binStockUtil;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmastDailyRepository emastDailyRepository;

	@Autowired
	MessVoucherRepository messVoucherRepository;

	@Autowired
	SalesvraRepo salesvraRepo;

	@Autowired
	OrganisationRepository orgRepo;

	@Autowired
	ProductMasterRepository productRepo;

	@Autowired
	GstTaxCategoryRepository gstRepo;

	@Autowired
	SalesvraitemsRepo salesvraItemsRepo;

	@Autowired
	BranchRepository branchRepo;

	@Autowired
	TRNIndexRepo trnIndexRepo;

	@Autowired
	DateUtil dateUtil;

	// change this method to save entry for generate bill(set default value for
	// generate bill)
	public ResponseEntity<?> saveOtherDeduction(OtherDeductionReqDTO otherDeductionReqDTO, User loggedUser) {

		if (!otherDeductionReqDTO.getEmpDetails().isEmpty()) {

			for (OtherDeductionEmpReqDto empReqDto : otherDeductionReqDTO.getEmpDetails()) {

				OtherDeduction salary = new OtherDeduction(otherDeductionReqDTO, empReqDto);
				salary.setCid(loggedUser.getOrg_id());
				salary.setCreatedby(loggedUser.getId());
				salary.setCreated_time(dateUtil.getISTDate());
				salary.setStatus("A");
				salary.setGeneratebill(false);
				otherDeductionRepo.save(salary);
			}
		}

		return new ResponseEntity<Status>(new Status("200", "Other Deduction save successfully!!!"), HttpStatus.OK);
	}

	// (26-08-2025) created by sivanantham (create new API for Bill creation)
	// suggested by alagu sir
	public ResponseEntity<?> otherDeductionListForBill(Long month, Long year, Long company, Long branch, Long dept,
			String category, String type, Pageable pageable) throws UsernameNotFoundException {

		Page<OtherDeductionList> list = null;
		if (category.equalsIgnoreCase("Staff")) {
			{
				list = otherDeductionRepo.findByStaffList(company, branch, dept, "emaster", month, type, year,
						pageable);
			}
		} else {
			list = otherDeductionRepo.findByWagesList(company, branch, dept, "emastdaily", month, type, year, pageable);
		}

		if (!list.isEmpty()) {
			List<OtherDeductionBillResDto> result = list.toList().stream().map(data -> {
				OtherDeductionBillResDto otherDeductionBillDTO = new OtherDeductionBillResDto();
				otherDeductionBillDTO.setDeductinId(data.getId());
				otherDeductionBillDTO.setEmpId(data.getEmpId());
				otherDeductionBillDTO.setEmpName(data.getEmpName());
				otherDeductionBillDTO.setAmount(data.getAmount());
				return otherDeductionBillDTO;
			}).collect(Collectors.toList());
			PageDataDto<OtherDeductionBillResDto> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<OtherDeductionBillResDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<OtherDeductionBillResDto>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	// (19-08-2025) changed by sivanantham(added two new
	// filters(employeename,deductiontype) for get other Deduction list and changed
	// one parameter (category into empType)
	// change suggested by alagu sir
	public ResponseEntity<?> otherDeductionList(Long month, Long year, Long company, Long branch, Long dept,
			String emptype, String employeename, String detype, Pageable pageable) {

		Page<OtherDeductionList> list = null;
		Long dacatid;
		if (emptype.equalsIgnoreCase("staff")) {
			dacatid = 1l;
			list = otherDeductionRepo.findByStaff(dacatid, branch, company, dept, employeename, month, year, detype,
					"emaster", pageable);
		} else if (emptype.equalsIgnoreCase("staff apprentice")) {
			dacatid = 6l;
			list = otherDeductionRepo.findByStaff(dacatid, branch, company, dept, employeename, month, year, detype,
					"emaster", pageable);
		} else if (emptype.equalsIgnoreCase("wages")) {
			list = otherDeductionRepo.findByWage(employeename, detype, branch, dept, company, month, year, "emastdaily",
					pageable);
		}
		if (!list.isEmpty()) {
			List<OtherDeductionDTO> result = list.toList().stream().map(data -> {
				OtherDeductionDTO otherDeductionDTO = new OtherDeductionDTO(data);
				return otherDeductionDTO;
			}).collect(Collectors.toList());
			PageDataDto<OtherDeductionDTO> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<OtherDeductionDTO>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<OtherDeductionDTO>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	// created by sivanantham(create new filter method for fetch employees based on
	// this filters)
	// Discovered by subash sir client need this filter method
	public ResponseEntity<?> otherDeductionListFillter(String emptype, Long branch, Long dept, String type, Long month,
			Long year, Pageable pageable) {

		Page<OtherDeductionResList> list = null;
		Long dacatid;
		if (emptype.equalsIgnoreCase("staff")) {
			dacatid = 1l;
			list = userRepository.findByDeductionFillerForStaff(branch, dept, type, month, year, dacatid, pageable);
		} else if (emptype.equalsIgnoreCase("staff apprentice")) {
			dacatid = 6l;
			list = userRepository.findByDeductionFillerForStaff(branch, dept, type, month, year, dacatid, pageable);
		} else {
			list = emastDailyRepository.findByDeductionWages(branch, dept, type, month, year, pageable);
		}

		if (!list.isEmpty()) {
			List<OtherDeductionResListDto> result = list.toList().stream().map(emp -> {
				OtherDeductionResListDto dto = new OtherDeductionResListDto();
				dto.setEmpId(emp.getEmpId());
				dto.setEmployeeName(emp.getEmployeeName());
				dto.setAmount(0.0);
				return dto;
			}).collect(Collectors.toList());
			PageDataDto<OtherDeductionResListDto> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<OtherDeductionResListDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<OtherDeductionResListDto>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	// (26-08-2025) created by sivanantham (create new API for Delete
	// OtherDeduction)
	// suggested by alagu sir
	public ResponseEntity<?> deleteOtherDeduction(List<Long> ids) {
		if (!ids.isEmpty()) {
			List<OtherDeduction> deductionsToDelete = otherDeductionRepo.findAllById(ids);

			if (!deductionsToDelete.isEmpty()) {
				otherDeductionRepo.deleteAll(deductionsToDelete);
				return new ResponseEntity<>(new Status("200", "Deleted Successfully"), HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
	}

	// (03-08-2025) created by sivanantham (create new API for save Bill)
	// verified by alagu sir
	public ResponseEntity<?> saveDeductionBills(SalesvraReqDto deductionReqDTO, User loggedUser) {

		if (!deductionReqDTO.getDeductiondetails().isEmpty()) {

			for (DeductionDetails deductiondetails : deductionReqDTO.getDeductiondetails()) {
				Salesvra bill = new Salesvra(deductionReqDTO, deductiondetails);
				Optional<OtherDeduction> deduction = otherDeductionRepo.findById(deductiondetails.getDeductionId());
				Optional<User> user = userRepository.findById(deductiondetails.getEmpId());
				int total = salesvraRepo.findTotalCount();
				double updatestqty = 0;
				Long qty = 1l;
				if (deduction.isPresent() && user.isPresent()) {
					String Billno = "Bill" + "-" + "000" + total;
					bill.setBillno(Billno);
					bill.setDept(user.get().getDepartment().getId());
					bill.setBilldate(dateUtil.getISTDate());
					String party = deductiondetails.getEmpId() + "." + deduction.get().getContable();
					bill.setParty(party);
					bill.setBillstaff(loggedUser.getId());
					bill.setCreatedBy(loggedUser.getId());
					bill.setCreatedTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
					bill.setStatus("created");
					bill.setTrnno(getTrnNo());
					bill.setTotable(deduction.get().getContable());
					if ("UNIFORM".equalsIgnoreCase(deductionReqDTO.getDeductionType())) {
						bill.setDescription("uniform_chargers");
					}
					if ("SHOE".equalsIgnoreCase(deductionReqDTO.getDeductionType())) {
						bill.setDescription("shoe_chargers");
					}
					bill.setEntry(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
					Optional<Branch> branch = branchRepo.findById(loggedUser.getBranch_id());
					bill.setPlace(branch.get().getBranchName());

					Salesvraitems deductionitems = new Salesvraitems(bill);
					if ("UNIFORM".equalsIgnoreCase(deductionReqDTO.getDeductionType())) {
						deductionitems.setRefid(5031l);
						Optional<ProductMaster> product = productRepo.findById(5031l);
						if (product.isPresent()) {
							GstTaxCategory gst = gstRepo.findByHsnCode(product.get().getHsnRefId());
							deductionitems.setCgst(gst.getCgst());
							deductionitems.setSgst(gst.getSgst());
							deductionitems.setIgst(gst.getIgst());
							BinStock bin = binStockRepo.findByBranchAndCidAndDeptAndRefid(loggedUser.getBranch_id(),
									deductionReqDTO.getCompany(),
									product.get().getId());
							if (bin == null) {
								return ResponseEntity.status(HttpStatus.NOT_FOUND)
										.body(new Status("404", "Stock not available at your branch"));
							}
							updatestqty = bin.getStkQty() - qty.floatValue();
							bin.setStkQty(updatestqty);
							if (bin.getAvgCost() != null) {
								bin.setCost(bin.getAvgCost() * updatestqty);
							}
							binStockRepo.save(bin);
							binStockUtil.updateBinLogForBillCreation(bin, loggedUser, qty.doubleValue());
						}

					}
					if ("SHOE".equalsIgnoreCase(deductionReqDTO.getDeductionType())) {
						deductionitems.setRefid(16703l);
						Optional<ProductMaster> product = productRepo.findById(16703l);
						if (product.isPresent()) {
							GstTaxCategory gst = gstRepo.findByHsnCode(product.get().getHsnRefId());
							deductionitems.setCgst(gst.getCgst());
							deductionitems.setSgst(gst.getSgst());
							deductionitems.setIgst(gst.getIgst());
							System.out.println(loggedUser.getDepartment().getId());
							BinStock bin = binStockRepo.findByBranchAndCidAndDeptAndRefid(loggedUser.getBranch_id(),
									deductionReqDTO.getCompany(),
									product.get().getId());
							if (bin == null) {
								return ResponseEntity.status(HttpStatus.NOT_FOUND)
										.body(new Status("404", "Stock not available at your branch"));
							}
							updatestqty = bin.getStkQty() - qty.floatValue();
							bin.setStkQty(updatestqty);
							if (bin.getAvgCost() != null) {
								bin.setCost(bin.getAvgCost() * updatestqty);
							}
							binStockRepo.save(bin);
							binStockUtil.updateBinLogForBillCreation(bin, loggedUser, qty.doubleValue());
						}
					}
					deductionitems.setQty(1l);
					salesvraRepo.save(bill);
					deductionitems.setBillid(bill.getId());
					salesvraItemsRepo.save(deductionitems);
					deduction.get().setGeneratebill(true);
					otherDeductionRepo.save(deduction.get());
				}
			}
		}

		return new ResponseEntity<Status>(new Status("200", "Deductionbill save successfully!!!"), HttpStatus.OK);
	}

	private String getTrnNo() {
		String trnNo = null;
		Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("SALESVRA");
		if (trnIndex.isPresent()) {
			TRNIndex trn = trnIndex.get();
			String trnprefix = trn.getPrefix().substring(0, 1);
			Long trnlast = trn.getLastno();
			trnNo = trnprefix + "-" + trn.getLastno();
			Long fstrnlast = trnlast + 1;
			trn.setLastno(fstrnlast);
			TRNIndex rs = trnIndexRepo.saveAndFlush(trn);
			return trnNo;
		} else {
			return trnNo;
		}
	}

	public ResponseEntity<?> salesvrabillListFilter(Long month, Long year, Long company, Long branch, Long dept,
			String category, String type, String billno, String employeename, Pageable pageable)
			throws UsernameNotFoundException {

		Page<SalesvraList> list = null;
		if (category.equalsIgnoreCase("Staff")) {
			list = salesvraRepo.findByStaff(employeename, billno, type, branch, dept, company, month, year, "emaster",
					pageable);
		} else {
			list = salesvraRepo.findByWages(employeename, billno, type, branch, dept, company, month, year,
					"emastdaily", pageable);
		}
		if (!list.isEmpty()) {
			List<SalesvraResList> result = list.toList().stream().map(data -> {
				SalesvraResList salesvraDto = new SalesvraResList(data);
				if (data.getdeductiontype() == null) {
					salesvraDto.setDeductiontype(null);
				} else {
					if (data.getdeductiontype().equalsIgnoreCase("uniform_chargers")
							|| data.getdeductiontype().equalsIgnoreCase("uniform")) {
						salesvraDto.setDeductiontype("uniform");
					}
					if (data.getdeductiontype().contains("shoe_chargers") || data.getdeductiontype().contains("shoe")) {
						salesvraDto.setDeductiontype("shoe");
					}
				}
				return salesvraDto;
			}).collect(Collectors.toList());
			PageDataDto<SalesvraResList> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<SalesvraResList>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<SalesvraResList>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	public ResponseEntity<?> getSalesvraBillsForPrint(List<Long> ids) {

		Map<String, Object> response = new LinkedHashMap<>();

		try {
			// Fetch projection list
			List<SalevraBillListDto> dataList = salesvraRepo.findBillPrintDetailsByIds(ids);

			if (dataList == null || dataList.isEmpty()) {
				response.put("status", false);
				response.put("message", "No bill details found for the given IDs");
				response.put("data", Collections.emptyList());
				return ResponseEntity.ok(response);
			}

			// Convert Projection → DTO (Java 8 Compatible)
			List<SalesvraBillResListDto> result = dataList.stream().map(data -> {
				SalesvraBillResListDto dto = new SalesvraBillResListDto(data);
				if (data.getCgst() == null && data.getSgst() == null && data.getIgst() == null) {
					dto.setRoundoff(null);
				}
				double cgst = (data.getRate() * data.getCgst()) / 100;
				double sgst = (data.getRate() * data.getSgst()) / 100;
				double igst = (data.getRate() * data.getIgst()) / 100;
				String input = data.getCst();
				String cst = input.replace("CST No. ", "");
				dto.setCst(cst);
				dto.setCgst(cgst);
				dto.setSgst(sgst);
				dto.setIgst(igst);
				dto.setRoundoff(data.getRate() + cgst + sgst + igst);
				dto.setCgstValue(data.getCgst());
				dto.setSgstValue(data.getSgst());
				dto.setIgstValue(data.getIgst());
				return dto;
			}).collect(Collectors.toList()); // <-- Java 8

			response.put("status", true);
			response.put("message", "Bill details fetched successfully");
			response.put("count", result.size());
			response.put("data", result);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("status", false);
			response.put("message", "Something went wrong while fetching bill details");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	public ResponseEntity<?> getBillForVouchers(Long month, Long year, Long branch, Long company) {

		List<SalesvraBillList> list = salesvraRepo.findBill(month, year, branch, company);

		if (list.isEmpty()) {
			Map<String, Object> resp = new LinkedHashMap<>();
			resp.put("meals", Collections.emptyMap());
			resp.put("uniform", Collections.emptyMap());
			resp.put("shoe", Collections.emptyMap());
			resp.put("message", "There is no bills created to generate a voucher.");
			return ResponseEntity.ok(resp);
		}

		List<SalesvraBillResList> all = list.stream().map(SalesvraBillResList::new).collect(Collectors.toList());

		// group lists
		List<SalesvraBillResList> meals = new ArrayList<>();
		List<SalesvraBillResList> uniform = new ArrayList<>();
		List<SalesvraBillResList> shoe = new ArrayList<>();

		for (SalesvraBillResList d : all) {
			String type = d.getDeductiontype();

			if ("MEALS".equalsIgnoreCase(type)) {
				meals.add(d);
			} else if ("UNIFORM".equalsIgnoreCase(type)) {
				uniform.add(d);
			} else if ("SHOE".equalsIgnoreCase(type)) {
				shoe.add(d);
			}
		}

		// Build response (Java 8)
		Map<String, Object> resp = new LinkedHashMap<>();

		Map<String, Object> mealsMap = new LinkedHashMap<>();
		mealsMap.put("title", "Meals Charges");
		mealsMap.put("count", meals.size());
		mealsMap.put("details", meals);

		Map<String, Object> uniformMap = new LinkedHashMap<>();
		uniformMap.put("title", "Uniform");
		uniformMap.put("count", uniform.size());
		uniformMap.put("details", uniform);

		Map<String, Object> shoeMap = new LinkedHashMap<>();
		shoeMap.put("title", "Shoe");
		shoeMap.put("count", shoe.size());
		shoeMap.put("details", shoe);

		resp.put("meals", mealsMap);
		resp.put("uniform", uniformMap);
		resp.put("shoe", shoeMap);

		return ResponseEntity.ok(resp);
	}

	public ResponseEntity<?> getBills(Long month, Long year, Long branch, Long company) {

		Map<String, Object> response = new LinkedHashMap<>();

		try {
			// Step 1: Fetch records
			List<SalesvraBillList> projectionList = salesvraRepo.findBill(month, year, branch, company);

			// Step 2: Check empty
			if (projectionList == null || projectionList.isEmpty()) {
				response.put("status", false);
				response.put("message", "No bills found");
				response.put("voucher", Collections.emptyMap());
				return ResponseEntity.ok(response);
			}

			// Step 3: Convert Projection → DTO
			List<SalesvraBillResList> dtoList = projectionList.stream().map(data -> {
				SalesvraBillResList dto = new SalesvraBillResList(data);
				String todayDt = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
				dto.setDate(todayDt);
				int m = month.intValue();
				int y = year.intValue();

				LocalDate today = LocalDate.now();
				LocalDate fromDate;
				LocalDate toDate;

				// If selected month is current month
				if (today.getMonthValue() == m && today.getYear() == y) {
					fromDate = LocalDate.of(y, m, 1); // 01-currentMonth
					toDate = today; // current date
				} else {
					// Past or future month
					fromDate = LocalDate.of(y, m, 1); // First day
					toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth()); // Last day
				}

				DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				dto.setFromDate(fromDate.format(fmt));
				dto.setTodate(toDate.format(fmt));
				double amt = data.getAmt() != null ? data.getAmt() : 0.0;
				double cgst = data.getCgst() != null ? (amt * data.getCgst()) / 100 : 0.0;
				double sgst = data.getSgst() != null ? (amt * data.getSgst()) / 100 : 0.0;
				double igst = data.getIgst() != null ? (amt * data.getIgst()) / 100 : 0.0;

				dto.setAmount(amt + cgst + sgst + igst);

				return dto;
			}).collect(Collectors.toList());

			// Step 4: Extract company details from FIRST record
			SalesvraBillResList first = dtoList.get(0);

			String companyName = first.getCompanyname();
			String companyAddress = first.getCaddress();
			String companyCity = first.getCcity();
			String plant = first.getPlant();
			String date = first.getDate();
			String fromdate = first.getFromDate();
			String todate = first.getTodate();

			// ----------------------------------------
			// GROUP DATA INTO 3 LISTS
			// ----------------------------------------
			List<SalesvraBillResList> meals = new ArrayList<>();
			List<SalesvraBillResList> uniform = new ArrayList<>();
			List<SalesvraBillResList> shoe = new ArrayList<>();

			for (SalesvraBillResList d : dtoList) {
				if ("MEALS".equalsIgnoreCase(d.getDeductiontype())
						|| "meals_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					meals.add(d);
				} else if ("UNIFORM".equalsIgnoreCase(d.getDeductiontype())
						|| "uniform_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					uniform.add(d);
				} else if ("SHOE".equalsIgnoreCase(d.getDeductiontype())
						|| "shoe_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					shoe.add(d);
				}
			}

			// ----------------------------------------
			// BUILD VOUCHER OBJECT
			// ----------------------------------------
			Map<String, Object> voucher = new LinkedHashMap<>();

			// MEALS

			Map<String, Object> mealsMap = new LinkedHashMap<>();
			mealsMap.put("title", "Meals Charges");
			mealsMap.put("units", meals.size());
			mealsMap.put("details", meals);

			// UNIFORM

			Map<String, Object> uniformMap = new LinkedHashMap<>();
			uniformMap.put("title", "Uniform");
			uniformMap.put("units", uniform.size());
			uniformMap.put("details", uniform);

			// SHOE

			Map<String, Object> shoeMap = new LinkedHashMap<>();
			shoeMap.put("title", "Shoe");
			shoeMap.put("units", shoe.size());
			shoeMap.put("details", shoe);

			// Assign to voucher
			voucher.put("meals", mealsMap);
			voucher.put("uniform", uniformMap);
			voucher.put("shoe", shoeMap);

			// ----------------------------------------
			// FINAL RESPONSE JSON
			// ----------------------------------------
			response.put("companyName", companyName);
			response.put("companyAddress", companyAddress);
			response.put("companycity", companyCity);
			response.put("plant", plant);
			response.put("date", date);
			response.put("fromDate", fromdate);
			response.put("toDate", todate);
			response.put("voucher", voucher);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put("status", false);
			response.put("message", "Something went wrong");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	public ResponseEntity<?> getBill(Long month, Long year, Long branch, Long company) {

		Map<String, Object> response = new LinkedHashMap<>();

		try {
			// Step 1: Call repository
			List<SalesvraBillList> projectionList = salesvraRepo.findBill(month, year, branch, company);

			// Step 2: Check for empty records
			if (projectionList == null || projectionList.isEmpty()) {
				response.put("status", false);
				response.put("message", "No bills found");
				response.put("data", Collections.emptyList());
				return ResponseEntity.ok(response);
			}

			// Step 3: Convert Projection → DTO

			List<SalesvraBillResList> dtoList = projectionList.stream().map(SalesvraBillResList::new)
					.collect(Collectors.toList());
			// Step 4: Prepare success response
			response.put("status", true);
			response.put("message", "Bills fetched successfully");
			response.put("count", dtoList.size());
			response.put("data", dtoList);

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			// Step 5: Handle any errors
			response.put("status", false);
			response.put("message", "Something went wrong");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Transactional
	public ResponseEntity<?> saveVoucher(MessVoucherDto request, User loggedUser) {

		MessVoucher messVoucher = new MessVoucher(request);
		messVoucher.setVoucherno(generateVoucherNo());
		messVoucher.setCreateby(loggedUser.getId().intValue());
		messVoucher.setCreateon(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		messVoucher.setCreatestatus("Y");
		messVoucher.setStatus("Created");
		MessVoucher voucher = messVoucherRepository.save(messVoucher);

		List<Salesvra> list = salesvraRepo.findBillsByIds(request.getBillIds());

		list.stream().peek(s -> {
			s.setMessvoucher(voucher.getVoucherno());
			s.setMessvoucherDate(voucher.getVoucherdate());
		}).collect(Collectors.toList());

		salesvraRepo.saveAll(list);

		return new ResponseEntity<>(new Status("200", "DeductionVoucher saved successfully!!!"), HttpStatus.OK);
	}

	public Long generateVoucherNo() {
		Long lastNo = messVoucherRepository.getMaxVoucherNo();
		return lastNo + 1;
	}

	public ResponseEntity<?> getVoucherLists(Long voucherno, Long month, Long year, Long branch, Long company,
			String status, Pageable pageable) {

		Page<MessVoucherList> list = messVoucherRepository.findVoucher(voucherno, month, year, branch, company, status,
				pageable);

		// Convert list -> DTO (empty list also accepted)
		List<MessVoucherListDto> result = list.getContent().stream().map(MessVoucherListDto::new)
				.collect(Collectors.toList());

		// Always send correct empty page response
		PageDataDto<MessVoucherListDto> pageData = new PageDataDto<>(result, list);

		return new ResponseEntity<>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> getVoucherList(Long voucherno, Long month, Long year, Long branch, Long company,
			String status, Pageable pageable) {

		Page<MessVoucherList> list = messVoucherRepository.findVoucher(voucherno, month, year, branch, company, status,
				pageable);

		if (!list.isEmpty()) {

// Convert Projection -> DTO
			List<MessVoucherListDto> result = list.toList().stream().map(MessVoucherListDto::new)
					.collect(Collectors.toList());

// Wrap with your existing PageDataDto
			PageDataDto<MessVoucherListDto> pageData = new PageDataDto<>(result, list);

			return new ResponseEntity<>(pageData, HttpStatus.OK);

		} else {

			return new ResponseEntity<>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

	@Transactional
	public ResponseEntity<?> voucherVerifyOrApproveOrAuthorize(User user, Long id, String type) {
		Optional<MessVoucher> voucher = messVoucherRepository.findById(id);
		MessVoucher voucherEntry = voucher.get();
		if ("Verify".equalsIgnoreCase(type)) {
			if (voucherEntry.getStatus().equalsIgnoreCase("Created")) {
				voucherEntry.setVerifystatus("Y");
				voucherEntry.setVerifyon(LocalDateTime.now());
				voucherEntry.setVerifyby(user.getId().intValue());
				voucherEntry.setStatus("Verified");
			} else {
				return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
			}
		} else if ("Approve".equalsIgnoreCase(type)) {
			if (voucherEntry.getStatus().equalsIgnoreCase("Verified")) {
				voucherEntry.setApprovestatus("Y");
				voucherEntry.setApproveon(LocalDateTime.now());
				voucherEntry.setApproveby(user.getId().intValue());
				voucherEntry.setStatus("Approved");
			} else {
				return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
			}
		} else if ("Reject".equalsIgnoreCase(type)) {
			if (voucherEntry.getStatus().equalsIgnoreCase("Rejected")) {
				return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
			} else {
				if (voucherEntry.getStatus().equalsIgnoreCase("Created")) {
					voucherEntry.setVerifystatus("R");
					voucherEntry.setVerifyon(LocalDateTime.now());
					voucherEntry.setVerifyby(user.getId().intValue());
					voucherEntry.setStatus("Rejected");
					List<Salesvra> sales = salesvraRepo.findByMessvoucher(voucherEntry.getVoucherno());
					if (sales.isEmpty()) {
						return new ResponseEntity<>(
								new Status("400", "voucher id is not present" + voucherEntry.getId()),
								HttpStatus.BAD_REQUEST);
					}
					for (Salesvra s : sales) {
						s.setMessvoucher(null);
					}
					salesvraRepo.saveAll(sales);

				} else if (voucherEntry.getStatus().equalsIgnoreCase("Verified")) {
					voucherEntry.setApprovestatus("R");
					voucherEntry.setApproveon(LocalDateTime.now());
					voucherEntry.setApproveby(user.getId().intValue());
					voucherEntry.setStatus("Rejected");
					List<Salesvra> sales = salesvraRepo.findByMessvoucher(voucherEntry.getVoucherno());
					for (Salesvra s : sales) {
						s.setMessvoucher(null);
					}
					salesvraRepo.saveAll(sales);
				}
			}

		} else {
			return new ResponseEntity<>(new Status("400", "Invalid action type: " + type), HttpStatus.BAD_REQUEST);
		}

		messVoucherRepository.save(voucherEntry);

		return new ResponseEntity<>(new Status("200", "Voucher " + type + " successful"), HttpStatus.OK);
	}

	public ResponseEntity<?> getPrintForVoucher(List<Long> ids) {
		Map<String, Object> response = new LinkedHashMap<>();

		try {
			// Step 1: Fetch records
			List<MessVoucherPrintList> projectionList = messVoucherRepository.findVoucherForPrint(ids);

			// Step 2: Check empty
			if (projectionList == null || projectionList.isEmpty()) {
				response.put("status", false);
				response.put("message", "No bills found");
				response.put("voucher", Collections.emptyMap());
				return ResponseEntity.ok(response);
			}

			// Step 3: Convert Projection → DTO
			List<MessVoucherPrintListDto> dtoList = projectionList.stream().map(data -> {
				MessVoucherPrintListDto dto = new MessVoucherPrintListDto(data);
				int m = data.getMonth().intValue();
				int y = data.getYear().intValue();
				LocalDate today = LocalDate.now();
				LocalDate fromDate;
				LocalDate toDate;
				// If selected month is current month
				if (today.getMonthValue() == m && today.getYear() == y) {
					fromDate = LocalDate.of(y, m, 1); // 01-currentMonth
					toDate = today; // current date
				} else {
					// Past or future month
					fromDate = LocalDate.of(y, m, 1); // First day
					toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth()); // Last day
				}

				DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				dto.setDate(data.getDate());
				dto.setFromDate(fromDate.format(fmt));
				dto.setTodate(data.getDate());
				double amt = data.getAmount() != null ? data.getAmount() : 0.0;
				double cgst = data.getCgst() != null ? (amt * data.getCgst()) / 100 : 0.0;
				double sgst = data.getSgst() != null ? (amt * data.getSgst()) / 100 : 0.0;
				double igst = data.getIgst() != null ? (amt * data.getIgst()) / 100 : 0.0;

				dto.setAmount(amt + cgst + sgst + igst);

				return dto;
			}).collect(Collectors.toList());

			// Step 4: Extract company details from FIRST record
			MessVoucherPrintListDto first = dtoList.get(0);

			String companyName = first.getCompanyname();
			String companyAddress = first.getCaddress();
			String companyCity = first.getCcity();
			String plant = first.getPlant();
			String date = first.getDate();
			String fromdate = first.getFromDate();
			String todate = first.getTodate();
			String voucherno = first.getVoucherno();
			String createdByName = first.getCreatedByName();
			String verifiedByName = first.getVerifiedByName();
			String approvedByName = first.getApprovedByName();

			// ----------------------------------------
			// GROUP DATA INTO 3 LISTS
			// ----------------------------------------
			List<MessVoucherPrintListDto> meals = new ArrayList<>();
			List<MessVoucherPrintListDto> uniform = new ArrayList<>();
			List<MessVoucherPrintListDto> shoe = new ArrayList<>();

			for (MessVoucherPrintListDto d : dtoList) {
				if ("MEALS".equalsIgnoreCase(d.getDeductiontype())
						|| "meals_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					meals.add(d);
				} else if ("UNIFORM".equalsIgnoreCase(d.getDeductiontype())
						|| "uniform_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					uniform.add(d);
				} else if ("SHOE".equalsIgnoreCase(d.getDeductiontype())
						|| "shoe_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					shoe.add(d);
				}
			}

			// ----------------------------------------
			// BUILD VOUCHER OBJECT
			// ----------------------------------------
			Map<String, Object> voucher = new LinkedHashMap<>();

			// MEALS

			Map<String, Object> mealsMap = new LinkedHashMap<>();
			mealsMap.put("title", "Meals Charges");
			mealsMap.put("units", meals.size());
			mealsMap.put("details", meals);

			// UNIFORM

			Map<String, Object> uniformMap = new LinkedHashMap<>();
			uniformMap.put("title", "Uniform");
			uniformMap.put("units", uniform.size());
			uniformMap.put("details", uniform);

			// SHOE

			Map<String, Object> shoeMap = new LinkedHashMap<>();
			shoeMap.put("title", "Shoe");
			shoeMap.put("units", shoe.size());
			shoeMap.put("details", shoe);

			// Assign to voucher
			voucher.put("meals", mealsMap);
			voucher.put("uniform", uniformMap);
			voucher.put("shoe", shoeMap);

			// ----------------------------------------
			// FINAL RESPONSE JSON
			// ----------------------------------------
			response.put("companyName", companyName);
			response.put("companyAddress", companyAddress);
			response.put("companycity", companyCity);
			response.put("plant", plant);
			response.put("date", date);
			response.put("fromDate", fromdate);
			response.put("toDate", todate);
			response.put("voucherno", voucherno);
			response.put("createdByName", createdByName);
			response.put("verifiedByName", verifiedByName);
			response.put("approvedByName", approvedByName);
			response.put("voucher", voucher);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put("status", false);
			response.put("message", "Something went wrong");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	public ResponseEntity<?> getVoucherById(Long id) {

		Map<String, Object> response = new LinkedHashMap<>();

		try {
			// Step 1: Fetch records
			List<MessVoucherPrintList> projectionList = messVoucherRepository.findVoucherById(id);

			// Step 2: Check empty
			if (projectionList == null || projectionList.isEmpty()) {
				response.put("status", false);
				response.put("message", "No bills found");
				response.put("voucher", Collections.emptyMap());
				return ResponseEntity.ok(response);
			}

			// Step 3: Convert Projection → DTO
			List<MessVoucherPrintListDto> dtoList = projectionList.stream().map(data -> {
				MessVoucherPrintListDto dto = new MessVoucherPrintListDto(data);
				int m = data.getMonth().intValue();
				int y = data.getYear().intValue();
				LocalDate today = LocalDate.now();
				LocalDate fromDate;
				LocalDate toDate;
				// If selected month is current month
				if (today.getMonthValue() == m && today.getYear() == y) {
					fromDate = LocalDate.of(y, m, 1); // 01-currentMonth
					toDate = today; // current date
				} else {
					// Past or future month
					fromDate = LocalDate.of(y, m, 1); // First day
					toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth()); // Last day
				}

				DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				dto.setDate(data.getDate());
				dto.setTodate(data.getDate());
				dto.setFromDate(fromDate.format(fmt));
				double amt = data.getAmount() != null ? data.getAmount() : 0.0;
				double cgst = data.getCgst() != null ? (amt * data.getCgst()) / 100 : 0.0;
				double sgst = data.getSgst() != null ? (amt * data.getSgst()) / 100 : 0.0;
				double igst = data.getIgst() != null ? (amt * data.getIgst()) / 100 : 0.0;

				dto.setAmount(amt + cgst + sgst + igst);

				return dto;
			}).collect(Collectors.toList());

			// Step 4: Extract company details from FIRST record
			MessVoucherPrintListDto first = dtoList.get(0);

			String companyName = first.getCompanyname();
			String companyAddress = first.getCaddress();
			String companyCity = first.getCcity();
			String plant = first.getPlant();
			String date = first.getDate();
			String fromdate = first.getFromDate();
			String todate = first.getTodate();
			String voucherno = first.getVoucherno();
			String createdByName = first.getCreatedByName();
			String verifiedByName = first.getVerifiedByName();
			String approvedByName = first.getApprovedByName();

			// ----------------------------------------
			// GROUP DATA INTO 3 LISTS
			// ----------------------------------------
			List<MessVoucherPrintListDto> meals = new ArrayList<>();
			List<MessVoucherPrintListDto> uniform = new ArrayList<>();
			List<MessVoucherPrintListDto> shoe = new ArrayList<>();

			for (MessVoucherPrintListDto d : dtoList) {
				if ("MEALS".equalsIgnoreCase(d.getDeductiontype())
						|| "meals_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					meals.add(d);
				} else if ("UNIFORM".equalsIgnoreCase(d.getDeductiontype())
						|| "uniform_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					uniform.add(d);
				} else if ("SHOE".equalsIgnoreCase(d.getDeductiontype())
						|| "shoe_chargers".equalsIgnoreCase(d.getDeductiontype())) {
					shoe.add(d);
				}
			}

			// ----------------------------------------
			// BUILD VOUCHER OBJECT
			// ----------------------------------------
			Map<String, Object> voucher = new LinkedHashMap<>();

			// MEALS

			Map<String, Object> mealsMap = new LinkedHashMap<>();
			mealsMap.put("title", "Meals Charges");
			mealsMap.put("units", meals.size());
			mealsMap.put("details", meals);

			// UNIFORM

			Map<String, Object> uniformMap = new LinkedHashMap<>();
			uniformMap.put("title", "Uniform");
			uniformMap.put("units", uniform.size());
			uniformMap.put("details", uniform);

			// SHOE

			Map<String, Object> shoeMap = new LinkedHashMap<>();
			shoeMap.put("title", "Shoe");
			shoeMap.put("units", shoe.size());
			shoeMap.put("details", shoe);

			// Assign to voucher
			voucher.put("meals", mealsMap);
			voucher.put("uniform", uniformMap);
			voucher.put("shoe", shoeMap);

			// ----------------------------------------
			// FINAL RESPONSE JSON
			// ----------------------------------------
			response.put("companyName", companyName);
			response.put("companyAddress", companyAddress);
			response.put("companycity", companyCity);
			response.put("plant", plant);
			response.put("date", date);
			response.put("fromDate", fromdate);
			response.put("toDate", todate);
			response.put("voucherno", voucherno);
			response.put("createdByName", createdByName);
			response.put("verifiedByName", verifiedByName);
			response.put("approvedByName", approvedByName);
			response.put("voucher", voucher);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put("status", false);
			response.put("message", "Something went wrong");
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

}
