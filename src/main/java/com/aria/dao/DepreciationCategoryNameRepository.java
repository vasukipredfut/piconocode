package com.aria.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.DepreciationCategoryName;

@Repository
public interface DepreciationCategoryNameRepository extends JpaRepository<DepreciationCategoryName, Long> {

	@Query(value = "SELECT tn.* FROM depreciation_category_name_master tn where"
			+ " (:depreciationCategoryName is null or (lower(tn.depreciation_name) LIKE ('%' || lower(:depreciationCategoryName) || '%')))",nativeQuery = true)
	Page<DepreciationCategoryName> findAllDepreciationCategoryName(@Param("depreciationCategoryName") String depreciationCategoryName, Pageable pageable);

}
