package com.aria.service;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.ExpenseCategoryRepository;
import com.aria.dto.ExpenseCategoryDto;
import com.aria.dto.ExpenseCategoryListDto;
import com.aria.model.ExpenseCategory;
import com.aria.model.User;

@Service
public class ExpenseCategoryService {

	@Autowired
	private ExpenseCategoryRepository expenseCategoryRepository;

	/**
	 * Save new expense category
	 */
	@Transactional
	public ResponseEntity<?> saveExpenseCategory(ExpenseCategoryDto expenseCategoryDto, User user) {
		try {
			// Check if category already exists
			Optional<ExpenseCategory> existingCategory = expenseCategoryRepository
					.findByName(expenseCategoryDto.getName());
			if (existingCategory.isPresent()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Expense Category with name '" + expenseCategoryDto.getName() + "' already exists");
			}

			ExpenseCategory expenseCategory = new ExpenseCategory(expenseCategoryDto);
			expenseCategory.setCreatedBy(user.getId());
			expenseCategory.setCreatedDate(LocalDateTime.now());
			expenseCategory.setUpdatedBy(user.getId());
			expenseCategory.setUpdatedDate(LocalDateTime.now());

			ExpenseCategory savedCategory = expenseCategoryRepository.save(expenseCategory);
			return ResponseEntity.status(HttpStatus.CREATED).body(new ExpenseCategoryDto(savedCategory));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error saving expense category: " + e.getMessage());
		}
	}

	/**
	 * Update expense category
	 */
	@Transactional
	public ResponseEntity<?> updateExpenseCategory(Long id, ExpenseCategoryDto expenseCategoryDto, User user) {
		try {
			Optional<ExpenseCategory> optionalCategory = expenseCategoryRepository.findById(id);
			if (!optionalCategory.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense Category not found");
			}

			ExpenseCategory expenseCategory = optionalCategory.get();

			// Check if name is being changed and if new name already exists
			if (!expenseCategory.getName().equals(expenseCategoryDto.getName())) {
				Optional<ExpenseCategory> existingCategory = expenseCategoryRepository
						.findByName(expenseCategoryDto.getName());
				if (existingCategory.isPresent()) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body("Expense Category with name '" + expenseCategoryDto.getName()
									+ "' already exists");
				}
			}

			expenseCategory.setName(expenseCategoryDto.getName());
			expenseCategory.setDescription(expenseCategoryDto.getDescription());
			expenseCategory.setStatus(expenseCategoryDto.getStatus());
			expenseCategory.setUpdatedBy(user.getId());
			expenseCategory.setUpdatedDate(LocalDateTime.now());

			ExpenseCategory updatedCategory = expenseCategoryRepository.save(expenseCategory);
			return ResponseEntity.ok(new ExpenseCategoryDto(updatedCategory));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating expense category: " + e.getMessage());
		}
	}

	/**
	 * Get expense category by ID
	 */
	public ResponseEntity<?> getExpenseCategoryById(Long id) {
		try {
			Optional<ExpenseCategory> optionalCategory = expenseCategoryRepository.findById(id);
			if (!optionalCategory.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense Category not found");
			}

			ExpenseCategory expenseCategory = optionalCategory.get();
			return ResponseEntity.ok(new ExpenseCategoryDto(expenseCategory));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching expense category: " + e.getMessage());
		}
	}

	/**
	 * Delete expense category
	 */
	@Transactional
	public ResponseEntity<?> deleteExpenseCategory(Long id) {
		try {
			Optional<ExpenseCategory> optionalCategory = expenseCategoryRepository.findById(id);
			if (!optionalCategory.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense Category not found");
			}

			expenseCategoryRepository.deleteById(id);
			return ResponseEntity.ok("Expense Category deleted successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error deleting expense category: " + e.getMessage());
		}
	}

	/**
	 * Get all expense categories with filter and pagination
	 */
	public ResponseEntity<?> filterExpenseCategory(String name, Boolean status, Pageable pageable) {
		try {
			Page<ExpenseCategoryListDto> result = expenseCategoryRepository.filterExpenseCategory(name, status,
					pageable);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error filtering expense categories: " + e.getMessage());
		}
	}

	/**
	 * Get active expense categories for dropdown
	 */
	public ResponseEntity<?> getActiveExpenseCategories() {
		try {
			return ResponseEntity.ok(expenseCategoryRepository.getActiveExpenseCategories());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching active expense categories: " + e.getMessage());
		}
	}
}
