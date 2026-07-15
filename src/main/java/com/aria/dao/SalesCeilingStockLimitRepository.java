package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesCeilingStockLimit;

@Repository
public interface SalesCeilingStockLimitRepository extends JpaRepository<SalesCeilingStockLimit, Long> {

	@Query(value = "SELECT * FROM salesceilingstocklimit s where s.branchid = :branchId and s.capacity = :capacity and s.productgroup = :productGroupId and s.status = 'A'", nativeQuery = true)
	SalesCeilingStockLimit findByBranchIdAndCapacityAndProductGroup(Long branchId, String capacity,
			Long productGroupId);

	@Query(value = "SELECT s.* FROM salesceilingstocklimit s LEFT JOIN salesproductgroup s2 ON s.productgroup = s2.id LEFT JOIN salesproductionbranches s3 ON s3.id = s.branchid WHERE (:capacity IS NULL OR lower(s.capacity) LIKE ('%' || lower(:capacity) || '%')) AND (:productGroup IS NULL OR lower(s2.\"name\") LIKE ('%' || lower(:productGroup) || '%')) AND (:branch IS NULL OR lower(s3.\"name\") LIKE ('%' || lower(:branch) || '%')) AND status = 'A' ORDER BY s.id DESC ", nativeQuery = true)
	Page<SalesCeilingStockLimit> getSalesProductFilmList(@Param("capacity") String capacity,
			@Param("productGroup") String productGroup, @Param("branch") String branch, Pageable pageable);

}
