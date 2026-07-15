package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.LabelCeilingStockLimitDto;
import com.aria.dto.LabelLocationDto;
import com.aria.dto.LabelPricesDto;
import com.aria.dto.LabelProductNameDto;
import com.aria.dto.SalesProductLabelDto;
import com.aria.dto.SalesProductLabelOrderDto;
import com.aria.model.User;
import com.aria.service.SalesProductLabelService;

@RestController
@RequestMapping("/api")
public class SalesProductLabelController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	SalesProductLabelService salesProductLabelService;

	@GetMapping("/LabelProductGroup")
	public ResponseEntity<?> getLabelProductGroup(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getSalesProductLabelGroup();
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveLabelFinalProduct")
	public ResponseEntity<?> saveLabelFinalProduct(@RequestBody LabelProductNameDto dto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.saveLabelFinalProduct(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getAllLabelProductByGroup")
	public ResponseEntity<?> getAllLabelProductByGroup(
			@RequestParam(value = "productGroupId", required = false) Long productGroupId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getAllLabelProductByGroup(productGroupId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveLabelLocation")
	public ResponseEntity<?> saveLabelLocation(@RequestBody LabelLocationDto dto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.saveLabelLocation(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelLocationByType")
	public ResponseEntity<?> getLabelLocation(@RequestParam(value = "type", required = false) String type,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelLocation(type);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveLabelPrices")
	public ResponseEntity<?> saveLabelPrices(@RequestBody List<LabelPricesDto> priceDtoList,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.savePriceList(priceDtoList, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelPricesByFilter")
	public ResponseEntity<?> getLabelPrices(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "productGroupId", required = false) Long productGroupId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelPrices(type, productGroupId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveLabelProduct")
	public ResponseEntity<?> saveLabelProduct(@RequestBody SalesProductLabelDto dto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return salesProductLabelService.saveLabelProduct(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}


	@PostMapping("/updateLabelProduct")
	public ResponseEntity<?> updateLabelProduct(@RequestBody SalesProductLabelDto dto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return salesProductLabelService.updateLabelProduct(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}


	@GetMapping("/getLabelProduct")
	public ResponseEntity<?> getLabelProduct(@RequestParam(value = "productId", required = false) Long productId,
			@RequestParam(value = "productName", required = false) String productName,
			@RequestParam(value = "productGroup", required = false) String productGroup,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "finalProductName", required = false) String finalProductName,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate,
											 Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelProduct(productId, productName, productGroup, branch,
					finalProductName, fromDate, toDate, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	/* ---------- Ceiling Stock ---------- */

	@PostMapping("/saveLabelCeilingStockLimit")
	public ResponseEntity<?> saveLabelCeilingStockLimit(@RequestBody LabelCeilingStockLimitDto dto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.saveCeilingStockLimit(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PutMapping("/updateLabelCeilingStockLimit")
	public ResponseEntity<?> updateLabelCeilingStockLimit(@RequestBody LabelCeilingStockLimitDto dto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.updateCeilingStockLimit(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/deleteLabelCeilingStockLimit/{id}")
	public ResponseEntity<?> deleteLabelSalesCeilingStockLimit(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.deleteSalesCeiling(id);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/getLabelCeilingStockLimitByFilter")
	public ResponseEntity<?> getLabelCeilingStockLimitByFilter(
			@RequestParam(value = "capacity", required = false) String capacity,
			@RequestParam(value = "productGroup", required = false) String productGroup,
			@RequestParam(value = "branch", required = false) String branch, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelCeilingStockLimitByFilter(capacity, productGroup, branch, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelCeilingStockLimitById/{id}")
	public ResponseEntity<?> getCeilingStockLimitById(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelCeilingStockLimitById(id);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	/* ---------- Label Order ---------- */

	@PostMapping("/saveLabelOrder")
	public ResponseEntity<?> saveFilmOrder(@RequestBody List<SalesProductLabelOrderDto> dto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.saveLabelOrders(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@PutMapping("/updateLabelPendingQty")
	public ResponseEntity<?> updateLabelPendingQty(@RequestParam(value = "mrnNo", required = false) Long mrnNo,
			@RequestParam(value = "completedQty", required = false) Double completedQty,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.updateLabelPendingQty(mrnNo, completedQty);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/getlabelOrderList")
	public ResponseEntity<?> getLabelOrderList(Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getAll(pageable);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/getLabelOrderById/{id}")
	public ResponseEntity<?> getLabelOrderById(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelOrderById(id);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelPendingStock")
	public ResponseEntity<?> getLabelPendingStock(@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "subLocationId", required = false) Long subLocationId,
			@RequestParam(value = "labelId", required = false) Long labelId,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "lot", required = false) Long lot, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelPendingStock(location, subLocationId, labelId, branch, lot);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelMrpDetails")
	public ResponseEntity<?> getLabelMrpDetails(@RequestParam(value = "locationId", required = false) Long locationId,
			@RequestParam(value = "productGroupId", required = false) Long ProductGroupId,
			@RequestParam(value = "productId", required = false) Long productId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelMrpDetails(locationId, ProductGroupId, productId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelStockInPouch")
	public ResponseEntity<?> getLabelStockInPouch(@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "productId", required = false) Long productId,
			@RequestParam(value = "lot", required = false) Long lot, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelStockInPouch(branch, dept, productId, lot);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getLabelOrderByFilter")
	public ResponseEntity<?> getLabelOrderByFilter(@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "pageFlag", required = false) String pageFlag,
			@RequestParam(value = "orderDate", required = false) String orderDate, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductLabelService.getLabelOrderByFilter(status, orderDate, pageFlag, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

}
