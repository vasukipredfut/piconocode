package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ChargeProjection;
import com.aria.model.WindmillOtherCharges;

@Repository
public interface WindmillOtherChargesRepository extends JpaRepository<WindmillOtherCharges, Long> {

	
	@Query(value = "SELECT id as id, name AS name, amount AS amount FROM windmillothercharges WHERE type = :type",nativeQuery = true)
		    List<ChargeProjection> findChargesByType(@Param("type") String type);
}
