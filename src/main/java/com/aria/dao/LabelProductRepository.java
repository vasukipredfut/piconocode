package com.aria.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.SalesProductLabel;

public interface LabelProductRepository extends JpaRepository<SalesProductLabel, Long> {

	@Query(value = " SELECT s.* FROM labelproduct s "
			+ " LEFT JOIN labelgroup s2 ON s2.id = s.productgroupid "
			+ " LEFT JOIN labelproductionbranches s3 ON s3.id  = s.branchid "
			+ " LEFT JOIN  labelproductname s4 ON s4.id = s.productnameid "
			+ " WHERE (:productId IS NULL OR CONCAT(s.refid , '') LIKE ( :productId || '%')) "
			+ " AND (:productName IS NULL OR lower(s.productname) LIKE ( lower(:productName) || '%')) "
			+ " AND (:productGroup IS NULL OR lower(s2.name) LIKE ( lower(:productGroup) || '%')) "
			+ " AND (:branch IS NULL OR lower(s3.name) LIKE ( lower(:branch) || '%')) "
			+ " AND (:finalProductName IS NULL OR lower(s4.productname) LIKE (lower(:finalProductName) || '%')) "
			+ " AND ( cast(:fromDate  as date) is null OR ( s.createddate >=:fromDate ) )"
			+ " AND (cast(:toDate  as date) is null OR ( s.createddate <=:toDate ) ) "
			+ " ORDER BY s.id DESC ", nativeQuery = true)
	Page<SalesProductLabel> getLabelProduct(@Param("productId") Long productId,
											@Param("productName") String productName, @Param("productGroup") String productGroup,
											@Param("branch") String branch, @Param("finalProductName") String finalProductName,
											@Param("fromDate") Date fromDate, @Param("toDate") Date toDate , Pageable pageable);

	Optional<SalesProductLabel> findByProductIdAndBranchId(Long productId, Long branchId);

}
