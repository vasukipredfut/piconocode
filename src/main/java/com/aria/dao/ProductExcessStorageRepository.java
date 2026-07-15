package com.aria.dao;


import com.aria.dto.ExcessOrShortageOrReturn;
import com.aria.dto.ProductExcessStorageList;
import com.aria.model.ProductExcessStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;



@Repository
public interface ProductExcessStorageRepository extends JpaRepository<ProductExcessStorage, Long> {


	@Query(value = "select * from product_excess_storage ORDER BY id DESC LIMIT 1",nativeQuery = true)
	Optional<ProductExcessStorage> find();

	@Query(value = "SELECT pes.id as id, ven.name as vendorName , ven.id as vendorId , pes.bill_no as billNo , pes.bill_inward_date as billInwardDate, pm.name  as productName, esp.type  as type, pes.status as status "
			+ " FROM product_excess_storage pes LEFT JOIN excess_shortage_products esp ON esp.excess_storage_id = pes.id LEFT JOIN products_master pm  ON pm .id = esp.product_master_id LEFT JOIN  ledger ven ON ven.id = pes.vendor_id  "
			+ " WHERE (:vendorName is null or (lower(ven.name) LIKE ('%' || lower(:vendorName) || '%')) ) "
			+ " AND (:billNo is null or (lower(pes.bill_no) LIKE ('%' || lower(:billNo) || '%')) ) "
			+ " AND (:type is null or (lower(esp.type) LIKE ('%' || lower(:type) || '%')) ) "
			+ " AND (:productName is null or (lower(pm.name) LIKE ('%' || lower(:productName) || '%')) ) "
			+ " AND (cast(:dateFrom as date) is null or (pes.bill_inward_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as date) is null or (pes.bill_inward_date <= :dateTo))"
			+ " AND (:branchId is null or (pes.branch_id =:branchId)) "
			+ " AND (:status is null or (lower(pes.status) LIKE ('%' || lower(:status) || '%')))GROUP BY pes.id, ven.name, ven.id, pes.bill_no, pes.bill_inward_date, pm.name, esp.type, pes.status order by pes.id desc ",nativeQuery = true)
	Page<ProductExcessStorageList> productExcessStorageFilterList(@Param("branchId") Long branchId, @Param("vendorName") String vendorName, @Param("billNo") String billNo,
																  @Param("type") String type, @Param("productName") String productName, @Param("dateFrom") Date dateFrom,
																  @Param("dateTo") Date dateTo, @Param("status") String status, Pageable pageable);


	@Query(value = "SELECT pes.id as id, pes.vendor_id as vendorId , pes.bill_no as billNo , esp.product_master_id as itemId , "
			+ " pes.bill_Date as billDate , esp.type as type , esp.lot as lot , SUM(esb.excess_or_shortage) as qty "
			+ " FROM product_excess_storage pes "
			+ " LEFT JOIN excess_shortage_products esp ON  esp.excess_storage_id = pes.id "
			+ " LEFT JOIN excess_stored_bin esb ON esb.excess_product_id =esp.id "
			+ " WHERE esp.product_master_id IS NOT NULL "
			+ " AND (:id is null or pes.id=:id) "
			+ " ANd (:vendorId is null or pes.vendor_id=:vendorId) "
			+ " AND (:billNo is null or pes.bill_no =:billNo) "
			+ " AND (esp.product_master_id is NOT NULL) "
			+ " AND (esp.type is NOT NULL) "
			+ " AND (esb.excess_or_shortage is NOT NULL) "
			+" GROUP BY  pes.id , pes.vendor_id  , pes.bill_No  , esp.product_master_id  , esp.type, esp.lot, pes.bill_Date ",nativeQuery = true)
	List<ExcessOrShortageOrReturn> findBy(@Param("id") Long id, @Param("vendorId") Long vendorId,
                                          @Param("billNo") String billNo);


	@Query(value = "select * from product_excess_storage pes "
			+" WHERE (:vendorId is null or pes.vendor_id=:vendorId) "
			+ "AND (:billNo is null or pes.bill_no =:billNo) "
			+ "AND (cast(:billDate as date) is null or pes.bill_date =:billDate) "
			+ "AND (cast(:inwardDate as date) is null or pes.bill_inward_date =:inwardDate) "
			+" LIMIT 1",nativeQuery = true)
	Optional<ProductExcessStorage> findBy(@Param("vendorId") Long vendorId,
										  @Param("billNo") String billNo,
										  @Param("billDate") Date billDate,
										  @Param("inwardDate") Date inwardDate);


	@Query(value = "select * from product_excess_storage pes "
			+ " WHERE (:vendorId is null or pes.vendor_id=:vendorId) "
			+ " AND (:billNo is null or pes.bill_no =:billNo) LIMIT 1",nativeQuery = true)
	Optional<ProductExcessStorage> findBy(@Param("vendorId") Long vendorId, @Param("billNo") String billNo);

}







	
