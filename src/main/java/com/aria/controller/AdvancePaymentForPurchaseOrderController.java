package com.aria.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.AdvancePaymentForPurchaseOrderDto;
import com.aria.model.User;
import com.aria.service.AdvancePaymentForPurchaseOrderService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AdvancePaymentForPurchaseOrderController {

	@Autowired
	private AdvancePaymentForPurchaseOrderService advancePaymentForPurchaseOrderService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/advancePayment/savePayment")
	public ResponseEntity<?> savePayment(
			@RequestBody AdvancePaymentForPurchaseOrderDto advancePaymentForPurchaseOrderDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return advancePaymentForPurchaseOrderService.savePayment(advancePaymentForPurchaseOrderDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/advancePayment/updatePayment")
	public ResponseEntity<?> updatePayment(
			@RequestBody AdvancePaymentForPurchaseOrderDto advancePaymentForPurchaseOrderDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return advancePaymentForPurchaseOrderService.updatePayment(advancePaymentForPurchaseOrderDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/advancePayment/getFilterAllPayments")
	public ResponseEntity<?> getFilterAllPayments(@RequestParam(value = "poNumber", required = false) String poNumber,
			@RequestParam(value = "firmId", required = false) Long firmId,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "vendorId", required = false) Long vendorId,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "trnNo", required = false) String trnNo,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate, Pageable page,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return advancePaymentForPurchaseOrderService.getAllFilterPayments(page, poNumber, vendorId, status, trnNo , firmId,
				branch, dept, fromDate, toDate, user.get());
	}

	@GetMapping("/advancePayment/getPayment/{id}")
	public ResponseEntity<?> getAllPayments(@PathVariable("id") Long id, Authentication authentication) {
		return advancePaymentForPurchaseOrderService.getPaymentById(id);
	}

	@PostMapping("/changeadvancePaymentStatus")
	public ResponseEntity<?> changeadvancePaymentStatus(@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "advancePaymentId", required = true) Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			if (status.equalsIgnoreCase("Reject")) {
				System.out.println(status);
				return advancePaymentForPurchaseOrderService.advancePaymentReject(id, user.get());
			} else {
				return advancePaymentForPurchaseOrderService.changeadvancePaymentStatus(id, user, status);
			}
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

}
