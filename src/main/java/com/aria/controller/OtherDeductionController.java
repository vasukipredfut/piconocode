package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.MessVoucherDto;
import com.aria.dto.OtherDeductionReqDTO;
import com.aria.dto.PageDto;
import com.aria.dto.SalesvraReqDto;
import com.aria.model.OtherDeduction;
import com.aria.model.User;
import com.aria.service.OtherDeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OtherDeductionController {

	@Autowired
	OtherDeductionService otherDeductionService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveOtherDeduction")
	public ResponseEntity<?> saveOtherDeduction(@RequestBody OtherDeductionReqDTO deductionReqDTO,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return otherDeductionService.saveOtherDeduction(deductionReqDTO, user.get());
	}

	// (19-08-2025) changed by sivanantham(added two new
	// filters(employeename,deductiontype) for get other Deduction list
	// and changed one parameter (category into empType)
	// change suggested by alagu sir
	@GetMapping("/otherDeductionList")
	public ResponseEntity<?> otherDeductionList(@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year,
			@RequestParam(value = "company", required = false) Long company,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "emptype", required = false) String emptype,
			@RequestParam(value = "employeename", required = false) String employeename,
			@RequestParam(value = "deductiontype", required = false) String deductiontype, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return otherDeductionService.otherDeductionList(month, year, company, branch, dept, emptype, employeename,
					deductiontype, pageable);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	// (26-08-2025) created by sivanantham (create api for bill generation)
	// change suggested by alagu sir
	@GetMapping("/otherDeductionListForBill")
	public ResponseEntity<?> otherDeductionListForBill(@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year,
			@RequestParam(value = "company", required = false) Long company,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "emptype", required = false) String category,
			@RequestParam(value = "deductiontype", required = false) String deductiontype, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return otherDeductionService.otherDeductionListForBill(month, year, company, branch, dept, category,
					deductiontype, pageable);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	// create a new API for other deduction employees list and with filter

	@GetMapping("/otherDeductionListfillter")
	public ResponseEntity<?> otherDeductionListFillter(
			@RequestParam(value = "emptype", required = false) String emptype,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return otherDeductionService.otherDeductionListFillter(emptype, branch, dept, type, month, year, pageable);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	// (26-08-2025) created by sivanantham (create new API for Delete
	// OtherDeduction)
	// suggested by alagu sir
	@DeleteMapping("/deleteOtherDeduction")
	public ResponseEntity<?> deleteOtherDeduction(@RequestBody List<Long> otherDeductionIdList,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return otherDeductionService.deleteOtherDeduction(otherDeductionIdList);
	}

	// (03-08-2025) created by sivanantham (create new API for save Bill)
	// verified by alagu sir
	@PostMapping("/saveDeductionBill")
	public ResponseEntity<?> saveDeductionBill(@RequestBody SalesvraReqDto deductionReqDTO,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return otherDeductionService.saveDeductionBills(deductionReqDTO, user.get());
	}

	@GetMapping("/salesvraBillListFilter")
	public ResponseEntity<?> salesvraBillListFilter(@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year,
			@RequestParam(value = "company", required = false) Long company,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "emptype", required = false) String category,
			@RequestParam(value = "employeename", required = false) String employeename,
			@RequestParam(value = "billno", required = false) String billno,
			@RequestParam(value = "deductiontype", required = false) String deductiontype, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return otherDeductionService.salesvrabillListFilter(month, year, company, branch, dept, category,
					deductiontype, billno, employeename, pageable);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/getSalesvraBillDeatails")
	public ResponseEntity<?> getSalesvraBillDeatails(@RequestBody List<Long> id, Authentication authentication) {
		return otherDeductionService.getSalesvraBillsForPrint(id);
	}

	@GetMapping("/salesvraListForVoucher")
	public ResponseEntity<?> SalesvraListForVoucher(@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year,
			@RequestParam(value = "company", required = false) Long company,
			@RequestParam(value = "branch", required = false) Long branch, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return otherDeductionService.getBills(month, year, branch, company);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/saveDeductionVoucher")
	public ResponseEntity<?> saveDeductionVoucher(@RequestBody MessVoucherDto request, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return otherDeductionService.saveVoucher(request, user.get());
	}

	@GetMapping("/getVoucherList")
	public ResponseEntity<?> getVoucherList(@RequestParam(required = false) Long voucherno,
			@RequestParam(required = false) Long month, @RequestParam(required = false) Long year,
			@RequestParam(required = false) Long branch, @RequestParam(required = false) Long company,
			@RequestParam(required = false) String status,Pageable pageable) {
		return otherDeductionService.getVoucherLists(voucherno, month, year, branch, company, status,pageable);
	}

	@PutMapping("/voucherVerifyOndApprove")
	public ResponseEntity<?> voucherVerifyOrApproveOrAuthorize(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "type", required = false) String type, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return otherDeductionService.voucherVerifyOrApproveOrAuthorize(user.get(), id, type);
	}
	
	@PostMapping("/getVoucherPrintDetails")
	public ResponseEntity<?> getVoucherPrintDeatails(@RequestBody List<Long> ids) {
		return otherDeductionService.getPrintForVoucher(ids);
	}
	
	@GetMapping("/getVoucherById/{id}")
	public ResponseEntity<?> getVoucherById(@PathVariable Long id){
		return otherDeductionService.getVoucherById(id);
	}
}
