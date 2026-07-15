package com.aria.dao;


import com.aria.dto.ExcessOrShortageOrReturn;
import com.aria.dto.ProductExcessStorageList;
import com.aria.model.PurchaseReturn;
import com.aria.model.PurchaseReturnProducts;
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
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {


    @Query(value = "select * from purchase_return ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<PurchaseReturn> find();

    @Query(value = "SELECT pr.id as id, ven.name as vendorName , ven.id as vendorId , pr.bill_no as billNo ,pr.status as status, pr.bill_inward_date as billInwardDate, pm.name  as productName, prp.reason  as type   " +
            " FROM purchase_return pr LEFT JOIN purchase_return_products prp ON prp.purchase_return_id = pr.id LEFT JOIN products_master pm  ON pm .id = prp.product_master_id LEFT JOIN  ledger ven ON ven.id = pr.vendor_id  " +
            " WHERE (:vendorName is null or (lower(ven.name) LIKE ('%' || lower(:vendorName) || '%')) ) "
            + " AND (:billNo is null or (lower(pr.bill_no) LIKE ('%' || lower(:billNo) || '%')) ) "
            + " AND (:type is null or (lower(prp.reason) LIKE ('%' || lower(:type) || '%')) ) "
            + " AND (:productName is null or (lower(pm.name) LIKE ('%' || lower(:productName) || '%')) ) "
            + " AND (cast(:dateFrom as date) is null or (pr.bill_inward_date >= :dateFrom)) "
            + " AND (cast(:dateTo as date) is null or (pr.bill_inward_date <= :dateTo))"
            + " AND (:branchId is null or (pr.branch_id =:branchId)) "
            + " AND (:status is null or (lower(pr.status) LIKE ('%' || lower(:status) || '%')))"
            + " GROUP BY pr.id, ven.name, ven.id, pr.bill_no, pr.status, pr.bill_inward_date, pm.name, prp.reason order by pr.id desc ", nativeQuery = true)
    Page<ProductExcessStorageList> purchaseReturnFilterList(@Param("branchId") Long branchId , @Param("vendorName") String vendorName, @Param("billNo") String billNo,
                                                            @Param("type") String type, @Param("productName") String productName, @Param("dateFrom") Date dateFrom,
                                                            @Param("dateTo") Date dateTo, @Param("status") String status, Pageable pageable);


    @Query(value = "SELECT pr.id as id, pr.vendor_id as vendorId , pr.bill_no as billNo , prp.product_master_id as itemId , pr.bill_Date as billDate , "
            + " prp.lot as lot , SUM(prsb.excess_or_shortage) as qty FROM purchase_return pr "
            + " LEFT JOIN purchase_return_products prp ON  prp.purchase_return_id = pr.id LEFT JOIN purchase_return_stored_bin prsb ON prsb.return_product_Id =prp.id "
            + " WHERE (:vendorId is null or pr.vendor_id=:vendorId) "
            + "AND (:billNo is null or pr.bill_no =:billNo) "
            + " GROUP BY  pr.id , pr.vendor_id  , pr.bill_No , prp.product_master_id , prp.lot , pr.bill_Date ", nativeQuery = true)
    List<ExcessOrShortageOrReturn> findBy(@Param("vendorId") Long vendorId, @Param("billNo") String billNo);

    @Query(value = "SELECT * FROM purchase_return r WHERE r.id =:id and r.product_id=:productId", nativeQuery = true)
    List<PurchaseReturnProducts> findByProductId(Long id, Long productId);


    @Query(value = " SELECT * from purchase_return pes "
            + " WHERE (:vendorId is null or pes.vendor_id=:vendorId) "
            + "AND (:billNo is null or pes.bill_no =:billNo) "
            + "AND (cast(:billDate as date)  is null or pes.bill_date =:billDate) "
            + "AND (cast(:inwardDate as date) is null or pes.bill_inward_date =:inwardDate) "
            + " LIMIT 1", nativeQuery = true)
    Optional<PurchaseReturn> findBy(@Param("vendorId") Long vendorId,
                                    @Param("billNo") String billNo,
                                    @Param("billDate") Date billDate,
                                    @Param("inwardDate") Date inwardDate);


    @Query(value = " SELECT * from purchase_return pes WHERE (:vendorId is null or pes.vendor_id=:vendorId) "
            + " AND (:billNo is null or pes.bill_no =:billNo) "
            + " LIMIT 1 ", nativeQuery = true)
    Optional<PurchaseReturn> findByReturn(@Param("vendorId") Long vendorId,
                                    @Param("billNo") String billNo);


}



