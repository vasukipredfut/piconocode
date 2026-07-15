package com.aria.dao;

import com.aria.dto.EvaluationBillList;
import com.aria.dto.EvaluationReportList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SupplierEvaluationReport;
import com.aria.model.SupplierEvaluationReportList;
import com.aria.model.SupplierEvaluationReportVendorScore;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierEvaluationReportRepository extends JpaRepository<SupplierEvaluationReport, Long> {

    @Query(value = "SELECT s.*, l.name as vendorName, COALESCE(po.po_no, NULL) as purchase_order_number "
            + "FROM supplierevaluationreport s " + "LEFT JOIN ledger l ON l.id = s.ledgerid "
            + "LEFT JOIN purchase_order po ON po.id = s.poid " + "WHERE (:poNumber is null or s.poid = :poNumber) "
            + "AND (:vendorId is null or s.ledgerid = :vendorId) " + "AND (:period is null or s.period = :period) "
            + "AND (:fromyear is null or s.year >= :fromyear) "
            + "AND (:toyear is null or s.year <= :toyear)", nativeQuery = true)
    Page<SupplierEvaluationReportList> findbySupplierEvaluation(@Param("poNumber") Long poNumber,
                                                                @Param("vendorId") Long vendorId, @Param("fromyear") Integer fromyear, @Param("toyear") Integer toyear,
                                                                @Param("period") String period, Pageable page);

    @Query(value = "SELECT s.year, s.ledgerid, avg(loyaltyscore) as loyaltyscore, avg(deliveryscore) as deliveryscore, avg(qualityscore) as qualityscore, l.name as vendor_name "
            + "FROM supplierevaluationreport s " + "LEFT JOIN ledger l ON l.id = s.ledgerid  WHERE "
            + " s.year= (select max(year) from supplierevaluationreport where ledgerid = :vendorId) "
            + "AND (:vendorId is null or s.ledgerid = :vendorId) "
            + " GROUP BY s.ledgerid, s.year, l.name  ", nativeQuery = true)
    SupplierEvaluationReportVendorScore findVendorScore(@Param("vendorId") Long vendorId);



    @Query(value = "SELECT * FROM supplierevaluationreport s WHERE  (:vendorId is null or s.ledgerid =:vendorId) "
            + "  AND  s.year =:year AND (:period is null or s.period =:period) order by id DESC LIMIT 1 ", nativeQuery = true)
    Optional<SupplierEvaluationReport> findBy(@Param("vendorId") Long vendorId,
                                              @Param("year") Integer year,
                                              @Param("period") String period);





    @Query(value = "SELECT * FROM supplierevaluationreport s WHERE  (:vendorId is null or s.ledgerid =:vendorId) "
            + " AND (:poId is null or s.poid =:poId) AND  s.year =:year AND (:branch is null or s.branch =:branch) "
            + " AND (:period is null or s.period =:period) order by id DESC LIMIT 1 ", nativeQuery = true)
    Optional<SupplierEvaluationReport> findBy(@Param("vendorId") Long vendorId,
                                              @Param("poId") Long poId,
                                              @Param("branch") Long branch,
                                              @Param("year") Integer year,
                                              @Param("period") String period);

    @Query(value = "SELECT * FROM supplierevaluationreport s WHERE  (:vendorId is null or s.ledgerid =:vendorId) AND (:branch is null or s.branch =:branch) "
            + "  AND  s.year =:year AND (:period is null or s.period =:period) order by id DESC LIMIT 1 ", nativeQuery = true)
    Optional<SupplierEvaluationReport> findByTrn(@Param("vendorId") Long vendorId, @Param("branch") Long branch, @Param("year") Integer year, @Param("period") String period);


    @Query(value = " select s.company as firmId , s.ledgerid as partyId , max(l.name) as partyName , s.year as year , max(s.trnno ) as trnNo , "
            + " s.period as  period,  ( SUM (s.qualityscore) / COUNT(s.id) )  as qualityScore , ( SUM (s.loyaltyscore) / COUNT(s.id) )  as loyaltyScore,  "
            + " ( SUM (s.deliveryscore) / COUNT(s.id) ) as deliveryScore , max(sp.status) as status from supplierevaluationreport  s left join ledger l on l.id = s.ledgerid  "
            + " left join supeval sp on sp.trnno = s.trnno  "
            + " WHERE (:vendorId is null or s.ledgerid = :vendorId) "
            + " AND (:firmId is null or s.company = :firmId) "
            + " AND (:partyName is null or lower(l.name) like ('%' || lower(:partyName) || '%'))  "
            + " AND (:period is null or s.period = :period) "
            + " AND (:fromyear is null or s.year >= :fromyear) "
            + " AND (:toyear is null or s.year <= :toyear) "
            + " AND (:status is null or lower(sp.status) like ('%' || lower(:status) || '%'))  "
            + " group  by s.ledgerid, s.year , s.period , s.company  "
            + " ORDER BY max(sp.entry) ASC ", nativeQuery = true)
    Page<EvaluationReportList> findBySupplierEvaluation(@Param("firmId") Long firmId,
                                                        @Param("vendorId") Long vendorId,
                                                        @Param("partyName") String partyName,
                                                        @Param("fromyear") Integer fromyear,
                                                        @Param("toyear") Integer toyear,
                                                        @Param("period") String period,
                                                        @Param("status") String status, Pageable page);


    @Query(value = " select s.branch as branchId , b.name as branchName , s.company as firmId , s.ledgerid as partyId , max(l.name) as partyName , s.year as year , "
            + " s.period as period,  ( SUM (s.qualityscore) / COUNT(s.id) )  as qualityScore , ( SUM (s.loyaltyscore) / COUNT(s.id) )  as loyaltyScore,  "
            + " ( SUM (s.deliveryscore) / COUNT(s.id) ) as deliveryScore , max(s.trnno) as trnNo   from supplierevaluationreport s left join ledger l on l.id = s.ledgerid  "
            + " left join branch b on s.branch = b.id WHERE (:firmId is null or s.company = :firmId) AND (:vendorId is null or s.ledgerid = :vendorId) "
            + " AND (:partyName is null or lower(l.name) like ('%' || lower(:partyName) || '%'))  "
            + " AND (:period is null or s.period = :period) "
            + " AND (:fromyear is null or s.year >= :fromyear) "
            + " AND (:toyear is null or s.year <= :toyear)"
            + " AND (:trnNo is null or s.trnno = :trnNo)"
            + " group  by s.ledgerid, s.year , s.period , s.branch , s.company, b.name ", nativeQuery = true)
    List<EvaluationReportList> findByEvaluationReportList(@Param("firmId") Long firmId, @Param("vendorId") Long vendorId,
                                                          @Param("partyName") String partyName,
                                                          @Param("fromyear") Integer fromyear,
                                                          @Param("toyear") Integer toyear,
                                                          @Param("period") String period,
                                                          @Param("trnNo") String trnNo);


    @Query(value = " select s.company as firmId,  s.ledgerid as vendorId ,  l.name as vendorName , s.branch as branchId, s.billid as billRefId," +
            "   s.billno as billNo, s.inwarddate as inwardDate , s.qualityscore as qcScore , s.deliveryscore as deliveryScore , " +
            "   s.deliveryduedate as deliveryDueDate , s.period as timePeriod, s.year as  year , po.po_no as poNumber   " +
            "   FROM supplierevaluationreport s left join ledger l on l.id  = s.ledgerid left join purchase_order po on po.id = s.poid " +
            "   WHERE s.ledgerid =:vendorId  AND s.branch =:branch AND s.company =:firmId " +
            "   AND s.year =:year AND (:timePeriod is null or s.period = :timePeriod ) ", nativeQuery = true)
    List<EvaluationBillList> findBySupplierEvaluationBillList(@Param("firmId") Long firmId,
                                                              @Param("branch") Long branch,
                                                              @Param("vendorId") Long vendorId,
                                                              @Param("timePeriod") String timePeriod,
                                                              @Param("year") Integer year);


    @Query(value = "SELECT * FROM supplierevaluationreport s WHERE (:poId is null or s.poid =:poId) ", nativeQuery = true)
    List<SupplierEvaluationReport> findByList(@Param("poId") Long poId);

}
