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
import com.aria.dto.SalesCeilingStockLimitDto;
import com.aria.dto.SalesFinalCapacityRequest;
import com.aria.dto.SalesFinalLocationRequest;
import com.aria.dto.SalesFinalProductRequest;
import com.aria.dto.SalesProdBranchRequest;
import com.aria.dto.SalesProductFilmDto;
import com.aria.dto.SalesProductFilmOrderDto;
import com.aria.dto.SalesProductFilmPricesRequest;
import com.aria.model.User;
import com.aria.service.SalesProductFilmService;

@RestController
@RequestMapping("/api")
public class SalesProductFilmController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	SalesProductFilmService salesProductFilmService;

	@PostMapping("/saveSalesProductFilm")
	public ResponseEntity<?> saveSalesProductFilm(@RequestBody SalesProductFilmDto salesProductFilmDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return salesProductFilmService.saveSalesProductFilm(salesProductFilmDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}


	@PutMapping("/updateSalesProductFilm")
	public ResponseEntity<?> updateSalesProductFilm(@RequestBody SalesProductFilmDto salesProductFilmDto,
												  Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return salesProductFilmService.updateSalesProductFilm(salesProductFilmDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/salesProductFilm/{id}")
	public ResponseEntity<?> deleteSalesProductFilm(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.deleteSalesProductFilm(id);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveSalesFilmLocation")
	public ResponseEntity<?> saveSalesFilmLocation(@RequestBody SalesFinalLocationRequest salesProductFilmDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveSalesLocation(salesProductFilmDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveSalesFilmCapacity")
	public ResponseEntity<?> saveSalesFilmCapacity(@RequestBody SalesFinalCapacityRequest salesFinalCapacityRequest,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveSalesCapacity(salesFinalCapacityRequest.getCapacity());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveSalesFilmBranch")
	public ResponseEntity<?> saveSalesFilmBranch(@RequestBody SalesProdBranchRequest salesFinalCapacityRequest,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveSalesBranches(salesFinalCapacityRequest.getBranchId());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveSalesFinalProduct")
	public ResponseEntity<?> saveSalesFinalProduct(@RequestBody SalesFinalProductRequest salesFinalProductRequest,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveSalesFinalProduct(salesFinalProductRequest, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/saveSalesPrices")
	public ResponseEntity<?> saveSalesPrices(@RequestBody List<SalesProductFilmPricesRequest> salesFinalProductRequest,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.savePrices(salesFinalProductRequest, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesProductGroup")
	public ResponseEntity<?> getSalesProductGroup(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesProductFilmGroup();
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesPrices")
	public ResponseEntity<?> getSalesPrices(@RequestParam(value = "locationType", required = false) String locationType,
			@RequestParam(value = "salesProductGroupId", required = false) Long salesProductGroupId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getPrices(locationType, salesProductGroupId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesProductCapacity")
	public ResponseEntity<?> getSalesProductCapacity(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesProductFilmCapacity();
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesProductBranches")
	public ResponseEntity<?> getSalesProductBranches(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesProductFilmBranches();
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesProductName")
	public ResponseEntity<?> getSalesProductName(
			@RequestParam(value = "productGroup", required = false) Long productGroup, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesProductName(productGroup);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesLocation")
	public ResponseEntity<?> getSalesLocation(
			@RequestParam(value = "locationType", required = false) String locationType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesLocation(locationType);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/salesProductFilm")
	public ResponseEntity<?> getSalesProductFilm(@RequestParam(value = "productId", required = false) Long productId,
			@RequestParam(value = "productName", required = false) String productName,
			@RequestParam(value = "productGroup", required = false) String productGroup,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "finalProductName", required = false) String finalProductName,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesProductFilm(productId, productName, productGroup, branch, finalProductName, dateFrom, dateTo, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	/* ---------- Ceiling Stock ---------- */

	@PostMapping("/saveCeilingStockLimit")
	public ResponseEntity<?> saveCeilingStockLimit(@RequestBody SalesCeilingStockLimitDto salesCeilingStockLimitDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveCeilingStockLimit(salesCeilingStockLimitDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PutMapping("/updateCeilingStockLimit")
	public ResponseEntity<?> updateCeilingStockLimit(@RequestBody SalesCeilingStockLimitDto salesCeilingStockLimitDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.updateCeilingStockLimit(salesCeilingStockLimitDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getSalesCeilingStockLimitByFilter")
	public ResponseEntity<?> getSalesCeilingStockLimitByFilter(
			@RequestParam(value = "capacity", required = false) String capacity,
			@RequestParam(value = "productGroup", required = false) String productGroup,
			@RequestParam(value = "branch", required = false) String branch, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesCeilingStockLimitByFilter(capacity, productGroup, branch, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/getSalesCeilingStockLimit")
	public ResponseEntity<?> getSalesCeilingStockLimit(@RequestBody SalesCeilingStockLimitDto salesCeilingStockLimitDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesCeilingStockLimit(salesCeilingStockLimitDto.getProductGroupId(),
					salesCeilingStockLimitDto.getPlantId(), salesCeilingStockLimitDto.getCapacity());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getCeilingStockLimitById/{id}")
	public ResponseEntity<?> getCeilingStockLimitById(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getSalesCeilingStockLimitById(id);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@DeleteMapping("/deleteCeilingStockLimit/{id}")
	public ResponseEntity<?> deleteSalesCeilingStockLimit(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.deleteSalesCeiling(id);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	/* ----------film Order---------- */
	@PostMapping("/saveFilmOrder")
	public ResponseEntity<?> saveFilmOrder(@RequestBody List<SalesProductFilmOrderDto> dto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.saveFilmOrders(dto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@PutMapping("/updatePendingQty")
	public ResponseEntity<?> updatePendingQty(@RequestParam(value = "mrnNo", required = false) Long mrnNo,
			@RequestParam(value = "completedQty", required = false) Double completedQty,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.updatePendingQty(mrnNo, completedQty);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/getFilmOrderList")
	public ResponseEntity<?> getFilmOrderList(Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getAll(pageable);
		else
			return ResponseEntity.badRequest().body("User not found");

	}

	@GetMapping("/getFilmOrderById/{id}")
	public ResponseEntity<?> getFilmOrderById(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getFilmOrderById(id);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getPendingStock")
	public ResponseEntity<?> getPendingStock(@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "subLocationId", required = false) Long subLocationId,
			@RequestParam(value = "filmId", required = false) Long filmId,
			@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "lot", required = false) Long lot, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getPendingStock(location, subLocationId, filmId, branch, lot);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getMrpDetails")
	public ResponseEntity<?> getMrpDetails(@RequestParam(value = "locationId", required = false) Long locationId,
			@RequestParam(value = "salesProductGroupId", required = false) Long salesProductGroupId,
			@RequestParam(value = "productId", required = false) Long productId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getMrpDetails(locationId, salesProductGroupId, productId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getStockInPouch")
	public ResponseEntity<?> getStockInPouch(@RequestParam(value = "branch", required = false) Long branch,
			@RequestParam(value = "dept", required = false) Long dept,
			@RequestParam(value = "productId", required = false) Long productId,
			@RequestParam(value = "lot", required = false) Long lot, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getStockInPouch(branch, dept, productId, lot);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getFilmOrderByFilter")
	public ResponseEntity<?> getFilmOrderByFilter(@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "pageFlag", required = false) String pageFlag,
			@RequestParam(value = "orderDate", required = false) String orderDate, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return salesProductFilmService.getFilmOrderByFilter(status, orderDate, pageFlag, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

}
