package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.No2ProductsPartyRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.No2ProductsDto;
import com.aria.dto.No2ProductsPartyDto;
import com.aria.dto.No2ProductsPriceDto;
import com.aria.dto.No2billsDto;
import com.aria.dto.No2billsUpdateDto;
import com.aria.model.User;
import com.aria.service.No2ProductService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class No2ProductsController {

	@Autowired
	No2ProductService no2productService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	No2ProductsPartyRepository no2ProductsPartyRepository;

	@PostMapping("/createno2product")
	public ResponseEntity<?> createno2product(@RequestBody No2ProductsDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.createproduct(reqDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/createno2party")
	public ResponseEntity<?> createno2party(@RequestBody No2ProductsPartyDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.createparty(reqDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getAllParty")
	public ResponseEntity<?> ListAllParty(@RequestParam("productType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return no2productService.ListAllParty(productType);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/createno2Bill")
	public ResponseEntity<?> createno2Bill(@RequestBody No2billsDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.createno2Bill(reqDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PostMapping("/createno2BillList")
	public ResponseEntity<?> createno2BillList(@RequestBody List<No2billsDto> reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.createno2BillList(reqDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PutMapping("/billsUpdate")
	public ResponseEntity<?> billsUpdate(@RequestBody No2billsUpdateDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			if (reqDto.getBillType().equals("Rejected")) {
				return no2productService.billReject(reqDto.getId());
			} else {
				return no2productService.billsUpdateDto(reqDto, user.get());
			}
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}

	}

	@GetMapping("/ListBillsPending")
	public ResponseEntity<?> ListBillsPending(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListBillsPending(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListBills")
	public ResponseEntity<?> ListBillsPending(@RequestParam("ProductType") String productType,
			@RequestParam(required = false, name = "fromDate") String fromDate,
			@RequestParam(required = false, name = "toDate") String toDate,
			@RequestParam(required = false, name = "partyId") Long partyId, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListBills(productType, user.get(), fromDate, toDate, partyId, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getAllOrderWithPrice")
	public ResponseEntity<?> ListAllCompletedBills(@RequestParam("ProductType") String productType,
			@RequestParam("billType") String billType, Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListBillsByStatus(productType, billType, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/billsDetail/{id}")
	public ResponseEntity<?> No2BillsbyId(@PathVariable("id") Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.no2billsgetbyid(id, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PutMapping("/updatepartyproductprice")
	public ResponseEntity<?> updatepartyproductprice(@RequestBody No2ProductsPriceDto reqDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.UpdatePartyPrice(reqDto.getProductId(), reqDto.getPartyId(),
					reqDto.getProductType(), reqDto.getLot(), reqDto.getPrice(), user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@PutMapping("/updateProductPriceWithAllParty")
	public ResponseEntity<?> updateProductPriceWithAllParty(@RequestBody No2ProductsPriceDto reqDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.UpdateProductPriceWithAllParty(reqDto, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@Transactional
	@DeleteMapping("/deleteno2party/{partyId}/{productType}")
	public ResponseEntity<?> deleteno2party(@PathVariable Long partyId, @PathVariable String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return no2productService.deleteParty(partyId, productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/deleteno2product/{productid}/{productType}")
	public ResponseEntity<?> deleteno2product(@PathVariable Long productid, @PathVariable String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.deleteProduct(productid, productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListPartyPrice")
	public ResponseEntity<?> createno2party(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListPricebyProductPartyMapping(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListPartybyProductType")
	public ResponseEntity<?> ListPartybyProductType(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return no2productService.ListPartybyProductType(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListBroker")
	public ResponseEntity<?> ListBroker(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return no2productService.ListBroker(user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListStockByProductType")
	public ResponseEntity<?> ListStockByProductType(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListStockByProductType(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListProductbyProductType")
	public ResponseEntity<?> ListProductbyProductType(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		if (user != null)
			return no2productService.ListProductbyProductType(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListRateHistory")
	public ResponseEntity<?> ListRateHistory(@RequestParam("ProductType") String productType,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListRateHistory(productType, user.get());
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/ListRateHistory/v2")
	public ResponseEntity<?> ListRateHistoryv2(@RequestParam("ProductType") String productType,
			@RequestParam(required = false, name = "fromDate") String fromDate,
			@RequestParam(required = false, name = "toDate") String toDate,
			@RequestParam(required = false, name = "partyId") Long partyId, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.ListRateHistoryv2(productType, user.get(), fromDate, toDate, partyId, pageable);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@Transactional
	@PutMapping("/UpdatePrimaryParty")
	public ResponseEntity<?> updatePrimaryParty(@RequestParam(required = false, name = "partyId") Long partyId,
			@RequestParam("productType") String productType, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return no2productService.UpdatePrimaryParty(partyId, productType);
		else
			return ResponseEntity.badRequest().body("User not found");

	}
}
