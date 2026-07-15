package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2Ratehistory;
import com.aria.model.RatehistoryGrouping;

@Repository
public interface No2RateHistoryRepository extends JpaRepository<No2Ratehistory, Long> {

	@Query(value = "SELECT pt.party_name as partyName, rt.partyid as partyId, pd.name as productName, rt.productid as productId, cast(rt.created_date as timestamp)  as createdDate, rt.lot as lot, rt.increasedecrease as incDec, rt.amount as amount, rt.originalamount as oldAmount FROM no2ratehistory rt, no2productsparty pt, no2products pd WHERE pd.refid=rt.productid  "
			+ "AND pd.active=1 AND  pt.partyid = rt.partyid AND rt.productid IN (:productList) "
			+ "AND (cast(:fromDate as date) is null or rt.created_date >= :fromDate) "
			+ "AND (cast(:toDate as date)  is null or rt.created_date <= :toDate) "
			+ "AND (:partyId is null or rt.partyid = :partyId) "
			+ "and pt.active=1  ORDER by rt.id , rt.created_date  desc", nativeQuery = true)
	Page<RatehistoryGrouping> findRatehistoryGroupings(@Param("productList") List<Long> productList,
			@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("partyId") Long partyId,
			Pageable pageable);

	@Query(value = "SELECT pt.party_name as partyName, rt.partyid as partyId, pd.name as productName, rt.productid as productId, cast(rt.created_date as timestamp)  as createdDate, rt.lot as lot, rt.increasedecrease as incDec, rt.amount as amount, rt.originalamount as oldAmount FROM no2ratehistory rt, no2productsparty pt, no2products pd WHERE pd.refid=rt.productid  "
			+ "AND pd.active=1 AND  pt.partyid = rt.partyid AND rt.productid IN (:productList) "
			+ "AND (cast(:fromDate as date) is null or rt.created_date >= :fromDate) "
			+ "AND (cast(:toDate as date)  is null or rt.created_date <= :toDate) "
			+ "AND (:partyId is null or rt.partyid = :partyId) "
			+ "and pt.active=1  ORDER by rt.id , rt.created_date  desc ", nativeQuery = true)
	List<RatehistoryGrouping> findRatehistoryGroupings(@Param("productList") List<Long> productList,
			@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("partyId") Long partyId);

}
