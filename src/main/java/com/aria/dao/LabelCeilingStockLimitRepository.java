package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.LabelCeilingStockLimit;

public interface LabelCeilingStockLimitRepository extends JpaRepository<LabelCeilingStockLimit, Long> {

	@Query(value = "SELECT * FROM labelceilingstocklimit l where l.branchid = :branchId and l.capacity = :capacity and l.productgroup = :productGroupId and l.status = 'A'", nativeQuery = true)
	LabelCeilingStockLimit findByBranchIdAndCapacityAndProductGroup(Long branchId, String capacity,
			Long productGroupId);

	@Query(value = "SELECT l.* FROM labelceilingstocklimit l  " + "LEFT JOIN labelgroup l2 ON l.productgroup = l2.id  "
			+ "LEFT JOIN labelproductionbranches l3 ON l3.id = l.branchid  "
			+ "WHERE (:capacity IS NULL OR lower(l.capacity) LIKE ('%' || lower(:capacity) || '%'))  "
			+ "AND (:productGroup IS NULL OR lower(l2.\"name\") LIKE ('%' || lower(:productGroup) || '%'))  "
			+ "AND (:branch IS NULL OR lower(l3.\"name\") LIKE ('%' || lower(:branch) || '%'))  "
			+ "AND status = 'A' ORDER BY l.id DESC", nativeQuery = true)
	Page<LabelCeilingStockLimit> getLabelProductFilmList(@Param("capacity") String capacity,
			@Param("productGroup") String productGroup, @Param("branch") String branch, Pageable pageable);

}
