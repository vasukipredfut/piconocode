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

import com.aria.dao.UserRepository;
import com.aria.dto.FoodItemDto;
import com.aria.model.User;
import com.aria.service.FoodItemService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FoodItemController {

	@Autowired
	FoodItemService foodItemService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveFoodItem")
	public ResponseEntity<?> saveFoodItem(@RequestBody FoodItemDto foodItemDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return foodItemService.saveFoodItem(foodItemDto, user.get());
	}
	
	@PutMapping("/updateFoodItem")
	public ResponseEntity<?> updateFoodItem(@RequestBody FoodItemDto foodItemDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return foodItemService.updateFoodItem(foodItemDto, user.get());
	}

	@GetMapping("/getFoodItemFilterList")
	public ResponseEntity<?> getFoodItemListByFoodNameAndStartDateAndEndDateAndOrganisationId(
			@RequestParam(value = "foodName", required = false) String foodName,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Authentication authentication, Pageable pageable) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return foodItemService.FoodItemfilterList(foodName, dateFrom,dateTo, pageable);
		} else {
			return new ResponseEntity<List<FoodItemDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}
	
	@GetMapping("/getFoodItemList")
	public ResponseEntity<?> getFoodItem(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return foodItemService.listAllfoodItem();
		} else {
			return new ResponseEntity<List<FoodItemDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}
	
	@GetMapping("/getFoodItemDetails/{id}")
	public ResponseEntity<?> getFoodItem(@PathVariable String id, Authentication authentication) {
		return foodItemService.getFoodItem(id);
	}
	
	@DeleteMapping("/deleteFoodItemDetails/{id}")
	public ResponseEntity<?> deleteFoodItem(@PathVariable String id, Authentication authentication) {
		return foodItemService.deleteFoodItem(id);
	}
	
}
