package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.QcConsumptionSetup;

@Repository
public interface QcConsumptionSetupRepository extends JpaRepository<QcConsumptionSetup, Long>{

	
	@Query(value="select * from qc_consumption_setup q where q.kpiid= :kpiId and q.productid = :productId limit 1 " ,nativeQuery =true)
	Optional<QcConsumptionSetup>  findByKpiName(Long kpiId, Long productId);

}
