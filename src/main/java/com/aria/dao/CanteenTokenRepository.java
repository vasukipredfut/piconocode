package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CanteenToken;

@Repository
public interface CanteenTokenRepository extends JpaRepository<CanteenToken, Long> {

	@Query(value = "SELECT ct FROM CanteenToken ct WHERE (:userId is null or ct.user.id = :userId) "
			+ "AND (:tokenFor is null or lower(ct.tokenFor) = lower(:tokenFor)) "
			+ "AND (:shiftId is null or ct.shift.id = :shiftId)"
			+ "AND (cast(:dateFrom as timestamp) is null or (ct.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (ct.date <= :dateTo))")
	List<CanteenToken> canteenTokenfilterList(@Param("userId") Long userId,
	        @Param("tokenFor") String tokenFor,@Param("shiftId") Long shiftId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);
}