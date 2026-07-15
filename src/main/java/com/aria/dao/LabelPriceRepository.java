package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.LabelPrices;

@Repository
public interface LabelPriceRepository extends JpaRepository<LabelPrices, Long> {

	@Query(value = "SELECT * FROM labelprices l WHERE l.labellocationid IN (:locationIds) AND l.labelproductgroupid =:productGroupId AND l.status = 'A'", nativeQuery = true)
	List<LabelPrices> findByLocationIdInAndProductGroupId(@Param("locationIds") List<Long> locationIds, @Param("productGroupId") Long productGroupId);

	@Query(value = "SELECT * FROM labelprices l WHERE l.labelproductid  = :productId AND l.labellocationid = :locationId AND l.labelproductgroupid = :productGroupId AND l.status = 'A'", nativeQuery = true)
	LabelPrices findByProductIdAndLocationIdAndProductGroupId(Long productId, Long locationId, Long productGroupId);

}
