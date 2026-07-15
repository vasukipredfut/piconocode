package com.aria.dao;

import com.aria.dto.*;
import com.aria.model.ProductionEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductionEntryRepository extends JpaRepository<ProductionEntry, Long> {

    @Query(value = "SELECT  *  FROM production_entry b WHERE  b.shiftid =:shiftId AND b.sjid =:journalId AND b.shiftdate =:shiftDate ", nativeQuery = true)
    List<ProductionEntry> findBy(@Param("journalId") Long journalId, @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);


    @Query(value = "SELECT       " +
            "     p.shiftdate as shiftDate, max(p.tsid) as tsId , max(p.tsname) as tsName, max(p.tstable) as tsTable,        " +
            "     max(p.dctype) as dcType , p.trnno as trnNo , p.batchno as batchNo, p.batchid as batchId,       " +
            "     jl.id as journalId , max(jl.name) as journalName, max(p.humidity) as humidity,  max(p.temperature) as temperature,       " +
            "     max(sh.name) as shiftName ,  sh.id as shiftId, max(wc.estatus) as status , p.mmcid as machineId ,  p.mmctable as mmctable,       " +
            "     max(g.id) as godownId, max(g.name) as godownName , max(br.id) as branchId, max(br.name) as branchName ,      " +
            "     max(dd.id) as deptId, max(dd.name) as deptName       " +
            "     FROM production_entry p       " +
            "     LEFT JOIN journal jl ON jl.id = p.sjid       " +
            "     LEFT JOIN pshifts sh ON sh.id = p.shiftid         " +
            "     LEFT JOIN godown g ON g.id = p.godownid      " +
            "     LEFT JOIN branch br ON br.id = p.branch       " +
            "     LEFT JOIN department_details dd ON dd.id = p.dept       " +
            "     LEFT JOIN wcshift wc ON wc.sjid = p.sjid AND wc.batchid = p.batchid AND wc.batchno = p.batchno AND wc.pshiftid = p.shiftid AND wc.shiftdate = p.shiftdate      " +
            "     WHERE (:branchId is null or p.branch=:branchId ) AND (:deptId is null or p.dept =:deptId) AND (:journalId is null or jl.id =:journalId )      " +
            "     AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) )      " +
            "     AND (:status is null or (lower(wc.estatus) LIKE ('%' || lower(:status) || '%')) )      " +
            "     AND (p.shiftdate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )      " +
            "     GROUP BY p.branch , p.dept ,   p.shiftdate,  p.status, p.batchno, p.batchid, jl.id, sh.id , p.mmcid , p.mmctable , p.trnno order by max(p.id) desc       " , nativeQuery = true)
    Page<ProductionEntryList> getProductionEntryFilterList(@Param("branchId") Long branchId,
                                                           @Param("deptId") Long deptId,
                                                           @Param("journalId") Long journalId,
                                                           @Param("journalName") String journalName,
                                                           @Param("status") String status,
                                                           @Param("dateFrom") Date dateFrom,
                                                           @Param("dateTo") Date dateTo, Pageable pageable);

    @Query(value = "SELECT  *  FROM production_entry b WHERE  b.shiftid =:shiftId AND b.sjid =:journalId AND b.shiftdate = cast(:shiftDate  as date) AND b.batchno =:batchno AND b.tstable =:tstable  AND b.dctype =:dcType", nativeQuery = true)
    List<ProductionEntry> findBy(@Param("journalId") Long journalId, @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate, @Param("batchno") String batchno, @Param("dcType") String dcType, @Param("tstable") String tstable);

    @Query(value = "SELECT  p.batchid as batchId, p.batchno as batchNo, max(p.trnno) as trnNo , p.shiftdate as shiftDate , p.shiftid as shiftId ,  p.sjid as journalId, p.tsid as tsId,  max (p.tsname) as tsName, p.mmcid as machineId, p.mcauxid as auxMachineId FROM production_entry p WHERE  " +
            " (:shiftId is null or p.shiftid =:shiftId ) AND " +
            " ( :journalId is null or p.sjid =:journalId ) AND " +
            " ( :batchno is null or p.batchno =:batchno ) AND " +
            " ( :shiftId is null or p.shiftid =:shiftId ) AND " +
            " ( cast(:shiftdate  as date) is null or p.shiftdate = :shiftdate ) " +
            " group by p.batchid, p.batchno, p.sjid, p.shiftid , p.shiftdate ,  p.tsid , p.mmcid , p.mcauxid  ", nativeQuery = true)
    List<ProductionEntryGrouping> findBy(@Param("journalId") Long journalId, @Param("shiftId") Long shiftId, @Param("shiftdate") Date shiftdate, @Param("batchno") String batchno);


    @Query(value = " SELECT   colid as colId, colname as colName, coltype as colType, colcate as colCate , " +
            " coltable as colTable , data_res_values as resultVal, datatype as dataType, sjid as journalId, " +
            " consider_as as considerAs , col_lot as colLot , tsid as tsId , ptype as pType , bom_intake_type as bomIntakeType " +
            " FROM production_entry WHERE  " +
            " ( :batchId is null or batchid =:batchId ) AND " +
            " ( :journalId is null or sjid =:journalId ) AND " +
            " ( :tsId is null or tsid =:tsId ) AND " +
            " ( :mmcid is null or mmcid =:mmcid ) AND " +
            " ( :auxmcid is null or mcauxid =:auxmcid ) AND " +
            " ( :shiftId is null or shiftid =:shiftId ) AND " +
            " ( cast(:shiftdate  as date) is null or shiftdate = :shiftdate ) AND " +
            " ( :batchno is null or batchno =:batchno ) ", nativeQuery = true)
    List<ProductionEntryGroupingList> findBy(@Param("journalId") Long journalId,
                                             @Param("batchId") Long batchId,
                                             @Param("batchno") String batchno,
                                             @Param("tsId") Long tsId,
                                             @Param("mmcid") Long mmcid,
                                             @Param("auxmcid") Long auxmcid,
                                             @Param("shiftId") Long shiftId,
                                             @Param("shiftdate") Date shiftdate);


    @Query(value = "SELECT p.sjid as journalId, max(j.name) as journalName, p.mmcid as machineId, " +
            " max(m.name)  as machineName, p.mcauxid as auxMachineId , max(am.name)  as auxMachineName  " +
            " FROM production_entry p left join journal j on j.id = p.sjid left join machine m on m.id = p.mmcid " +
            " left join machine am on am.id = p.mcauxid  WHERE  " +
            " (:shiftId is null or p.shiftid =:shiftId ) AND " +
            " ( :journalId is null or p.sjid =:journalId ) AND " +
            "  ( :batchno is null or p.batchno =:batchno ) " +
            " group by  p.sjid,  p.mmcid , p.mcauxid ", nativeQuery = true)
    List<ProductionEntryMachineGrouping> findByMachineDetails(@Param("journalId") Long journalId,
                                                              @Param("shiftId") Long shiftId,
                                                              @Param("batchno") String batchno);


    @Query(value = "SELECT  p.batchid, p.batchno as batchNo, p.branch as branchId, max(b.name) as branchName, p.dept as deptId, max(d.name) as deptName , "
            + " p.sjid as journalId, max(j.name) as journalName,  p.mmcid, p.shiftid as shiftId, max(ph.name) as shiftName, "
            + " max(p.dctype) as dcType , p.shiftdate as shiftDate,  p.trnno as trnNo ,"
            + " CASE " +
            "   WHEN EXISTS ( " +
            "     SELECT 1 FROM asrs_production_detail adp " +
            "     WHERE adp.journalid = p.sjid AND adp.trnno = p.trnno" +
            "   ) THEN 'YES' " +
            "   ELSE 'NO' " +
            " END AS asrsMapped " 
            + " FROM production_entry p LEFT JOIN journal j ON j.id  = p.sjid LEFT JOIN branch b ON b.id = p.branch "
            + " LEFT JOIN department_details d ON d.id = p.dept  LEFT JOIN pshifts ph ON ph.id = p.shiftid "
            + " LEFT JOIN journal_entry je ON je.sjid  = p.sjid AND je.pshiftid  = p.shiftid  AND je.atrndate = p.shiftdate  AND je.batchid = p.batchid AND je.batchno  = p.batchno AND je.bid = p.branch  "
            + " WHERE p.branch =:branch "
            + " AND (cast(:startDate as timestamp) is null or (p.created_date >=:startDate)) "
            + " AND (cast(:endDate as timestamp) is null or (p.created_date <=:endDate)) "
            + " AND (je.batchno IS NULL OR je.batchno = '') "
            + " GROUP BY p.batchid, p.batchno, p.branch, p.dept, p.sjid, p.shiftid, p.shiftdate,  p.trnno , p.mmcid, je.batchno ", nativeQuery = true)
    List<JournalOrMrnNotClosingList> findBy(@Param("branch") Long branch,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);

    @Query(
            value =
                    " SELECT " +
                            "   p.batchid, " +
                            "   p.batchno AS batchNo, " +
                            "   p.branch AS branchId, " +
                            "   b.name AS branchName, " +
                            "   p.dept AS deptId, " +
                            "   d.name AS deptName, " +
                            "   p.sjid AS journalId, " +
                            "   j.name AS journalName, " +
                            "   p.mmcid, " +
                            "   p.shiftid AS shiftId, " +
                            "   ph.name AS shiftName, " +
                            "   p.dctype AS dcType, " +
                            "   p.shiftdate AS shiftDate, " +
                            "   p.trnno AS trnNo, " +
                            "   CASE " +
                            "       WHEN EXISTS ( " +
                            "           SELECT 1 FROM asrs_production_detail adp " +
                            "           WHERE adp.journalid = p.sjid " +
                            "             AND adp.trnno = p.trnno " +
                            "       ) THEN 'YES' " +
                            "       ELSE 'NO' " +
                            "   END AS asrsMapped " +
                            " FROM production_entry p " +
                            " LEFT JOIN journal j ON j.id = p.sjid " +
                            " LEFT JOIN branch b ON b.id = p.branch " +
                            " LEFT JOIN department_details d ON d.id = p.dept " +
                            " LEFT JOIN pshifts ph ON ph.id = p.shiftid " +
                            " LEFT JOIN journal_entry je " +
                            "   ON je.sjid = p.sjid " +
                            "  AND je.pshiftid = p.shiftid " +
                            "  AND je.atrndate = p.shiftdate " +
                            "  AND je.batchid = p.batchid " +
                            "  AND je.batchno = p.batchno " +
                            "  AND je.bid = p.branch " +
                            " WHERE p.branch IN (:branchList) " +
                            " AND (cast(:startDate as timestamp) is null or (p.created_date >=:startDate)) "+
                            " AND (cast(:endDate as timestamp) is null or (p.created_date <=:endDate)) "+
                            "   AND (je.batchno IS NULL OR je.batchno = '') " +
                            " GROUP BY " +
                            "   p.batchid, p.batchno, p.branch, b.name, " +
                            "   p.dept, d.name, p.sjid, j.name, " +
                            "   p.mmcid, p.shiftid, ph.name, " +
                            "   p.dctype, p.shiftdate, p.trnno " +
                            " ORDER BY MAX(p.created_date) ASC ",
            nativeQuery = true
    )
    List<JournalOrMrnNotClosingList> findByBranchList(
            @Param("branchList") List<Long> branchList,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );



    @Query(value = " SELECT  *  FROM production_entry b " +
            " WHERE (:shiftId is null or b.shiftid =:shiftId ) " +
            " AND (:journalId is null or b.sjid =:journalId ) " +
            " AND (:batchno is null or b.batchno =:batchno ) " +
            " AND ( cast(:shiftDate as date) is null or b.shiftdate =:shiftDate  ) " +
            " AND (:trnNo is null or b.trnno = :trnNo) " +
            " AND (:tsId is null or b.tsid = :tsId) " +
            " AND (:tsName is null or b.tsname = :tsName) " +
            " AND (:tstable is null or b.tstable =:tstable  ) ", nativeQuery = true)
    List<ProductionEntry> deleteProductionEntrySlapDetails(@Param("journalId") Long journalId,
                                                           @Param("shiftId") Long shiftId,
                                                           @Param("shiftDate") Date shiftDate,
                                                           @Param("batchno") String batchno,
                                                           @Param("tstable") String tstable,
                                                           @Param("tsName") String tsName,
                                                           @Param("tsId") Long tsId,
                                                           @Param("trnNo") String trnNo);



    @Query(value = " SELECT  *  FROM production_entry b " +
            " WHERE (:shiftId is null or b.shiftid =:shiftId ) " +
            " AND (:journalId is null or b.sjid =:journalId ) " +
            " AND (:batchno is null or b.batchno =:batchno ) " +
            " AND ( cast(:shiftDate as date) is null or b.shiftdate =:shiftDate  ) " +
            " AND (:trnNo is null or b.trnno = :trnNo) " , nativeQuery = true)
    List<ProductionEntry> deleteProductionEntry(@Param("journalId") Long journalId,
                                                           @Param("shiftId") Long shiftId,
                                                           @Param("shiftDate") Date shiftDate,
                                                           @Param("batchno") String batchno,
                                                           @Param("trnNo") String trnNo);





    @Query(value = " SELECT p.shiftdate as shiftDate, max(p.tsid) as tsId , max(p.tsname) as tsName, max(p.tstable) as tsTable,  " +
            " max(p.dctype) as dcType , p.trnno as trnNo , p.batchno as batchNo, p.batchid as batchId, jl.id as journalId , " +
            " max(jl.name) as journalName, max(p.humidity) as humidity,  max(p.temperature) as temperature, " +
            " max(sh.name) as shiftName ,  sh.id as shiftId, max(wc.estatus) as status , p.mmcid as machineId ,   " +
            " p.mmctable as mmctable, max(g.id) as godownId, max(g.name) as godownName " +
            " FROM production_entry p LEFT JOIN journal jl ON jl.id = p.sjid  " +
            " LEFT JOIN pshifts sh ON sh.id = p.shiftid   LEFT JOIN godown g ON g.id = p.godownid " +
            " LEFT JOIN wcshift wc ON wc.sjid = p.sjid AND wc.batchid = p.batchid  " +
            " AND wc.batchno = p.batchno AND wc.pshiftid = p.shiftid AND wc.shiftdate = p.shiftdate " +
            " WHERE (:branchId is null or p.branch=:branchId ) AND (:deptId is null or p.dept =:deptId)  " +
            " AND (:journalId is null or jl.id =:journalId ) " +
            " AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) ) " +
            " AND (:status is null or (lower(wc.estatus) LIKE ('%' || lower(:status) || '%')) ) " +
            " AND (p.accounting_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) " +
            " GROUP BY p.shiftdate,  p.status, p.batchno, p.batchid, jl.id, sh.id , p.mmcid , p.mmctable , p.trnno order by max(p.id) desc  ", nativeQuery = true)
    Page<ProductionEntryList> getDayEndPageProductionEntryFilterList(@Param("branchId") Long branchId,
                                                                     @Param("deptId") Long deptId,
                                                                     @Param("journalId") Long journalId,
                                                                     @Param("journalName") String journalName,
                                                                     @Param("status") String status,
                                                                     @Param("dateFrom") Date dateFrom,
                                                                     @Param("dateTo") Date dateTo,
                                                                     Pageable pageable);




    @Query(value = "SELECT  *  FROM production_entry b WHERE   b.sjid =:journalId AND  b.batchid =:batchId AND  b.batchno =:batchno AND b.trnno =:trnNo order by b.id desc limit 1 ", nativeQuery = true)
    ProductionEntry findBy(@Param("journalId") Long journalId, @Param("batchId") Long batchId,  @Param("batchno") String batchno, @Param("trnNo") String trnNo);



    @Query(value = "SELECT p.shiftdate as shiftDate, max(p.tsid) as tsId , max(p.tsname) as tsName, max(p.tstable) as tsTable,  max(p.dctype) as dcType , p.trnno as trnNo , " +
            "             p.batchno as batchNo, p.batchid as batchId, jl.id as journalId , max(jl.name) as journalName, max(p.humidity) as humidity,  max(p.temperature) as temperature, " +
            "             max(sh.name) as shiftName ,  sh.id as shiftId, max(wc.estatus) as status , p.mmcid as machineId ,  p.mmctable as mmctable, max(g.id) as godownId, max(g.name) as godownName " +
            "             FROM production_entry p LEFT JOIN journal jl ON jl.id = p.sjid" +
            "             LEFT JOIN pshifts sh ON sh.id = p.shiftid   LEFT JOIN godown g ON g.id = p.godownid" +
            "             LEFT JOIN wcshift wc ON wc.sjid = p.sjid AND wc.batchid = p.batchid AND wc.batchno = p.batchno AND wc.pshiftid = p.shiftid AND wc.shiftdate = p.shiftdate" +
            "             WHERE (:branchId is null or p.branch=:branchId ) AND (:deptId is null or p.dept =:deptId) AND (:journalId is null or jl.id =:journalId )" +
            "             AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) )" +
            "             AND (:status is null or (lower(wc.estatus) LIKE ('%' || lower(:status) || '%')) )" +
            "             AND (p.shiftdate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )" +
            "             GROUP BY p.shiftdate,  p.status, p.batchno, p.batchid, jl.id, sh.id , p.mmcid , p.mmctable , p.trnno order by max(p.id) desc " +
            "             offset :offset limit :limitv ", nativeQuery = true)
    List<ProductionEntryList> getProductionEntryFilterList(@Param("branchId") Long branchId,
                                                           @Param("deptId") Long deptId,
                                                           @Param("journalId") Long journalId,
                                                           @Param("journalName") String journalName,
                                                           @Param("status") String status,
                                                           @Param("dateFrom") Date dateFrom,
                                                           @Param("dateTo") Date dateTo,
                                                           @Param("offset") long offset,
                                                           @Param("limitv") long limitv);


    @Query(value = " SELECT count(ta)  FROM ( SELECT count(1)  FROM production_entry p LEFT JOIN journal jl ON jl.id = p.sjid " +
            "       LEFT JOIN pshifts sh ON sh.id = p.shiftid   LEFT JOIN godown g ON g.id = p.godownid " +
            "       LEFT JOIN wcshift wc ON wc.sjid = p.sjid AND wc.batchid = p.batchid AND wc.batchno = p.batchno AND wc.pshiftid = p.shiftid AND wc.shiftdate = p.shiftdate " +
            "       WHERE (:branchId is null or p.branch=:branchId ) AND (:deptId is null or p.dept =:deptId) AND (:journalId is null or jl.id =:journalId )" +
            "       AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) )" +
            "       AND (:status is null or (lower(wc.estatus) LIKE ('%' || lower(:status) || '%')) )" +
            "       AND (p.shiftdate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )" +
            "       GROUP BY p.shiftdate,  p.status, p.batchno, p.batchid, jl.id, sh.id , p.mmcid , p.mmctable , p.trnno order by max(p.id) desc ) as ta ", nativeQuery = true)
    Long getProductionEntryFilterListCount(@Param("branchId") Long branchId,
                                           @Param("deptId") Long deptId,
                                           @Param("journalId") Long journalId,
                                           @Param("journalName") String journalName,
                                           @Param("status") String status,
                                           @Param("dateFrom") Date dateFrom,
                                           @Param("dateTo") Date dateTo);




    //  29-08-2025 added one column is asrsstatus ,  by dhivya (for amendment restriction), discussed by alaguraja sir

    @Query(value = "SELECT       " +
            " p.trnno as trnNo, j.id as journalId, j.name as journalName , j.proid as productId, pm.name as productName , " +
            " wc.estatus as status, p.batchid as batchId , p.shiftdate as shiftDate, " +
            " p.pshift as shiftId , s.name as shiftName , ars.id as asrsId , max(wc.dominocount) as dominoCount, " +
            " ars.status as asrsStatus , max(pm.pallet_quantity) as palletQuantity ,pm.asrs_product_type as asrsProductType  " +
            " from production_entry p " +
            " LEFT JOIN journal j ON j.id = p.sjid " +
            " LEFT JOIN products_master pm on pm.id = j.proid" +
            " LEFT JOIN pshifts s on s.id = p.pshift" +
            " LEFT JOIN wcshift wc ON wc.sjid = p.sjid AND wc.trnno = p.trnno " +
            " AND wc.batchno = p.batchno AND wc.pshiftid = p.pshift  AND wc.shiftdate = p.shiftdate " +
            " LEFT JOIN asrs_production_detail ars ON ars.journalid = p.sjid AND ars.trnno = p.trnno " +
            " WHERE j.asrs = true  AND (:branchId is null or p.branch=:branchId ) " +
            " AND (:deptId is null or p.dept =:deptId) AND (:journalId is null or j.id =:journalId ) " +
            " AND (:journalName is null or (lower(j.name ) LIKE ('%' || lower(:journalName) || '%')) )      " +
            " AND (:trnNo is null or (lower(p.trnno ) LIKE ('%' || lower(:trnNo) || '%')) )      " +
            " AND (:productName is null or (lower(pm.name ) LIKE ('%' || lower(:productName) || '%')) )      " +
            " AND (:status is null or (lower(wc.estatus) LIKE ('%' || lower(:status) || '%')) )      " +
            " AND ( cast(:shiftDate as date) is null or p.shiftdate =:shiftDate )      " +
            " GROUP BY p.trnno , j.id, j.name  , j.proid , pm.name ,  wc.estatus , " +
            "  p.batchid  , p.shiftdate  , p.pshift , s.name , ars.id ,ars.asrs_status,pm.asrs_product_type order by max(p.id) desc  " , nativeQuery = true)
    Page<ASRSProductionEntryList> getASRSProductionEntryFilterList(@Param("branchId") Long branchId,
                                                           @Param("deptId") Long deptId,
                                                           @Param("journalId") Long journalId,
                                                           @Param("journalName") String journalName,
                                                           @Param("productName") String productName,
                                                           @Param("trnNo") String trnNo,
                                                           @Param("status") String status,
                                                           @Param("shiftDate") Date shiftDate,
                                                            Pageable pageable);

    @Query(value = "SELECT p.trnno as trnNo, p.data_res_values as dataResValues from production_entry p WHERE  p.colid IN (:colId1,:colId2) " , nativeQuery = true)
            List<partcategoryList> findByColId(Long colId1, Long colId2);




    @Query(value = "SELECT * FROM production_entry b " +
            " WHERE  b.coltable = 'pattributes' " +
            " AND (:trnNo IS NULL OR b.trnno = :trnNo) " +
            " AND (:journalId IS NULL OR b.sjid = :journalId) " +
            " AND (:shiftId IS NULL OR b.shiftid = :shiftId) " +
            " AND (:batchno IS NULL OR b.batchno = :batchno) " +
            " AND (CAST(:shiftDate AS date) IS NULL OR b.shiftdate = :shiftDate) " +
            " AND (:trnNo IS NULL OR b.trnno = :trnNo) " +
            " ORDER BY b.id DESC ", nativeQuery = true)
    List<ProductionEntry> findProductionEntryRowsForMap(@Param("journalId") Long journalId,
                                                 @Param("shiftId") Long shiftId,
                                                 @Param("shiftDate") Date shiftDate,
                                                 @Param("batchno") String batchno,
                                                 @Param("trnNo") String trnNo);
    @Query(value = "SELECT * FROM production_entry b " +
            " WHERE  b.coltable = 'pattributes' " +
            " AND (:trnNo IS NULL OR b.trnno = :trnNo) " +
            " AND (:journalId IS NULL OR b.sjid = :journalId) " +
            " AND (:trnNo IS NULL OR b.trnno = :trnNo) " +
            " ORDER BY b.id DESC ", nativeQuery = true)
    List<ProductionEntry> findProductionEntryRowsForMap(@Param("journalId") Long journalId,
                                                        @Param("trnNo") String trnNo);

    @Query(value = "SELECT DISTINCT p.data_res_values as dataResValues , p.batchno as trnNo from production_entry p WHERE  p.colid IN (:colId1,:colId2) " , nativeQuery = true)
	List<partcategoryList> findPackingPatchByColId(@Param("colId1") long colId1,@Param("colId2") long colId2);




   @Query(value = "SELECT * from production_entry p WHERE  p.colid IN (44,304) AND p.coltable = 'pattributes' and p.trnno = :trnNo ORDER BY p.id DESC LIMIT 1 " , nativeQuery = true)
        ProductionEntry findPackingPatchByTrnNo(@Param("trnNo") String trnNo);



}
