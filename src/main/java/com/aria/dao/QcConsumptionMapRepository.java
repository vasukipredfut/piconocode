package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.QcConsumptionMap;

@Repository
public interface QcConsumptionMapRepository extends JpaRepository<QcConsumptionMap, Long>{
	@Query(value = "SELECT * FROM qc_consumption_map m LEFT JOIN qc_consumption_setup s ON m.configid = s.id "
	 		+ "    WHERE s.kpiid = :kpiId AND  s.productid = :productId ",nativeQuery = true)
	List<QcConsumptionMap> findConsumableProduct( @Param("kpiId") Long kpiId,@Param("productId") Long productId);
}
