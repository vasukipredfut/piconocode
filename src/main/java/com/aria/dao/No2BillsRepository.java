package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2bills;

@Repository
public interface No2BillsRepository extends JpaRepository<No2bills, Long> {

	@Query(value = " SELECT  *  FROM no2_bills b WHERE b.product_type= :productType " +
			" AND b.bill_status <> 'Completed' ", nativeQuery = true)
	List<No2bills> findAllPending(@Param("productType") String productType);

	@Query(value = " SELECT  *  FROM no2_bills b WHERE b.product_type= :productType "
			+ " AND (cast(:fromDate as date) is null or b.transaction_date >= :fromDate) "
			+ " AND (cast(:toDate as date) is null or b.transaction_date <= :toDate) "
			+ " AND (:partyId is null or b.party_id = :partyId) "
			+ " ORDER BY b.id DESC ", nativeQuery = true)
	Page<No2bills> findAllBills(@Param("productType") String productType, @Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, @Param("partyId") Long partyId, Pageable pageable);

	@Query(value = "SELECT  *  FROM no2_bills b WHERE b.product_type= :productType AND b.bill_status = :billType ", nativeQuery = true)
	Page<No2bills> findAllByStatus(@Param("productType") String productType, @Param("billType") String billType,
			Pageable pageable);


}
