package com.aria.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.WindmillOtherChargesProjection;
import com.aria.model.WindmillOtherChargesTrn;

@Repository
public interface WindmillOtherChargesTrnRepository extends JpaRepository<WindmillOtherChargesTrn, Long> {

	@Query(value = "select wo.windmillid as windmillid,wo.amount as amount,wo.windmillotherchargesid as otherchargesid,w.name as name,wo.trndate as trndate from windmill_othercharges_trn wo \r\n"
			+ "			left join windmillothercharges w on w.id=wo.windmillotherchargesid where wo.windmillid=:windmillid\r\n"
			+ "			and wo.trndate=:trndate", nativeQuery = true)
	List<WindmillOtherChargesProjection> findByChargesUsingWindmillIdandTrndate(@Param("trndate")LocalDate trndate,@Param("windmillid")Long windmillid);

}
