package com.aria.dao;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.SupplierEvaluationAttributes;

@Repository
public interface SupplierEvaluationAttributesRepository extends JpaRepository<SupplierEvaluationAttributes, Long> {

	
	@Query(value = "select * from supplierevaluationattributes where status='A'", nativeQuery = true)
	List<SupplierEvaluationAttributes> findListSupplierEvaluationAttributes();
	
	
}
