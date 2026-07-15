package com.aria.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.ExpenseCategoryDto;
import com.aria.model.User;
import com.aria.service.ExpenseCategoryService;

@RestController
@RequestMapping("/api/expense-category")
public class ExpenseCategoryController {

	@Autowired
	private ExpenseCategoryService expenseCategoryService;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Create new expense category
	 * 
	 * @param expenseCategoryDto
	 * @param authentication
	 * @return
	 */
	@PostMapping("/save")
	public ResponseEntity<?> saveExpenseCategory(@RequestBody ExpenseCategoryDto expenseCategoryDto,
			Authentication authentication) {
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			User user = userRepository.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			return expenseCategoryService.saveExpenseCategory(expenseCategoryDto, user);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error: " + e.getMessage());
		}
	}

	/**
	 * Update expense category
	 * 
	 * @param id
	 * @param expenseCategoryDto
	 * @param authentication
	 * @return
	 */
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateExpenseCategory(@PathVariable Long id,
			@RequestBody ExpenseCategoryDto expenseCategoryDto, Authentication authentication) {
		try {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			User user = userRepository.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			return expenseCategoryService.updateExpenseCategory(id, expenseCategoryDto, user);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error: " + e.getMessage());
		}
	}

	/**
	 * Get expense category by ID
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/get/{id}")
	public ResponseEntity<?> getExpenseCategoryById(@PathVariable Long id) {
		return expenseCategoryService.getExpenseCategoryById(id);
	}

	/**
	 * Delete expense category
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteExpenseCategory(@PathVariable Long id) {
		return expenseCategoryService.deleteExpenseCategory(id);
	}

	/**
	 * Filter expense categories with pagination
	 * 
	 * @param name
	 * @param status
	 * @param pageable
	 * @return
	 */
	@GetMapping("/filter-list")
	public ResponseEntity<?> filterExpenseCategory(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "status", required = false) Boolean status,
			Pageable pageable) {
		return expenseCategoryService.filterExpenseCategory(name, status, pageable);
	}

	/**
	 * Get active expense categories for dropdown
	 * 
	 * @return
	 */
	@GetMapping("/active-list")
	public ResponseEntity<?> getActiveExpenseCategories() {
		return expenseCategoryService.getActiveExpenseCategories();
	}
}
