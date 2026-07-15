
package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.OtherBranchReceiptDto;

import com.aria.model.User;
import com.aria.service.OtherBranchReceiptService;

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
public class OtherBranchReceiptController {

	@Autowired
	OtherBranchReceiptService otherBranchReceiptService;

	@Autowired
	UserRepository userRepository;


	@PostMapping("/saveOtherBranchReceipt")
	public ResponseEntity<?> saveOtherBranchReceipt(@RequestBody OtherBranchReceiptDto otherBranchReceiptDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return otherBranchReceiptService.updateOtherBranchReceipt(otherBranchReceiptDto, user.get());
	}

	@PutMapping("/updateOtherBranchReceipt")
	public ResponseEntity<?> updateOtherBranchReceipt(@RequestBody OtherBranchReceiptDto otherBranchReceiptDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return otherBranchReceiptService.updateOtherBranchReceipt(otherBranchReceiptDto, user.get());
	}

	@GetMapping("/getOtherBranchReceiptList")
	public ResponseEntity<?> getOtherBranchReceiptList(@RequestParam(value = "firmId", required = false) Long firmId,
													   @RequestParam(value = "branch", required = false) Long branch,
													   @RequestParam(value = "deptId", required = false) Long deptId,
													   @RequestParam(value = "requestId", required = false) Long requestId,
													   @RequestParam(value = "hupId", required = false) String hupId,
													   @RequestParam(value = "dktNo", required = false) String dktNo,
													   @RequestParam(value = "issueDeptName", required = false) String issueDeptName,
													   @RequestParam(value = "issueBranchName", required = false) String issueBranchName,
													   @RequestParam(value = "fromDate", required = false) String fromDate,
													   @RequestParam(value = "toDate", required = false) String toDate,
													   @RequestParam(value = "status", required = false) String status,
													   Pageable pageable, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			boolean isValidRequest = otherBranchReceiptService.isValidRequestForUser(branch, deptId);
			if(isValidRequest)
			return otherBranchReceiptService.filterListAll( firmId , branch, deptId, requestId, hupId, dktNo, issueDeptName, issueBranchName, fromDate, toDate, status, pageable);
		} else {
			return new ResponseEntity<List<OtherBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
		}
		return new ResponseEntity<List<OtherBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
	}

	@GetMapping("/getOtherBranchReceiptDetails/{id}")
	public ResponseEntity<?> getOtherBranchReceiptDetails(@PathVariable String id, Authentication authentication) {
		return otherBranchReceiptService.getOtherBranchReceipt(id);
	}

	@DeleteMapping("/deleteOtherBranchReceiptDetails/{id}")
	public ResponseEntity<?> deleteOtherBranchReceiptDetails(@PathVariable String id, Authentication authentication) {
		return otherBranchReceiptService.deleteOtherBranchReceipt(id);
	}
}
