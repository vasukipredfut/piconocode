package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.PurchaseBillApproveDto;
import com.aria.dto.WorkFlowDto;
import com.aria.model.User;
import com.aria.service.PurchaseBillApproveService;

@RestController
public class PurchaseBillApproveController {

	@Autowired
	public PurchaseBillApproveService purchaseBillApproveService;

	@Autowired
	public UserRepository userRepository;

	// PurchaseClaimPremium

	@PostMapping("/api/purchaseBillApprove")
	public ResponseEntity<?> savePurchaseClaimPremium(@RequestBody PurchaseBillApproveDto purchaseBillApproveDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return purchaseBillApproveService.savePurchaseBillApprove(purchaseBillApproveDto, user.get());
	}

	@PutMapping("/api/purchaseBillApprove")
	public ResponseEntity<?> updatePurchaseClaimPremium(@RequestBody PurchaseBillApproveDto purchaseBillApproveDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return purchaseBillApproveService.updatePurchaseBillApprove(purchaseBillApproveDto, user.get());
	}

	@GetMapping("/api/listAllPurchaseBillApprove")
	public ResponseEntity<?> listAllPurchaseBillApprove(Authentication authentication, Pageable pageable) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return purchaseBillApproveService.listAllPurchaseBillApprove(pageable);
		} else {
			return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/api/getPurchaseBillApproveDetails/{id}")
	public ResponseEntity<?> getPurchaseBillApproveDetails(@PathVariable String id, Authentication authentication) {

		return purchaseBillApproveService.getPurchaseBillApproveDetails(id);
	}

	@DeleteMapping("/api/deletePurchaseBillApproveDetails/{id}")
	public ResponseEntity<?> deletePurchaseBillApproveDetails(@PathVariable String id, Authentication authentication) {

		return purchaseBillApproveService.deletePurchaseBillApproveDetails(id);
	}

}
