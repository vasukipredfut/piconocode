package com.aria.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.DpatchDispatchDto;
import com.aria.model.User;
import com.aria.service.DpatchDespatchService;

@RestController
@RequestMapping("/api/despatch")
@CrossOrigin
public class DpatchDespatchController {
	@Autowired
	private DpatchDespatchService dpatchDespatchService;

	@Autowired
	private UserRepository UserRepository;

	@PostMapping("/createDespatch")
	public ResponseEntity<?> createDespatch(@RequestBody DpatchDispatchDto dpatchDespatchDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = UserRepository.findByUsername(userPrincipal.getUsername());

		if (user != null) {
			return dpatchDespatchService.saveDispatch(dpatchDespatchDto, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

	@DeleteMapping("/deleteDpatch")
	public ResponseEntity<?> deleteDpatch(@RequestParam("orderId") Long dispatchId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = UserRepository.findByUsername(userPrincipal.getUsername());

		return dpatchDespatchService.deleteDespatch(dispatchId);
	}

	@PostMapping("/changeDespatchOrderStatus")
	public ResponseEntity<?> changeDespatchOrderStatus(@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "dispatchId", required = true) Long dispatchId,
			@RequestParam(value = "binStockId", required = false) Long binStockId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = UserRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			if (status.equalsIgnoreCase("approved")) {
				return dpatchDespatchService.dispatchApprove(dispatchId, user.get());
			} else {
				return dpatchDespatchService.dispatchReject(dispatchId);
			}
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

	@GetMapping("/getAllDespatchOrder")
	public ResponseEntity<?> getAllDespatchOrderByStatus( @RequestParam(value = "firmId",required = false) Long firmId,
														  @RequestParam(value = "branchId",required = false) Long branchId,
														  @RequestParam(value = "orderNumber",required = false) Long orderNumber,
														  @RequestParam(value = "dispatchDate ",required = false) String dispatchDate ,
														  @RequestParam(value = "vendorName",required = false) String vendorName,
														  @RequestParam(value = "billNo",required = false) String billNo,
														  @RequestParam(value = "status",required = false) String status,
			@RequestParam("type") Long type, Authentication authentication, Pageable page) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = UserRepository.findByUsername(userPrincipal.getUsername());

		return dpatchDespatchService.getAllByStatus(firmId, branchId, orderNumber , billNo,  dispatchDate , vendorName , status, type, page);
	}

	@GetMapping("/productsBasedOnDispatchId")
	public ResponseEntity<?> productsBasedOnDispatchId(@RequestParam("despatchOrderId") Long orderId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = UserRepository.findByUsername(userPrincipal.getUsername());

		return dpatchDespatchService.productsBasedOnDispatchId(orderId);
	}



}
