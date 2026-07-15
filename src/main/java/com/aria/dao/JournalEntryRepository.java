package com.aria.dao;

import com.aria.dto.SplitBinLotWiseStock;
import com.aria.dto.*;
import com.aria.model.Journal;
import com.aria.model.JournalEntry;
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
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {


    @Query(value = "SELECT p.atrndate as shiftDate, p.pshiftid as shiftId , p.sjid as journalId , jl.name as journalName, sh.name as shiftName,  "
            + " p.batchno as batchNo, p.trnno as trnNo, max(p.astatus) as status FROM journal_entry p LEFT JOIN journal jl ON jl.id = p.sjid "
            + " LEFT JOIN pshifts sh ON sh.id = p.pshiftid  WHERE (:journalId is null or p.sjid =:journalId ) "
            + " AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) ) "
            + " AND (p.atrndate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) "
            + " GROUP BY p.atrndate, p.pshiftid, p.sjid , jl.name , sh.name, p.batchno, p.trnno  ORDER BY p.atrndate DESC", nativeQuery = true)
    Page<JournalEntryList> getJournalEntryFilterList(@Param("journalId") Long journalId,
                                                     @Param("journalName") String journalName,
                                                     @Param("dateFrom") Date dateFrom,
                                                     @Param("dateTo") Date dateTo,
                                                     Pageable pageable);


    @Query(value = " SELECT p.atrndate as trnDate, p.trnno as trnNo, max(p.wcid) as wcid, max(p.wctable) as wcTable, max(p.godown) as godownId, max(p.astatus) as status, max(pm.id) as productId , max(pm.name) as producedProductName, p.sjid as journalId , "
            + " max(jl.name) as journalName,  max(sh.name) as shiftName , max(dt.name) as departmentName , max(br.name) as branchName , max(p.adate) as aDate, max(p.aby) as aby , max(p.eby) as eby"
            + " FROM journal_entry p LEFT JOIN journal jl ON jl.id = p.sjid LEFT JOIN pshifts sh ON sh.id = p.pshiftid LEFT JOIN department_details dt ON dt.id = p.did LEFT JOIN branch br ON br.id = p.bid LEFT JOIN products_master pm ON pm.id = jl.proid   "
            + "WHERE (:journalId is null or jl.id =:journalId ) "
            + "AND (:batchNo is null or  p.batchno =:batchNo ) "
            + "AND (:trnNo is null or  p.trnno =:trnNo ) "
            + "AND (:shiftid is null or  p.pshiftid =:shiftid ) "
            + "AND (cast(:shiftDate as date) is null OR p.atrndate =:shiftDate ) GROUP BY  p.atrndate, p.trnno, p.sjid  , p.pshiftid LIMIT 1", nativeQuery = true)
    JournalEntryView getJournalEntryView(@Param("journalId") Long journalId,
                                         @Param("shiftDate") Date shiftDate,
                                         @Param("batchNo") String batchNo,
                                         @Param("trnNo") String trnNo,
                                         @Param("shiftid") Long pshiftid);


    @Query(value = "SELECT * FROM journal_entry p "
            + "WHERE (:journalId is null or p.sjid =:journalId ) "
            + "AND (:batchNo is null or  p.batchno =:batchNo ) "
            + "AND (:trnNo is null or  p.trnno =:trnNo ) "
            + "AND (:shiftid is null or  p.pshiftid =:shiftid ) "
            + "AND (cast(:shiftDate as date) is null OR p.atrndate =:shiftDate ) ", nativeQuery = true)
    List<JournalEntry> getJournalEntryViewList(@Param("journalId") Long journalId,
                                               @Param("shiftDate") Date shiftDate,
                                               @Param("batchNo") String batchNo,
                                               @Param("trnNo") String trnNo,
                                               @Param("shiftid") Long pshiftid);


    @Query(value = "SELECT pm.id as itemId, pm.name as itemName " +
            "FROM  products_master pm WHERE pm.id in ( 770, 778 ) ", nativeQuery = true)
    List<YieldSummary> getYieldSummaryListVaraloti();



    @Query(value = "SELECT pm.id as itemId, pm.name as itemName " +
            "FROM  products_master pm WHERE pm.id in ( 1040, 777 ) ", nativeQuery = true)
    List<YieldSummary> getYieldSummaryList();


    @Query(value = "SELECT COALESCE(SUM(j.stkqty),0.0) as todayProduction FROM journal_entry j " +
            " WHERE j.proid =:itemId AND (j.bid =:branch ) AND (j.type='bye') " +
            " AND (cast(:accountingDate as date) is null or (j.accounting_date =:accountingDate)) " +
            " GROUP BY j.proid ", nativeQuery = true)
    Double getYieldSummaryTodayProduction(@Param("branch") Long branch,
                                                   @Param("itemId") Long itemId,
                                                   @Param("accountingDate") Date accountingDate);
    
    @Query(value = "SELECT SUM(j.stkqty) as todayProduction, j.proid as itemId, max (pm.name) as itemName "
            + " FROM journal_entry j LEFT JOIN products_master pm on pm.id = j.proid "
            + " WHERE j.proid in ( 770, 778 ) AND (j.bid =:branch ) AND (j.type='bye') "
            + " AND (cast(:startDate as timestamp) is null or (j.adate >=:startDate)) "
            + " AND (cast(:endDate as timestamp) is null or (j.adate <=:endDate))  GROUP BY j.proid ", nativeQuery = true)
    List<YieldSummary> getYieldSummaryListVaraloti(@Param("branch") Long branch,
                                                   @Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate);

    @Query(value = "SELECT SUM(j.stkqty) as todayProduction , j.proid as itemId, max (pm.name) as itemName  "
            + " FROM journal_entry j LEFT JOIN products_master pm on pm.id = j.proid "
            + " WHERE j.proid in ( 1040, 777 ) AND (j.bid =:branch ) AND (j.type='bye') "
            + " AND (cast(:startDate as timestamp) is null or (j.adate >=:startDate)) "
            + " AND (cast(:endDate as timestamp) is null or (j.adate <=:endDate)) "
            + " GROUP BY j.proid ", nativeQuery = true)
    List<YieldSummary> getYieldSummaryList(@Param("branch") Long branch,
                                           @Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);


    @Query(value = "SELECT j.stkqty  as qty, j.proid as itemId  FROM journal_entry j  "
            + " WHERE j.proid =:proId "
            + " AND (j.bid =:branch ) "
            + " AND (j.type='bom') "
            + " AND (j.batchno =:batchNo) ", nativeQuery = true)
    List<YieldSummaryList> findBy(@Param("branch") Long branch,
                                  @Param("proId") Long proId,
                                  @Param("batchNo") String batchNo);


    @Query(value = "SELECT  p.batchNo as batchNo ,  p.proid as itemId , max(p.stkqty ) as qty FROM journal_entry p  "
            + " WHERE  p.proid =:proId AND  p.bid =:branch  AND  p.type='bye'  "
            + " AND (cast(:accountingDate as date) is null or (p.accounting_date =:accountingDate))"
            + " GROUP BY   p.batchNo, p.proid ", nativeQuery = true)
    List<YieldSummaryList> getYieldSummaryValues(@Param("proId") Long proId,
                                                 @Param("branch") Long branch,
                                                 @Param("accountingDate") Date accountingDate);


    @Query(value = "SELECT  SUM(p.stkqty ) as qty FROM journal_entry p  "
            + " WHERE  p.proid =:proId AND  p.bid =:branch  AND  p.type='bom' AND p.trntype = 'I' "
            + " AND (cast(:accountingDate as date) is null or (p.accounting_date =:accountingDate)) "
            + " GROUP BY  p.proid ", nativeQuery = true)
    Double getConsumptionSummaryValues(@Param("proId") Long proId,
                                       @Param("branch") Long branch,
                                       @Param("accountingDate") Date accountingDate);


    @Query(value = "SELECT COALESCE(SUM(je.stkqty * je.lot),0.0) as qty FROM journal_entry je WHERE je.sjid in (:journalId) "
            + " AND ( je.proid =:proId ) " + " AND ( je.type='bom' ) AND je.bid = :branch "
            + " AND ( je.accounting_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ", nativeQuery = true)
    Double issueQty(@Param("journalId") List<Long> journalId, @Param("proId") Long proId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Long branch);


    @Query(value = "SELECT COALESCE(SUM(je.stkqty * je.lot),0.0) as qty FROM journal_entry je WHERE  "
            + "  ( je.proid =:proId ) " + " AND ( je.type='bom' ) "
            + " AND ( je.trndate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ", nativeQuery = true)
    Double issueQtyWithOutJournal( @Param("proId") Long proId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);



    @Query(value = "SELECT COALESCE(SUM(je.stkqty * je.lot),0.0) as qty FROM journal_entry je WHERE je.sjid in (:journalId) "
            + " AND ( je.proid =:proId ) " + " AND ( je.type='bye' ) AND je.bid = :branch "
            + " AND ( je.accounting_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ", nativeQuery = true)
    Double receiveQty(@Param("journalId") List<Long> journalId, @Param("proId") Long proId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Long branch);


    @Query(value = "SELECT COALESCE(SUM(je.stkqty * je.lot),0.0) as qty FROM journal_entry je WHERE  "
            + "  ( je.proid =:proId ) " + " AND ( je.type='bye' ) "
            + " AND ( je.trndate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ", nativeQuery = true)
    Double receiveQtyWithOutJournal(@Param("proId") Long proId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);


    @Query(value = "SELECT  p.proid as itemId , max(pm.name) as itemName, max(p.stkqty ) as qty     " +
            " FROM journal_entry p LEFT JOIN products_master pm on pm.id =p.proid     " +
            " WHERE p.bid =:branch  AND p.did =:dept  AND   p.type='bye'      " +
            " AND ( p.accounting_date  BETWEEN     " +
            "     cast(:startDate  as date) AND cast(:endDate  as date)     " +
            "    OR (cast(:startDate as date) is null OR cast(:endDate as date) is null)     " +
            "    ) GROUP BY   p.batchNo, p.proid  ", nativeQuery = true)
    List<PackingList> getPackingDetails(@Param("dept") Long dept,
                                        @Param("branch") Long branch,
                                        @Param("startDate") Date startDate,
                                        @Param("endDate") Date endDate);

    @Query(value = "SELECT * FROM journal_entry p "
            + "WHERE  (:batchNo is null or  p.batchno =:batchNo ) "
            + "AND (:trnNo is null or  p.trnno =:trnNo ) "
            + "AND (:batchid is null or  p.batchid =:batchid ) LIMIT 1", nativeQuery = true)
    Optional<JournalEntry> getJournalEntry(@Param("batchNo") String batchNo,
                                           @Param("trnNo") String trnNo,
                                           @Param("batchid") Long batchid);

    @Query(value = "SELECT  p.batchno as batchNo , p.batchid as batchId "
            + " FROM journal_entry p LEFT JOIN binstock b ON b.batchno = p.batchNo AND b.batchid = p.batchid "
            + " WHERE  b.stkqty > 0 AND p.bid =:branch  AND p.did =:dept  AND p.proid =:productId   "
            + " AND (:batchNo is null or (lower(p.batchno ) LIKE ('%' || lower(:batchNo) || '%')) ) "
            + " AND (cast(:shiftDate as date) is null OR p.atrndate =:shiftDate )  "
            + " AND (:shiftId is null OR p.pshiftid =:shiftId )  "
            + " GROUP BY   p.batchNo, p.batchid ", nativeQuery = true)
    List<InProcessBatchList> getInProcessBatchList(@Param("branch") Long branch, @Param("dept") Long dept,
                                                   @Param("batchNo") String batchNo, @Param("productId") Long productId,
                                                   @Param("shiftDate") Date shiftDate, @Param("shiftId")  Long shiftId);


    @Query(value = "SELECT  pe.mcauxid as machineId , max(m.name) as machineName , max(j.binid) as binId, max(j.binname) as binName  "
            + "  FROM journal_entry j LEFT JOIN production_entry pe on pe.sjid = j.sjid and pe.batchid = j.batchid and pe.batchno = j.batchno "
            + "  LEFT JOIN machine m ON m.id = pe.mcauxid LEFT JOIN binstock b ON b.batchno = j.batchNo AND b.batchid = j.batchid "
            + "  WHERE b.stkqty > 0 AND j.type='bye'  "
            + " AND (:batchNo is null or (lower(j.batchno ) LIKE ('%' || lower(:batchNo) || '%')) ) "
            + " AND (:batchId is null or j.batchid =:batchId ) "
            + " GROUP BY j.batchNo, j.batchid, pe.mmcid, pe.mcauxid, j.sjid  ", nativeQuery = true)
    List<InProcessBatchWithDetailList> getInProcessBatchWithAuxilaryList(@Param("batchId") Long batchId,
                                                                         @Param("batchNo") String batchNo);

    @Query(value = "SELECT  p.wcid as machineId , max(m.name) as machineName , max(p.binid) as binId, max(p.binname) as binName  "
            + " FROM journal_entry p LEFT JOIN machine m ON m.id = p.wcid LEFT JOIN binstock b ON b.batchno = p.batchNo AND b.batchid = p.batchid "
            + " WHERE b.stkqty > 0 AND p.type='bye' "
            + " AND (:batchNo is null or (lower(p.batchno ) LIKE ('%' || lower(:batchNo) || '%')) ) "
            + " AND (:batchId is null or p.batchid =:batchId ) "
            + " GROUP BY p.batchNo, p.batchid, p.wcid, p.sjid ", nativeQuery = true)
    List<InProcessBatchWithDetailList> getInProcessBatchWithDetailList(@Param("batchId") Long batchId,
                                                                       @Param("batchNo") String batchNo);


    @Query(value = "SELECT  p.wcid as machineId ,max(m.name) as machineName , p.binid as binId, p.binname as binName  "
            + " FROM journal_entry p LEFT JOIN machine m ON m.id = p.wcid LEFT JOIN binstock b ON b.batchno = p.batchNo AND b.batchid = p.batchid "
            + " WHERE b.stkqty > 0 AND p.type='bye' "
            + " AND (:batchNo is null or (lower(p.batchno ) LIKE ('%' || lower(:batchNo) || '%')) ) "
            + " AND (:batchId is null or p.batchid =:batchId ) "
            + " GROUP BY p.batchNo, p.batchid, p.wcid, p.sjid , p.binid, p.binname ", nativeQuery = true)
    List<InProcessBatchWithDetailList> getInProcessBatchWithBinDetailList(@Param("batchId") Long batchId, @Param("batchNo") String batchNo);


    @Query(value = " select  j.batchno as batchNo, j.stkqty as qty, j.proid as productId , pm.name as productName, "
            + " j.binname as binNo , j.lot as lot , j.type as type from journal_entry j left join products_master pm on pm.id = j.proid "
            + " WHERE (:batchNo is null or j.batchno =:batchNo) "
            + " AND (:binNo is null or j.binname =:binNo) "
            + " AND (:lot is null or j.lot = :lot)  ", nativeQuery = true)
    List<SplitBinLotWiseStock> getSplitJournalBatch(@Param("batchNo") String batchNo,
                                                    @Param("binNo") Long binNo,
                                                    @Param("lot") Double lot);

    @Query(value = "SELECT * FROM journal_entry p "
            + " WHERE  (:batchNo is null or  p.batchno =:batchNo ) LIMIT 1", nativeQuery = true)
    Optional<JournalEntry> getSplitJournal(@Param("batchNo") String batchNo);


    @Query(value = "SELECT pm.id as productId , max(pm.name) as productName , j.lot as lot, j.trnno as trnNo, j.batchid as batchId, j.batchno as batchNo," +
            "             SUM(j.stkqty) as qty, j.type as type , max(j.total_input_qty) as totalInputQty, max(j.total_output_qty) as totalOutputQty " +
            "             FROM journal_entry j  LEFT JOIN products_master pm ON j.proid = pm.id WHERE j.trnno = :trnNo " +
            "             group by j.proid , pm.id , j.trnno , j.type , j.lot , j.batchid , j.batchno", nativeQuery = true)
    List<WagesJournalWork> journalEntryList(@Param("trnNo") String trnNo);


    @Query(value = "SELECT * FROM journal_entry p "
            + " WHERE (:journalId is null or p.sjid =:journalId ) AND p.type='bye' "
            + " AND (:batchNo is null or  p.batchno =:batchNo ) "
            + " AND (:trnNo is null or  p.trnno =:trnNo ) "
            + " AND (:shiftid is null or  p.pshiftid =:shiftid ) "
            + " AND (cast(:shiftDate as date) is null OR p.atrndate =:shiftDate ) ", nativeQuery = true)
    List<JournalEntry> getJournalEntryViewByeList(@Param("journalId") Long journalId,
                                                  @Param("shiftDate") Date shiftDate,
                                                  @Param("batchNo") String batchNo,
                                                  @Param("trnNo") String trnNo,
                                                  @Param("shiftid") Long pshiftid);

    @Query(value = " SELECT  max(aby) as aby, max(aon) as aon, max(astatus) as astatus, max(atrndate) as atrndate, max(atrnid) as atrnid, max(atrnno) as atrnno, "
            + "  max(bptype) as bptype, max(casgrp) as casgrp, max(casweekid) as casweekid, max(cost) as cost, max(cper) as cper, max(ctype) as ctype, "
            + "  max(exid) as exid, max(godown) as godown, max(instype) as instype, max(lineid) as lineid, max(lot) as lot, max(poid) as poid, "
            + "  max(priid) as priid, max(prilot) as prilot, max(priqty) as priqty, proid, pshiftid, max(rowid) as rowid, max(sflag) as sflag,  "
            + "  max(srce) as srce, max(sstkqty) as sstkqty, max(stkqty) as stkqty, max(tastk) as tastk, max(tasum) as tasum, max(trndate) as trndate, "
            + "  trnno, max(trntype) as trntype,  type , max(wcid) as wcid, max(wctable) as wctable, max(adate) as adate, max(eby) as eby, max(bom_intake_type) as bomTakeType , "
            + "  max(total_input_qty) as total_input_qty, max(total_output_qty) as total_output_qty, batchid, batchno,  bid, did, sjid "
            + "  FROM journal_entry  WHERE (:journalId is null or sjid =:journalId ) AND type = 'bom' "
            + " AND (:batchNo is null or  batchno =:batchNo ) "
            + " AND (:trnNo is null or  trnno =:trnNo ) "
            + " AND (:shiftid is null or  pshiftid =:shiftid ) "
            + " AND (cast(:shiftDate as date) is null OR atrndate =:shiftDate ) "
            + " group by batchid, batchno , trnno , proid , sjid , pshiftid , trndate , bid, did , type order by max(id) desc ", nativeQuery = true)
    List<JournalEntryDataList> getJournalEntryViewBomList(@Param("journalId") Long journalId,
                                                          @Param("shiftDate") Date shiftDate,
                                                          @Param("batchNo") String batchNo,
                                                          @Param("trnNo") String trnNo,
                                                          @Param("shiftid") Long pshiftid);

    @Query(value = "SELECT * FROM journal_entry p "
            + " WHERE (:journalId is null or p.sjid =:journalId ) "
            + " AND (:batchNo is null or  p.batchno =:batchNo ) "
            + " AND (:trnNo is null or  p.trnno =:trnNo ) ", nativeQuery = true)
    List<JournalEntry> deleteJournalEntryList(@Param("journalId") Long journalId,
                                              @Param("batchNo") String batchNo,
                                              @Param("trnNo") String trnNo);


    @Query(value = "SELECT * FROM journal_entry p "
            + " WHERE (:journalId is null or p.sjid =:journalId ) "
            + " AND (:productId is null or  p.proid =:productId ) "
            + " AND (:type is null or  p.type =:type ) "
            + " AND (:trnNo is null or  p.trnno =:trnNo ) ", nativeQuery = true)
    List<JournalEntry> getJournalEntryTransaction(@Param("journalId") Long journalId,
                                                  @Param("productId") Long productId,
                                                  @Param("trnNo") String trnNo,
                                                  @Param("type") String type);


    @Query(value = "SELECT pm.id as productId , max(pm.name) as productName , j.lot as lot, j.trnno as trnNo, j.batchid as batchId, j.batchno as batchNo, " +
            "SUM(j.stkqty) as qty, j.type as type , max(j.total_input_qty) as totalInputQty, max(j.total_output_qty) as totalOutputQty  " +
            "FROM journal_entry j  LEFT JOIN products_master pm ON j.proid = pm.id  " +
            "WHERE j.trnno = :trnNo  and (j.sal_status IS NULL or j.sal_status <> 'Completed') " +
            "group by j.proid , pm.id , j.trnno , j.type , j.lot , j.batchid , j.batchno", nativeQuery = true)
    List<WagesJournalWork> journalTransActionList(@Param("trnNo") String trnNo);



    @Query(value = "SELECT * FROM journal_entry j where j.id in (:ids) and j.type = 'bye' " +
            "AND (:batchNo is null or j.batchno=:batchNo ) order by j.id asc ", nativeQuery = true)
    List<JournalEntry> getList(@Param("ids") List<Long> ids, @Param("batchNo") String batchNo);


    @Query(value = " select distinct je.wcid AS machineId , m.name as machineName " +
            " FROM journal_entry je left join machine m on m.id =je.wcid " +
            " WHERE je.type = 'bye' AND (:batchNo is null or je.batchno=:batchNo ) limit 1 ", nativeQuery = true)
    Optional<QcInProcessEntry> getJournalEntry (@Param("batchNo") String batchNo);


    @Query(value = " SELECT COALESCE(SUM(j.stkqty * j.lot),0.0) as todayProduction FROM journal_entry j " +
            " WHERE j.proid =:itemId AND (j.bid =:branch ) AND (j.did =:dept ) AND (j.type='bom') " +
            " AND (cast(:accountingDate as date) is null or (j.accounting_date =:accountingDate)) " +
            " GROUP BY j.proid ", nativeQuery = true)
    Double getYieldSummaryTodayProductionIssue(@Param("branch") Long branch,
                                               @Param("dept") Long dept,
                                          @Param("itemId") Long itemId,
                                          @Param("accountingDate") Date accountingDate);


    @Query(value = "SELECT p.atrndate as shiftDate, p.pshiftid as shiftId , p.sjid as journalId , jl.name as journalName, sh.name as shiftName,  "
            + " p.batchno as batchNo, p.trnno as trnNo, max(p.astatus) as status FROM journal_entry p LEFT JOIN journal jl ON jl.id = p.sjid "
            + " LEFT JOIN pshifts sh ON sh.id = p.pshiftid  WHERE (:journalId is null or p.sjid =:journalId ) "
            + " AND (:journalName is null or (lower(jl.name ) LIKE ('%' || lower(:journalName) || '%')) ) "
            + " AND (p.atrndate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) "
            + " GROUP BY p.atrndate, p.pshiftid, p.sjid , jl.name , sh.name, p.batchno, p.trnno  ORDER BY p.atrndate DESC", nativeQuery = true)
    Page<JournalEntryList> getASRSEntryFilterList(@Param("journalId") Long journalId,
                                                     @Param("journalName") String journalName,
                                                     @Param("dateFrom") Date dateFrom,
                                                     @Param("dateTo") Date dateTo,
                                                     Pageable pageable);
}
