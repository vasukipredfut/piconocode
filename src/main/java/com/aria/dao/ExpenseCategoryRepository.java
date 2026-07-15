package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ExpenseCategoryListDto;
import com.aria.model.ExpenseCategory;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

	Optional<ExpenseCategory> findByName(String name);

	@Query(value = "SELECT ec.id AS id, ec.name AS name, ec.description AS description, "
			+ "ec.status AS status, ec.created_by AS createdBy, ec.updated_by AS updatedBy, "
			+ "ec.created_date AS createdDate, ec.updated_date AS updatedDate "
			+ "FROM expense_category ec "
			+ "WHERE (:name IS NULL OR LOWER(ec.name) LIKE '%' || LOWER(:name) || '%') "
			+ "AND (:status IS NULL OR ec.status = :status) "
			+ "ORDER BY ec.id DESC", nativeQuery = true)
	Page<ExpenseCategoryListDto> filterExpenseCategory(@Param("name") String name, @Param("status") Boolean status,
			Pageable pageable);

	@Query(value = "SELECT ec.id AS id, ec.name AS name, ec.description AS description, "
			+ "ec.status AS status, ec.created_by AS createdBy, ec.updated_by AS updatedBy, "
			+ "ec.created_date AS createdDate, ec.updated_date AS updatedDate "
			+ "FROM expense_category ec WHERE ec.status = true ORDER BY ec.name ASC LIMIT 100", nativeQuery = true)
	List<ExpenseCategoryListDto> getActiveExpenseCategories();
}
