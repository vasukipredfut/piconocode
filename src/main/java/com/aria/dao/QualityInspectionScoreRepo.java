package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.QualityInspectionScoreList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.QualityInspectionScore;

public interface QualityInspectionScoreRepo extends JpaRepository<QualityInspectionScore, Long> {

    @Query(value = "SELECT p.* FROM purchase_quality_inspection_score p, bill_inward i WHERE "
            + " (:po_no is null or (p.bill_ref_id = i.id AND i.purchase_order_id = :po_no )) AND "
            + "( (:billNumber) is null or p.bill_number IN (:billNumber)) AND "
            + " (:vendorId is null or p.vendor_ref_id =:vendorId) AND "
            + "  (cast(:fromDt as date) is null or p.bill_date > :fromDt) and (cast(:toDt as date) is null or p.bill_date < :toDt)", nativeQuery = true)
    Page<QualityInspectionScore> getQualityInspection(@Param("billNumber") List<String> billNumber,
                                                      @Param("vendorId") Long vendorId, @Param("fromDt") Date fromDt, @Param("toDt") Date toDt,
                                                      @Param("po_no") Long po_no, Pageable page);

    @Query(value = "select * from purchase_quality_inspection_score p where p.item_id =:itemId and p.bill_ref_id = :billId and p.po_id = :poId and p.po_number = :poNumber LIMIT 1", nativeQuery = true)
    Optional<QualityInspectionScore> findByFilter(@Param("itemId") Long itemId, @Param("billId") Long billRefId,
                                                  @Param("poId") Long poId, @Param("poNumber") String poNumber);


    @Query(value = " select  pq.company as company,  pq.vendor_ref_id as vendorId , max(l.name) as vendorName , pq.branch as branchId, "
            + "   string_agg( distinct CAST(pq.bill_ref_id AS varchar ) ,'#') as billRefId, max(pq.inward_date) as inwardDate , "
            + "  ROUND ( SUM(pq.qc_score) / count(pq.purchase_quality_inspection_score_id)  )  as qcScore , "
            + "  ROUND ( SUM(pq.delivery_score) / count(pq.purchase_quality_inspection_score_id) )  as deliveryScore , "
            + "  count(pq.purchase_quality_inspection_score_id) as billCount , max(po.deliveryduedate) as deliveryDueDate "
            + "  from purchase_quality_inspection_score pq left join  bill_inward bi on pq.bill_ref_id = bi.id and bi.vendor_id = pq.vendor_ref_id "
            + "  left join ledger l on l.id = pq.vendor_ref_id left join purchase_order po on po.id = pq.po_id  "
            + "  where pq.vendor_ref_id =:vendorId and pq.po_id =:poId and pq.branch =:branch AND pq.company=:company "
            + "  AND (cast(:fromDate as date) is null or (pq.inward_date >= :fromDate)) "
            + "  AND (cast(:toDate as date) is null or (pq.inward_date <= :toDate)) "
            + "  group by pq.vendor_ref_id , pq.po_id , pq.company , pq.branch LIMIT 1 ", nativeQuery = true)
    QualityInspectionScoreList findBy(@Param("vendorId") Long vendorId,
                                      @Param("poId") Long poId,
                                      @Param("branch") Long branch,
                                      @Param("company") Long company,
                                      @Param("fromDate") Date fromDate,
                                      @Param("toDate") Date toDate);


    @Query(value = " select  pq.company as company,  pq.vendor_ref_id as vendorId , max(l.name) as vendorName , pq.branch as branchId, " +
            "    string_agg( distinct CAST(pq.bill_ref_id AS varchar ) ,'#') as billRefId, string_agg(distinct bi.billno ,'#') as billNo, " +
            "    max(pq.inward_date) as inwardDate , ROUND ( SUM(pq.qc_score) / count(pq.purchase_quality_inspection_score_id)  )  as qcScore , " +
            "    ROUND ( SUM(pq.delivery_score) / count(pq.purchase_quality_inspection_score_id) )  as deliveryScore , pq.po_id as poId , " +
            "    count(pq.purchase_quality_inspection_score_id) as billCount , max(po.deliveryduedate) as deliveryDueDate " +
            "    from purchase_quality_inspection_score pq left join  bill_inward bi on pq.bill_ref_id = bi.id and bi.vendor_id = pq.vendor_ref_id " +
            "    left join ledger l on l.id = pq.vendor_ref_id left join purchase_order po on po.id = pq.po_id  " +
            "    where pq.company=:company AND pq.vendor_ref_id =:vendorId " +
            "    AND (cast(:fromDate as date) is null or (pq.inward_date >= :fromDate)) " +
            "    AND (cast(:toDate as date) is null or (pq.inward_date <= :toDate)) " +
            "   group by pq.vendor_ref_id , pq.po_id , pq.company , pq.branch  ", nativeQuery = true)
    List<QualityInspectionScoreList> findBy(@Param("company") Long company,
                                            @Param("vendorId") Long vendorId,
                                            @Param("fromDate") Date fromDate,
                                            @Param("toDate") Date toDate);


    @Query(value = " SELECT  pq.company as company,  pq.branch as branchId , pq.vendor_ref_id as vendorId "
            + "  FROM purchase_quality_inspection_score pq where pq.company <> 0 and pq.branch <> 0 and pq.vendor_ref_id <> 0 "
            + "  AND (cast(:fromDate as date) is null or (pq.inward_date >= :fromDate)) "
            + "  AND (cast(:toDate as date) is null or (pq.inward_date <= :toDate)) "
            + "  group by pq.vendor_ref_id , pq.po_id , pq.company , pq.branch  ", nativeQuery = true)
    List<QualityInspectionScoreList> findBy(@Param("fromDate") Date fromDate,
                                            @Param("toDate") Date toDate);



    @Query(value = "select * from purchase_quality_inspection_score p where p.item_id =:itemId and p.bill_ref_id = :billId and p.po_id = :poId and p.po_number = :poNumber ", nativeQuery = true)
    List<QualityInspectionScore> findList(@Param("itemId") Long itemId, @Param("billId") Long billRefId,
                                                  @Param("poId") Long poId, @Param("poNumber") String poNumber);

}
