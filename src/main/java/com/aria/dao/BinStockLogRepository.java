package com.aria.dao;

import com.aria.dto.*;
import com.aria.model.BinStock;
import com.aria.model.BinStockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface BinStockLogRepository extends JpaRepository<BinStockLog, Long> {


    @Query(value = "SELECT COALESCE(SUM(bin.qty),0.0) from stbin bin WHERE "
            + " (:productId is null or bin.refid = :productId) "
            + "AND (:entryType is null or bin.entry_type = :entryType) "
            + "AND ( bin.bdt BETWEEN (cast(:start as timestamp)) AND (cast(:end as timestamp)) )", nativeQuery = true)
    Object getMonthSock(@Param("productId") Long productId, @Param("entryType") String entryType, @Param("start") Date start, @Param("end") Date end);


    @Query(value = "SELECT st.id, st.adate as accountingDate, st.updatedon as txnDate, st.trnno as txnNo,  st.entry_type as type, st.naration as narration,  st.trntype as trntype, st.qty as qty FROM stbin st WHERE "
            + " (:productId is null or st.refid = :productId) "
            + "AND (:companyId is null or st.cid = :companyId) "
            + "AND (:branchId is null or st.branch = :branchId) "
            + "AND (cast(:start as timestamp) is null or (st.adate >=:start)) "
            + "AND (cast(:end as timestamp) is null or (st.adate >=:end)) ORDER BY st.id DESC", nativeQuery = true)
    Page<StockLedger> getStockLedger(@Param("companyId") Long companyId, @Param("branchId") Long branchId, @Param("productId") Long productId, @Param("start") Date start, @Param("end") Date end, Pageable pageable);


    @Query(value = "SELECT s.branch , b.name as branchName , s.dept , dd.name as deptName, COALESCE(SUM(s.qty),0.0) as qty  from stbin s , branch b, department_details dd  wHERE b.id = s.branch  and dd.id  = s.dept "
            + " AND (:productId is null or s.refid = :productId) "
            + " AND (s.trntype =:trnType) "
            + " AND ( s.bdt BETWEEN (cast(:start as timestamp)) AND (cast(:end as timestamp)) )   group by s.branch , s.dept , b.name, dd.name ", nativeQuery = true)
    List<PlantWiseStock> getMonthConceptionOrInwardStock(@Param("productId") Long productId, @Param("trnType") String trnType, @Param("start") Date start, @Param("end") Date end);

    @Query(value = "SELECT COALESCE(SUM(bin.qty),0.0) from stbin bin WHERE "
            + " (:productId is null or bin.refid = :productId) "
            + " AND (s.trntype = 'I') "
            + " AND ( bin.bdt BETWEEN (cast(:start as timestamp)) AND (cast(:end as timestamp)) )", nativeQuery = true)
    Double getMonthSock(@Param("productId") Long productId, @Param("start") Date start, @Param("end") Date end);


    @Query(value = "SELECT COALESCE(SUM(s.qty),0.0) from stbin s WHERE "
            + " (:productId is null or s.refid = :productId) "
            + "AND (s.trntype = 'I') ", nativeQuery = true)
    Double getConceptionStock(@Param("productId") Long productId);


    @Query(value = "SELECT s.branch , b.name as branchName , s.dept , dd.name as deptName, COALESCE(SUM(s.qty),0.0) as qty  from stbin s , branch b, department_details dd  wHERE b.id = s.branch  and dd.id  = s.dept "
            + " AND (:productId is null or s.refid = :productId) "
            + " AND (s.trntype =:trnType) GROUP BY s.branch , s.dept , b.name, dd.name ", nativeQuery = true)
    List<PlantWiseStock> getConceptionStock(@Param("productId") Long productId, @Param("trnType") String trnType);


    @Query(value = "SELECT COALESCE(SUM(s.qty),0.0) from stbin s WHERE " +
            " (:productId is null or s.refid = :productId) " +
            " AND (:batchNo is null or s.batchno = :batchNo) " +
            " AND (:binName is null or s.gname = :binName) " +
            " AND (s.trntype = 'I') ", nativeQuery = true)
    Object getConceptionStock(@Param("productId") Long productId, @Param("batchNo") String batchNo, @Param("binName") Long binName);


    @Query(value = " SELECT *  from stbin s "
            + " WHERE (:trnNo is null or s.trnno =:trnNo)  "
            + " AND (:journalId is null or s.sjid = :journalId) "
            + " AND (:productId is null or s.refid = :productId) "
            + " AND (s.trntype =:trnType) ", nativeQuery = true)
    List<BinStockLog> getLogList(@Param("journalId") Long journalId,
                                 @Param("productId") Long productId,
                                 @Param("trnType") String trnType,
                                 @Param("trnNo") String trnNo);


    @Query(value = " SELECT s.batchid as batchid, s.batchno as batchno, s.gcode as binid, s.lot as lot , s.trntype , "
            + " s.gname as binname , max(gd.id)  as godownId , max(gd.name) as godownName ,"
            + " max(pm.name ) as productName , SUM(s.qty) as stkqty , SUM(s.cost) as cost , max(s.avgcost) as avgcost"
            + " from stbin s left join products_master pm on pm.id = s.refid "
            + " left join gogroup g on g.gcode = s.gcode and s.gname  = g.goname "
            + " left join godown gd on gd.id = g.gid  "
            + " WHERE s.batchid <> 0 AND (:trnNo is null or s.trnno =:trnNo)  "
            + " AND (:journalId is null or s.sjid = :journalId) "
            + " AND (:productId is null or s.refid = :productId) "
            + " AND (s.trntype =:trnType) "
            + " group by s.batchid , s.batchno , s.gcode , s.gname , s.refid , s.lot , s.trntype ", nativeQuery = true)
    List<BinStockLogList> getBinStockLogList(@Param("journalId") Long journalId,
                                             @Param("productId") Long productId,
                                             @Param("trnType") String trnType,
                                             @Param("trnNo") String trnNo);


    @Query(value = "SELECT * from stbin s WHERE s.entry_type='Stock Inward' AND s.trntype ='R' "
            + " AND(:trnNo is null or s.trnno =:trnNo)  "
            + " AND (:productId is null or s.refid = :productId)  "
            + " AND (:lot is null or s.lot = :lot) ORDER BY s.id ASC ", nativeQuery = true)
    List<BinStockLog> getTransactionList(@Param("productId") Long productId, @Param("trnNo") String trnNo, @Param("lot") Double lot);


    @Query(value = "SELECT s.refid as productId ,  max(pm.name) as productName , s.lot as lot, s.trnno as trnNo, " +
            "             s.batchid as batchId, s.batchno as batchNo, SUM(s.qty) as qty " +
            "             FROM stbin s LEFT JOIN products_master pm ON s.refid = pm.id " +
            "             WHERE (:trnNo is null or s.trnno = :trnNo ) and (s.sal_status IS NULL OR s.sal_status<>'Completed') " +
            "             AND s.entry_type='Stock Inward' AND s.trntype ='R' " +
            "             group by s.refid , s.trnno , s.lot , s.batchid , s.batchno ", nativeQuery = true)
    List<WagesJournalWork> inwardProductList(@Param("trnNo") String trnNo);


    @Query(value = " SELECT s.refid as productId , max(pm.name) as productName , s.lot as lot, s.trnno as trnNo, SUM(s.qty) as qty " +
            " FROM stbin s LEFT JOIN products_master pm ON s.refid = pm.id " +
            " WHERE (:trnNo is null or s.trnno = :trnNo ) and (s.sal_status IS NULL OR s.sal_status<>'Completed')" +
            " AND s.entry_type='MATERIAL_TRANSACTION' AND s.trntype ='I' " +
            " group by s.refid , s.trnno , s.lot ", nativeQuery = true)
    List<WagesJournalWork> mrnIssueProductList(@Param("trnNo") String trnNo);


    @Query(value = "SELECT s.refid as productId ,  max(pm.name) as productName , s.lot as lot, s.trnno as trnNo, " +
            "s.batchid as batchId, s.batchno as batchNo, SUM(s.qty) as qty " +
            "FROM stbin s LEFT JOIN products_master pm ON s.refid = pm.id " +
            "WHERE (:trnNo is null or s.trnno = :trnNo ) and (s.sal_status IS NULL OR s.sal_status<>'Completed') " +
            "AND s.entry_type='MATERIAL_TRANSACTION' AND s.trntype ='R' " +
            "group by s.refid , s.trnno , s.lot , s.batchid , s.batchno ", nativeQuery = true)
    List<WagesJournalWork> mrnOtherBranchReceiptProductList(@Param("trnNo") String trnNo);


    @Query(value = "SELECT * from stbin s WHERE (:trnNo is null or s.trnno =:trnNo)  "
            + " AND (:trnType is null or s.trntype = :trnType) "
            + " AND (:productId is null or s.refid = :productId) "
            + " AND (:entryType is null or s.entry_type = :entryType) "
            + " AND (:lot is null or s.lot = :lot) ORDER BY s.id ASC ", nativeQuery = true)
    List<BinStockLog> getMRNTransactionList(@Param("productId") Long productId,
                                            @Param("trnNo") String trnNo,
                                            @Param("trnType") String trnType,
                                            @Param("lot") Double lot,
                                            @Param("entryType") String entryType);


    @Query(value = "SELECT s.refid as productId ,  max(pm.name) as productName , s.lot as lot, s.trnno as trnNo, " +
            "s.batchid as batchId, s.batchno as batchNo, SUM(s.qty) as qty " +
            "FROM stbin s LEFT JOIN products_master pm ON s.refid = pm.id " +
            " WHERE (:trnNo is null or s.trnno = :trnNo ) and (s.sal_status IS NULL OR s.sal_status<>'Completed') " +
            " AND s.entry_type='EXTERNAL_GODOWN' AND s.trntype ='R' " +
            "group by s.refid , s.trnno , s.lot , s.batchid , s.batchno  ", nativeQuery = true)
    List<WagesJournalWork> getGodownReceiveItem(@Param("trnNo") String trnNo);


    @Query(value = "SELECT distinct s.lot FROM stbin s "
            + " WHERE  ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)   ", nativeQuery = true)
    List<Double> findByLotList(@Param("trnDate") Date trnDate);


    @Query(value = "SELECT COALESCE(SUM(s.qty),0.0) from stbin s WHERE "
            + " (:productId is null or s.refid = :productId) "
            + " AND (:branch is null or s.branch = :branch) "
            + " AND (:lot is null or s.lot =:lot) "
            + " AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)   "
            + " AND (s.entry_type ='Stock Inward' ) AND (s.trntype = 'R') ", nativeQuery = true)
    Object rawMaterialMovementSummary(@Param("productId") Long productId,
                                      @Param("branch") Long branch,
                                      @Param("lot") Double lot,
                                      @Param("trnDate") Date trnDate);

    @Query(value = " SELECT COALESCE(SUM(s.qty),0.0) from stbin s   " +
            " left join material_issue mi on mi.trn_no = s.trnno    " +
            " WHERE " +
            " s.trnno in (  " +
            "       SELECT s.trnno from stbin s   " +
            "       left join material_issue mi on mi.trn_no = s.trnno and s.toid = mi.request_branch_id and mi.issue_branch_id = s.fmid  " +
            "       WHERE (:productId is null or s.refid = :productId)    " +
            "       AND (:reqbranch is null or s.toid  = :reqbranch)    " +
            "       AND (:issuebranch is null or s.fmid  = :issuebranch)    " +
            "       AND (:lot is null or s.lot = :lot)    " +
            "       AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)    " +
            "       AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype = 'R') " +
            " ) " +
            " AND (:productId is null or s.refid = :productId)    " +
            " AND (:reqbranch is null or mi.request_branch_id  = :reqbranch)    " +
            " AND (:issuebranch is null or mi.issue_branch_id  = :issuebranch)    " +
            " AND (:lot is null or s.lot = :lot)    " +
            " AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)    " +
            " AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype = 'I')  ", nativeQuery = true)
    Object rawMaterialMovementSummaryMRNIssue(@Param("productId") Long productId,
                                              @Param("reqbranch") Long reqbranch,
                                              @Param("issuebranch") Long issuebranch,
                                              @Param("lot") Double lot,
                                              @Param("trnDate") Date trnDate);


    @Query(value = " SELECT COALESCE(SUM(s.qty),0.0) from stbin s  " +
            "left join material_issue mi on mi.trn_no = s.trnno and s.toid = mi.request_branch_id and mi.issue_branch_id = s.fmid " +
            "WHERE (:productId is null or s.refid = :productId)   " +
            "AND (:reqbranch is null or s.toid  = :reqbranch)   " +
            "AND (:issuebranch is null or s.fmid  = :issuebranch)   " +
            "AND (:lot is null or s.lot = :lot)   " +
            "AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)   " +
            "AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype = 'R')", nativeQuery = true)
    Object rawMaterialMovementSummaryMRNReceive(@Param("productId") Long productId,
                                                @Param("reqbranch") Long reqbranch,
                                                @Param("issuebranch") Long issuebranch,
                                                @Param("lot") Double lot,
                                                @Param("trnDate") Date trnDate);


    @Query(value = " SELECT COALESCE(SUM(s.qty),0.0) from stbin s  " +
            " WHERE (:productId is null or s.refid = :productId)  " +
            " AND (:branch is null or s.branch = :branch) " +
            " AND (:lot is null or s.lot = :lot)  " +
            " AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate) " +
            " AND (s.entry_type ='PRODUCTION' ) AND (s.trntype = 'I')  ", nativeQuery = true)
    Object rawMaterialMovementSummaryProductionIssue(@Param("productId") Long productId,
                                                     @Param("branch") Long branch,
                                                     @Param("lot") Double lot,
                                                     @Param("trnDate") Date trnDate);


    @Query(value = " SELECT COALESCE(SUM(s.qty), 0.0) from stbin s  " +
            " WHERE (:productId is null or s.refid = :productId)  " +
            " AND (:branch is null or s.branch = :branch) " +
            " AND (:lot is null or s.lot = :lot)  " +
            " AND ( cast(:trnDate as date) is null or s.accounting_date =:trnDate) " +
            " AND (s.entry_type ='PRODUCTION' ) AND (s.trntype = 'R')  ", nativeQuery = true)
    Object rawMaterialMovementSummaryProductionReceive(@Param("productId") Long productId,
                                                       @Param("branch") Long branch,
                                                       @Param("lot") Double lot,
                                                       @Param("trnDate") Date trnDate);


    @Query(value = "SELECT * from stbin s WHERE (:trnNo is null or s.trnno =:trnNo) ORDER BY s.id ASC ", nativeQuery = true)
    List<BinStockLog> getProductionRevertList(@Param("trnNo") String trnNo);


    @Query(value = "SELECT max(b.billno) as billNo , max(b.received_date) as inwardDate , max(s.trn_date) trnDate, max(b.lorry_serial_no) as lorrySerialNo, bi.batch_no as batchNo, sum(s.qty) as qty  " +
            "FROM stbin s left join bill_inward_product_item_received bi on bi.batch_no = s.batchno and bi.batch_id = s.batchid  " +
            "left join bill_inward b on b.id = bi.inward_id  " +
            "WHERE (:productId is null or s.refid = :productId)  " +
            "AND (:branch is null or s.branch = :branch)  " +
            "AND (:lot is null or s.lot =:lot)  " +
            "AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)    " +
            "AND (s.entry_type ='Stock Inward' ) AND (s.trntype = 'R') " +
            "group by b.id , bi.batch_no , bi.batch_id ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> getPurchaseRawMaterialMovementQtyWiseSummary(@Param("branch") Long branch,
                                                                                         @Param("productId") Long productId,
                                                                                         @Param("lot") Double lot,
                                                                                         @Param("trnDate") Date trnDate);


    @Query(value = "SELECT s.gcode as binId, s.gname as binName , gd.name as godownName , s.qty as qty " +
            "FROM stbin s left join gogroup g on g.goname = s.gname left join godown gd on gd.id = g.gid  " +
            "WHERE (:productId is null or s.refid = :productId)  " +
            "AND (:branch is null or s.branch = :branch)  " +
            "AND (:lot is null or s.lot =:lot)  " +
            "AND (:batchNo is null or s.batchno = :batchNo)   " +
            "AND (s.entry_type ='Stock Inward' ) AND (s.trntype = 'R')", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> getPurchaseRawMaterialMovementQtyWiseSummaryBinAndGodown(@Param("branch") Long branch,
                                                                                                     @Param("productId") Long productId,
                                                                                                     @Param("lot") Double lot,
                                                                                                     @Param("batchNo") String batchNo);


    @Query(value = "SELECT s.trnno as trnNo , COALESCE(SUM(s.qty),0.0) as qty       " +
            "from stbin s left join material_issue mi on mi.trn_no = s.trnno         " +
            "WHERE mi.issue_branch_id != mi.request_branch_id      " +
            "AND (:productId is null or s.refid = :productId)         " +
            "AND (:reqbranch is null or mi.request_branch_id  = :reqbranch)         " +
            "AND (:issuebranch is null or mi.issue_branch_id  = :issuebranch)         " +
            "AND (:lot is null or s.lot = :lot)         " +
            "AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)         " +
            "AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype ='I')        " +
            "group by s.trnno , s.refid , s.lot", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementQtyWiseSummaryIssueTo(@Param("reqbranch") Long reqbranch,
                                                                                     @Param("issuebranch") Long issuebranch,
                                                                                     @Param("productId") Long productId,
                                                                                     @Param("lot") Double lot,
                                                                                     @Param("trnDate") Date trnDate);


    @Query(value = "SELECT s.gcode as binId, s.gname as binName , gd.name as godownName , s.qty as qty , s.batchno  as batchNo " +
            "FROM stbin s left join gogroup g on g.goname = s.gname left join godown gd on gd.id = g.gid  " +
            "WHERE (:productId is null or s.refid = :productId)  " +
            "AND (:lot is null or s.lot =:lot)  " +
            "AND (:trnNo is null or s.trnno = :trnNo)  " +
            "AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype ='I')  ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementQtyWiseSummaryIssueToBinAndGodown(@Param("productId") Long productId,
                                                                                                 @Param("lot") Double lot,
                                                                                                 @Param("trnNo") String trnNo);


    @Query(value = "SELECT s.trnno as trnNo , COALESCE(SUM(s.qty),0.0) as qty  from stbin s " +
            "left join material_issue mi on mi.trn_no = s.trnno    " +
            "WHERE (:productId is null or s.refid = :productId)    " +
            "AND (:reqbranch is null or mi.request_branch_id  = :reqbranch)    " +
            "AND (:issuebranch is null or mi.issue_branch_id  = :issuebranch)    " +
            "AND (:lot is null or s.lot = :lot)    " +
            "AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)    " +
            "AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype ='R')   " +
            "group by s.trnno , s.refid , s.lot ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementQtyWiseSummaryReceivedFrom(@Param("reqbranch") Long reqbranch,
                                                                                          @Param("issuebranch") Long issuebranch,
                                                                                          @Param("productId") Long productId,
                                                                                          @Param("lot") Double lot,
                                                                                          @Param("trnDate") Date trnDate);


    @Query(value = " SELECT s.gcode as binId, s.gname as binName , gd.name as godownName , s.qty as qty , s.batchno  as batchNo " +
            " FROM stbin s left join gogroup g on g.goname = s.gname left join godown gd on gd.id = g.gid  " +
            " WHERE (:productId is null or s.refid = :productId)  " +
            " AND (:lot is null or s.lot =:lot)  " +
            " AND (:trnNo is null or s.trnno = :trnNo)  " +
            " AND (s.entry_type ='MATERIAL_TRANSACTION' ) AND (s.trntype ='R')  ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementQtyWiseSummaryReceivedBinAndGodown(@Param("productId") Long productId,
                                                                                                  @Param("lot") Double lot,
                                                                                                  @Param("trnNo") String trnNo);


    @Query(value = "SELECT s.trnno as trnNo , s.batchno  as batchNo , s.refid as itemId, s.lot as lot , COALESCE(SUM(s.qty),0.0) as qty from stbin s   " +
            " WHERE (:productId is null or s.refid = :productId)  " +
            " AND (:branch is null or s.branch = :branch)  " +
            " AND (:lot is null or s.lot = :lot)   " +
            " AND ( cast(:trnDate as date) is null or s.accounting_date = :trnDate)  " +
            " AND (s.entry_type =:entryType) AND (s.trntype = :trnType)  " +
            " group by s.trnno , s.refid , s.batchno, s.lot  ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementSummaryProduction(@Param("branch") Long branch,
                                                                                 @Param("productId") Long productId,
                                                                                 @Param("lot") Double lot,
                                                                                 @Param("trnDate") Date trnDate,
                                                                                 @Param("entryType") String entryType,
                                                                                 @Param("trnType") String trnType
    );


    @Query(value = "SELECT s.gcode as binId, s.gname as binName , gd.name as godownName , s.qty as qty , s.batchno  as batchNo " +
            "FROM stbin s left join gogroup g on g.goname = s.gname left join godown gd on gd.id = g.gid  " +
            "WHERE (:productId is null or s.refid = :productId)  " +
            "AND (:lot is null or s.lot =:lot)  " +
            "AND (:batchNo is null or s.batchno = :batchNo)  " +
            "AND (:trnNo is null or s.trnno = :trnNo)  " +
            "AND (s.entry_type =:entryType) AND (s.trntype =:trnType)  ", nativeQuery = true)
    List<RawMaterialMovementQtyWiseSummary> rawMaterialMovementSummaryProductionBinAndGodown(@Param("productId") Long productId,
                                                                                             @Param("lot") Double lot,
                                                                                             @Param("batchNo") String batchNo,
                                                                                             @Param("trnNo") String trnNo,
                                                                                             @Param("entryType") String entryType,
                                                                                             @Param("trnType") String trnType);


    @Query(value = " SELECT b.gname as binName from stbin b WHERE  b.gname <> 0 " +
            " AND b.refid =:productId " +
            " AND b.lot =:lot " +
            " AND (:dept is null or b.dept = :dept) " +
            " AND ( cast(:trnDate as date) is null or b.accounting_date = :trnDate)  " +
            " AND (:branch is null or b.branch = :branch)  ORDER BY  b.gname ASC  ", nativeQuery = true)
    List<Long> getStockPaperBinList1(@Param("productId") Long productId, @Param("lot") Double lot,
                                     @Param("branch") Long branch, @Param("dept") Long dept, @Param("trnDate") Date trnDate);


    @Query(value = " SELECT SUM(b.qty) as qty , b.entry_type as entryType, b.trntype as trnType  " +
            " FROM stbin b WHERE  b.gname <> 0 " +
            " AND b.refid =:productId " +
            " AND b.lot =:lot" +
            " AND (:binName is null or b.gname  = :binName) " +
            " AND (:dept is null or b.dept = :dept)" +
            " AND ( cast(:trnDate as date) is null or b.accounting_date = :trnDate)  " +
            " AND (:branch is null or b.branch = :branch) " +
            " AND (:type is null or b.trntype = :type) " +
            " GROUP BY  b.entry_type , b.trntype ", nativeQuery = true)
    List<StockPaper> getStockPaperIssueItems(@Param("binName") Long binName,
                                             @Param("productId") Long productId,
                                             @Param("lot") Double lot,
                                             @Param("branch") Long branch,
                                             @Param("dept") Long dept,
                                             @Param("trnDate") Date trnDate,
                                             @Param("type") String type);



    @Query(value = "SELECT distinct s.lot FROM stbin s "
            + " WHERE s.refid=:productId "
            + " AND ( s.accounting_date BETWEEN :startDate AND :endDate )   ", nativeQuery = true)
    List<Double> findByLotList(@Param("productId") Long productId,
                               @Param("startDate") Date startDate,
                               @Param("endDate") Date endDate
                               );


    @Query(value = " SELECT coalesce(SUM(s.qty),0.0) as qty , s.lot as lot FROM stbin s "
            + " WHERE s.sjid in (   "
            + "     select distinct prjg.sjid from process_report_journal_group prjg "
            + "     where prjg.prgrptitle =:menuId and prjg.prid =:reportNameId and prjg.prgrpid  =:reportGroupId "
            + "     ) "
            + " AND s.branch=:branch  AND s.trntype =:trnType AND s.lot in (:lots) AND s.refid=:productId "
            + " AND s.entry_type =:entryType AND ( s.accounting_date BETWEEN :startDate AND :endDate )  "
            + " group by s.refid, s.lot , s.entry_type , s.trntype ", nativeQuery = true)
    List<ProcessReportProductLotWithStockList> getStock(@Param("branch") Long branch, @Param("reportNameId") Long reportNameId, @Param("reportGroupId") Long reportGroupId,
                    @Param("menuId") Long menuId, @Param("productId") Long productId,  @Param("lots") List<Double> lots,
                    @Param("trnType") String trnType, @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                                        @Param("entryType") String entryType );




    @Query(value = " SELECT coalesce(SUM(s.qty),0.0) as qty , s.lot as lot FROM stbin s "
            + " WHERE  s.branch=:branch AND s.refid=:productId AND s.lot in (:lots)   "
            + " AND s.entry_type =:entryType AND ( s.accounting_date BETWEEN :startDate AND :endDate )  "
            + " GROUP BY s.refid, s.lot , s.entry_type  ", nativeQuery = true)
    List<ProcessReportProductLotWithStockList> getStock(  @Param("branch") Long branch, @Param("productId") Long productId,  @Param("lots") List<Double> lots,
                                                         @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                                        @Param("entryType") String entryType );




    @Query(value = " SELECT coalesce(SUM(s.qty),0.0) as qty , s.lot as lot FROM stbin s "
            + " WHERE  s.branch=:branch AND s.refid=:productId AND s.lot in (:lots)   "
            + " AND s.entry_type =:entryType AND s.trntype =:trnType AND ( s.accounting_date BETWEEN :startDate AND :endDate )  "
            + " GROUP BY s.refid, s.lot , s.entry_type ,s.trntype  ", nativeQuery = true)
    List<ProcessReportProductLotWithStockList> getStock(  @Param("branch") Long branch, @Param("productId") Long productId,  @Param("lots") List<Double> lots,
                                                          @Param("trnType") String trnType ,  @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                                        @Param("entryType") String entryType );





    @Query(value = "select sum(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
            + " WHERE s.branch =:branch AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
            + " AND s.sjid =:journalId AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYRotary(@Param("branch") Long branch, @Param("type") String type, @Param("trnType") String trnType,
                                 @Param("itemIds") List<Long> ids, @Param("journalId") Long journalId,
                                 @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);



    @Query(value = "select sum(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
            + " WHERE s.branch =:branch AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYCrushing(@Param("branch") Long branch, @Param("type") String type, @Param("trnType") String trnType,
                                       @Param("itemIds") List<Long> ids,
                                       @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);




    @Query(value = "select sum(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
            + " WHERE s.branch =:branch AND s.dept =:dept AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
            + " AND s.sjid =:journalId AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYQc(@Param("branch") Long branch, @Param("dept") Long dept,
                                       @Param("type") String type, @Param("trnType") String trnType,
                                       @Param("itemIds") List<Long> ids, @Param("journalId") Long journalId,
                                       @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);



    @Query(value = "select sum(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
            + " WHERE s.branch =:branch AND s.dept =:dept AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYQcWithOutJournal(@Param("branch") Long branch, @Param("dept") Long dept,
                                   @Param("type") String type, @Param("trnType") String trnType,
                                   @Param("itemIds") List<Long> ids,
                                   @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);



    @Query(value = "select SUM(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
            + " WHERE s.branch =:branch  AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
            + " AND s.sjid = :sjid AND  s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYPackingList(@Param("branch") Long branch,
                                   @Param("type") String type, @Param("trnType") String trnType,
                                   @Param("itemIds") List<Long> ids, @Param("fromDate") Date fromDate,
                                            @Param("toDate") Date toDate,@Param("sjid") Long sjid);



    @Query(value = " SELECT *  from stbin s "
            + " WHERE (:batchNo is null or s.batchno =:batchNo)  "
            + " AND (:batchId is null or s.batchid = :batchId) "
            + " AND (s.trntype =:trnType) ", nativeQuery = true)
    List<BinStockLog> getDeleteLogList(@Param("batchId") Long batchId,
                                 @Param("batchNo") String batchNo,
                                 @Param("trnType") String trnType);


    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal  "
            + " WHERE bal.branch =:branch AND  bal.entry_type =:type AND bal.trntype = :trnType AND bal.refid =:itemId AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND (:dept is null or bal.dept = :dept) AND (:gid is null or bal.gid= :gid) group by bal.refid , bal.trntype , bal.entry_type , bal.lot ", nativeQuery = true)   
    List<StockQtyWithLot> findByRefidAfterCleaningData( @Param("branch") Long branch,  @Param("type") String type,  @Param("trnType") String trnType, @Param("itemId") Long itemId, @Param("fromDate") Date fromDate,
    		 @Param("toDate") Date toDate , @Param("dept") Long dept,@Param("gid") Long gid);
    
    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal  "
            + " WHERE bal.branch =:branch AND bal.trntype = :type  AND bal.refid =:itemId AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by bal.refid , bal.trntype, bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> findBYRefIdWithLots(@Param("branch") Long branch, @Param("type") String type,
                                             @Param("itemId") Long itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate );

    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal  left join material_issue mi on mi.trn_no = bal.trnno"
            + " WHERE mi.request_branch_id = 2 AND mi.issue_branch_id =:branch AND  bal.entry_type =:type AND bal.trntype = :trnType AND bal.refid =:itemId AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND (:dept is null or bal.dept = :dept)and (bal.gid = :gid)  group by bal.refid , bal.trntype , bal.entry_type , bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> interPlantTOPlant2(@Param("branch") Long branch,  @Param("type") String type,  @Param("trnType") String trnType, @Param("itemId") Long itemId, @Param("fromDate") Date fromDate,
   		 @Param("toDate") Date toDate , @Param("dept") Long dept,@Param("gid") Long gid);

    @Query(value = "SELECT distinct s.lot FROM stbin s "
            + " WHERE s.refid=:itemId "
            + "  AND EXTRACT(YEAR FROM s.accounting_date) = :year "
            + "  AND EXTRACT(MONTH FROM s.accounting_date) = :month   ", nativeQuery = true)
	List<Double> findByListLot(  @Param("itemId")  Long itemId, @Param("year")  Long year, @Param("month")  Long month);

    @Query(value = "SELECT distinct  s.lot as lot ,s.gid as godownId ,g.name as gname  FROM stbin s  left join godown g on g.id=s.gid"
            + " WHERE s.gid is not null AND s.refid=:itemId AND (:branch is null OR s.branch = :branch) AND (:firmid is null or s.cid = :firmid ) "
            + "  AND EXTRACT(YEAR FROM s.accounting_date) = :year "
            + "  AND EXTRACT(MONTH FROM s.accounting_date) = :month  AND (:lot is null or s.lot = :lot) ", nativeQuery = true)
	List<GoDownDetailsForBankList> findTransactionsByProduct(@Param("branch") Long branch,@Param("firmid") Long firmid,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot);


    @Query(value = "select sum(bal.qty) as qty  from stbin bal  WHERE (bal.entry_type != 'MATERIAL_TRANSACTION' OR (bal.entry_type = 'MATERIAL_TRANSACTION' AND bal.fmid = :branch AND bal.toid = :branch)) "
            + " AND  (:firmid is null or bal.cid = :firmid ) AND  (:branch is null or bal.branch = :branch) AND bal.trntype = :type  AND bal.refid =:itemId AND EXTRACT(YEAR FROM bal.accounting_date) = :year "
            + " AND EXTRACT(MONTH FROM bal.accounting_date) = :month AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid)  group by bal.refid , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQtyReciept(@Param("branch") Long branch,@Param("firmid") Long firmid, @Param("type") String type,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot,@Param("gid") Long gid);
    
    @Query(value = "select sum(bal.qty) as qty  from stbin bal WHERE (bal.entry_type != 'MATERIAL_TRANSACTION' OR (bal.entry_type = 'MATERIAL_TRANSACTION' AND bal.fmid = :branch AND bal.toid = :branch))  "
            + " AND (:firmid is null or bal.cid = :firmid )AND (:branch is null or bal.branch = :branch) AND (bal.trntype is null OR bal.trntype = 'I') AND bal.refid =:itemId AND EXTRACT(YEAR FROM bal.accounting_date) = :year "
            + " AND EXTRACT(MONTH FROM bal.accounting_date) = :month AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid)   group by bal.refid , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQtyIssue(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot,@Param("gid") Long gid);
    
    @Query(value = "select sum(bal.qty) as qty  from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmid is null or bal.cid = :firmid )AND (:branch is null or mi.issue_branch_id = :branch) AND bal.trntype = :type AND (bal.entry_type ='MATERIAL_TRANSACTION' )  AND bal.refid =:itemId AND EXTRACT(YEAR FROM bal.accounting_date) = :year "
            + " AND EXTRACT(MONTH FROM bal.accounting_date) = :month AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid) group by bal.refid , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQtynterPlantIssue(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("type") String type,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot,@Param("gid") Long gid);
    
    @Query(value = "select sum(bal.qty) as qty  from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmid is null or bal.cid = :firmid )AND (:branch is null or mi.request_branch_id = :branch) AND bal.trntype = :type AND (bal.entry_type ='MATERIAL_TRANSACTION' )  AND bal.refid =:itemId AND EXTRACT(YEAR FROM bal.accounting_date) = :year "
            + " AND EXTRACT(MONTH FROM bal.accounting_date) = :month AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid) group by bal.refid , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQtynterPlantReceipt(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("type") String type,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot,@Param("gid") Long gid);

    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal left join  material_issue mi on mi.trn_no = bal.trnno "
            + " WHERE mi.issue_branch_id != mi.request_branch_id AND bal.branch =:branch AND  bal.entry_type =:type AND bal.trntype = :trnType AND bal.refid =:itemId AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND (:gid is null or bal.gid= :gid) group by bal.refid , bal.trntype , bal.entry_type , bal.lot ", nativeQuery = true)   
	List<StockQtyWithLot> findByRefidReceivedFromData(@Param("branch") Long branch,@Param("type") String type,@Param("trnType") String trnType,@Param("itemId") Long itemId,
			@Param("fromDate")Date fromDate,@Param("toDate") Date toDate,@Param("gid") Long gid);
    
    @Query(value = "SELECT distinct s.lot ,s.branch , b.name FROM stbin s left join branch b on b.id = s.branch"
            + " WHERE s.refid=:itemId "
            + "  AND EXTRACT(YEAR FROM s.accounting_date) = :year "
            + "  AND EXTRACT(MONTH FROM s.accounting_date) = :month   ", nativeQuery = true)
	List<Object[]> findBranchLotList(  @Param("itemId")  Long itemId, @Param("year")  Long year, @Param("month")  Long month);
	
	@Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from  stbin bal "
            + " WHERE (:branch is null OR bal.branch =:branch) AND  bal.entry_type =:type AND (bal.refid  IN(:refId)) AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.cid = :firmId)  group by  bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> findByRefidQtyWithLot(@Param("branch") Long branch, @Param("type") String type,
                                              @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

	@Query(value = "select sum(s.qty ) as qty , s.lot as lot  from stbin s  "
            + " WHERE (:branch is null OR s.branch =:branch) AND (:dept is null or s.dept =:dept) AND (:firmId is null or s.cid = :firmId) AND  s.trntype =:trnType AND s.entry_type =:type AND (s.refid IN(:refId)) "
            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by  s.lot ", nativeQuery = true)
    List<StockQtyWithLot> findBYRecRefWithLot(@Param("branch") Long branch, @Param("type") String type,@Param("trnType") String trnType, @Param("dept") Long dept,  
            @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
	
	@Query(value = "select sum(bal.qty) as qty , bal.lot as lot from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
	            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmId is null or bal.cid = :firmId )AND (:branch is null or bal.branch = :branch) AND bal.trntype = :trntype AND bal.entry_type = :type AND (bal.refid IN(:refId)) "
	            + " AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by bal.lot ", nativeQuery = true)
	 List<StockQtyWithLot> findBYInterRefidWithLot(@Param("branch") Long branch, @Param("type") String type,@Param("trntype") String trntype,  
	           @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

	 @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal  "
	            + " WHERE (:branch is null OR bal.branch =:branch) AND bal.trntype = :type  AND (bal.refid IN(:refId)) AND (:firmId is null or bal.cid = :firmId ) AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by  bal.lot ", nativeQuery = true)
	    List<StockQtyWithLot> findBYTotalRefIdWithLots(@Param("branch") Long branch, @Param("type") String type,
	                                             @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate ,@Param("firmId") Long firmId);
	 
	 @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from  stbin bal "
	            + " WHERE (:branch is null OR bal.branch =:branch) AND  bal.entry_type =:type AND ( bal.sjid IN(:sjId)) AND (bal.refid  IN(:refId)) AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.cid = :firmId)  group by  bal.lot ", nativeQuery = true)
	    List<StockQtyWithLot> findByRefidQtyWithLotSjid(@Param("branch") Long branch, @Param("type") String type,
	                                             @Param("sjId") List<Long> sjId, @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

		@Query(value = "select sum(s.qty ) as qty , s.lot as lot  from stbin s  "
	            + " WHERE (:branch is null OR s.branch =:branch) AND (:dept is null or s.dept =:dept) AND (:firmId is null or s.cid = :firmId) AND  s.trntype =:trnType AND s.entry_type =:type AND (s.sjid IN(:sjId)) AND (s.refid IN(:refId)) "
	            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by  s.lot ", nativeQuery = true)
	    List<StockQtyWithLot> findBYRecRefWithLotSjid(@Param("branch") Long branch, @Param("type") String type,@Param("trnType") String trnType, @Param("dept") Long dept,  
	            @Param("sjId") List<Long> sjId,@Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
		
		@Query(value = "select sum(bal.qty) as qty , bal.lot as lot from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
		            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmId is null or bal.cid = :firmId )AND (:branch is null or bal.branch = :branch) AND bal.trntype = :trntype AND bal.entry_type = :type   AND ( bal.sjid IN(:sjId)) AND (bal.refid IN(:refId)) "
		            + " AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by bal.lot ", nativeQuery = true)
		 List<StockQtyWithLot> findBYInterRefidWithLotSjid(@Param("branch") Long branch, @Param("type") String type,@Param("trntype") String trntype,  
		            @Param("sjId") List<Long> sjId,  @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

		 @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal  "
		            + " WHERE (:branch is null OR bal.branch =:branch) AND bal.trntype = :type  AND (bal.sjid IN(:sjId)) AND (bal.refid IN(:refId)) AND (:firmId is null or bal.cid = :firmId ) AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by  bal.lot ", nativeQuery = true)
		    List<StockQtyWithLot> findBYTotalRefIdWithLotsSjid(@Param("branch") Long branch, @Param("type") String type,
		                                             @Param("sjId") List<Long> sjId,@Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate ,@Param("firmId") Long firmId);
		 
	 
	 @Query(value = "select sum(bal.qty) as qty ,bal.refid as itemId ,bal.lot as lot from  stbin bal "
	            + " WHERE (:branch is null OR bal.branch =:branch) AND  bal.entry_type =:type AND  bal.refid in (:itemId) AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.cid = :firmId) AND bal.lot in (:lots)   group by bal.refid , bal.lot", nativeQuery = true)
	    List<DayEndStockList> findByFgStock(@Param("branch") Long branch, @Param("type") String type,
	                                             @Param("itemId") List<Long> itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,List<Double> lots, @Param("firmId") Long firmId);

	 @Query(value = "select sum(bal.qty) as qty ,bal.refid as itemId,bal.branch as branchId ,bal.lot as lot from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
	            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmId is null or bal.cid = :firmId ) AND bal.trntype = :trntype AND bal.entry_type = :type   AND  bal.refid in (:itemId) AND bal.lot in (:lot) "
	            + " AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by bal.branch ,bal.refid, bal.lot ", nativeQuery = true)
	 List<DayEndStockList> findBYFgStockInterIssue( @Param("type") String type,@Param("trntype") String trntype,  
	            @Param("itemId") List<Long> itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,List<Double> lot, @Param("firmId") Long firmId);
	 @Query(value = "select sum(s.qty) as qty , s.refid as itemId ,s.lot as lot from stbin s  "
	            + " WHERE s.branch =:branch AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) "
	            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate AND s.lot in (:lot) AND (:firmId is null or s.cid =:firmId)  group by s.refid , s.trntype , s.entry_type , s.lot ", nativeQuery = true)
	    List<DayEndStockList> findBYStockCrushing(@Param("branch") Long branch, @Param("type") String type, @Param("trnType") String trnType,
	                                       @Param("itemIds") List<Long> ids,
	                                       @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("lot")List<Double> lot,@Param("firmId") Long firmId);

	 
	 
	 @Query(value = " SELECT coalesce(SUM(s.qty),0.0) as qty , s.lot as lot,s.toid as branch FROM stbin s "
	            + " WHERE  s.fmid=:branch AND s.refid=:productId AND s.lot in (:lots) AND s.fmid <> s.toid    "
	            + " AND s.entry_type =:entryType AND s.trntype =:trnType AND ( s.accounting_date BETWEEN :startDate AND :endDate )  "
	            + " GROUP BY s.refid, s.lot , s.entry_type ,s.trntype ,s.toid ", nativeQuery = true)
	    List<ProcessReportProductLotWithStockList> getStockIssueByPlant(  @Param("branch") Long branch, @Param("productId") Long productId,  @Param("lots") List<Double> lots,
	                                                          @Param("trnType") String trnType ,  @Param("startDate") Date startDate, @Param("endDate") Date endDate,
	                                                        @Param("entryType") String entryType );
	 @Query(value = " SELECT coalesce(SUM(s.qty),0.0) as qty , s.lot as lot,s.fmid as branch FROM stbin s "
	            + " WHERE  s.toid=:branch AND s.refid=:productId AND s.lot in (:lots) AND s.fmid <> s.toid   "
	            + " AND s.entry_type =:entryType AND s.trntype =:trnType AND ( s.accounting_date BETWEEN :startDate AND :endDate )  "
	            + " GROUP BY s.refid, s.lot , s.entry_type ,s.trntype ,s.fmid ", nativeQuery = true)
	    List<ProcessReportProductLotWithStockList> getStockReceiptByPlant(  @Param("branch") Long branch, @Param("productId") Long productId,  @Param("lots") List<Double> lots,
	                                                          @Param("trnType") String trnType ,  @Param("startDate") Date startDate, @Param("endDate") Date endDate,
	                                                        @Param("entryType") String entryType );
	
	 @Query(value = " select s.refid as itemId ,max(pm.name) as name ,sum(s.qty *s.lot) as stockQty , CAST(s2.conversion AS TEXT) as conversion  from stbin s  left join products_master pm  on pm.id=s.refid "
	 		+ "	 		 	left join stkoillist s2  on s2.refid=s.refid  WHERE s.branch =:branch AND (:dept is null or s.dept =:dept) AND  s.trntype ='R' AND s.entry_type =:type AND s.cid=:firmId and s.refid in (:refid) "
	 		+ "	 		 	 AND  s.accounting_date  BETWEEN :fromDate AND :toDate group by s.refid,pm.name,s2.conversion  " , nativeQuery = true)
	    List<ByeProductDtoList> findBYPackingLists(@Param("branch") Long branch, @Param("dept") Long dept,
	                                  @Param("type") String type,
	                                   @Param("firmId") Long firmId, @Param("fromDate") Date fromDate,
	                                            @Param("toDate") Date toDate,  @Param("refid") List<Long> refid);
	
	 @Query(value = "SELECT distinct s.lot ,s.branch , b.name FROM stbin s left join branch b on b.id = s.branch"
	            + " WHERE s.refid=:itemId "
	            + "  AND ( s.accounting_date BETWEEN :fromDate AND :todate ) ", nativeQuery = true)
		List<Object[]> findBranchLotListByDate(  @Param("itemId")  Long itemId, @Param("fromDate")  Date fromDate, @Param("todate")  Date todate);
		
		
		@Query(value = "SELECT distinct  s.lot as lot ,s.gid as godownId ,g.name as gname  FROM stbin s  left join godown g on g.id=s.gid"
		            + " WHERE s.gid is not null AND s.refid=:itemId AND (:branch is null OR s.branch = :branch) AND (:firmid is null or s.cid = :firmid ) "
		            + "  AND ( s.accounting_date BETWEEN :fromDate AND :todate )  AND (:lot is null or s.lot = :lot) ", nativeQuery = true)
			List<GoDownDetailsForBankList> findTransactionsByProductByDate(@Param("branch") Long branch,@Param("firmid") Long firmid,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot);
		
		 @Query(value = "SELECT distinct s.lot FROM stbin s "
		            + " WHERE s.refid=:itemId "
		            + "  AND ( s.accounting_date BETWEEN :fromDate AND :todate )  ", nativeQuery = true)
			List<Double> findByListLotByDate(  @Param("itemId")  Long itemId, @Param("fromDate")  Date fromDate, @Param("todate")  Date todate);
		
		 @Query(value = "select sum(bal.qty) as qty  from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
		            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmid is null or bal.cid = :firmid )AND (:branch is null or mi.issue_branch_id = :branch) AND bal.trntype = :type AND (bal.entry_type ='MATERIAL_TRANSACTION' )  AND bal.refid =:itemId AND ( bal.accounting_date BETWEEN :fromDate AND :todate ) AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid) group by bal.refid , bal.lot ", nativeQuery = true)
			Double findBYRefIdWithQtynterPlantIssueByDate(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("type") String type,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot,@Param("gid") Long gid);
		    
		
		  @Query(value = "select sum(bal.qty) as qty  from stbin bal left join material_issue mi on mi.trn_no = bal.trnno  "
		            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmid is null or bal.cid = :firmid )AND (:branch is null or mi.request_branch_id = :branch) AND bal.trntype = :type AND (bal.entry_type ='MATERIAL_TRANSACTION' )  AND bal.refid =:itemId AND ( bal.accounting_date BETWEEN :fromDate AND :todate ) AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid) group by bal.refid , bal.lot ", nativeQuery = true)
			Double findBYRefIdWithQtynterPlantReceiptByDate(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("type") String type,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot,@Param("gid") Long gid);
		  
		  @Query(value = "select sum(bal.qty) as qty  from stbin bal  WHERE (bal.entry_type != 'MATERIAL_TRANSACTION' OR (bal.entry_type = 'MATERIAL_TRANSACTION' AND bal.fmid = :branch AND bal.toid = :branch))  "
		            + " AND  (:firmid is null or bal.cid = :firmid ) AND  (:branch is null or bal.branch = :branch) AND bal.trntype = :type  AND bal.refid =:itemId AND ( bal.accounting_date BETWEEN :fromDate AND :todate ) AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid)  group by bal.refid , bal.lot ", nativeQuery = true)
			Double findBYRefIdWithQtyRecieptByDate(@Param("branch") Long branch,@Param("firmid") Long firmid, @Param("type") String type,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot,@Param("gid") Long gid);
		    
		    @Query(value = "select sum(bal.qty) as qty  from stbin bal WHERE (bal.entry_type != 'MATERIAL_TRANSACTION' OR (bal.entry_type = 'MATERIAL_TRANSACTION' AND bal.fmid = :branch AND bal.toid = :branch))   "
		            + " AND (:firmid is null or bal.cid = :firmid )AND (:branch is null or bal.branch = :branch) AND (bal.trntype is null OR bal.trntype = 'I') AND bal.refid =:itemId AND ( bal.accounting_date BETWEEN :fromDate AND :todate ) AND (:lot is null OR bal.lot = :lot) AND (:gid is null OR bal.gid = :gid)   group by bal.refid , bal.lot ", nativeQuery = true)
			Double findBYRefIdWithQtyIssueByDate(@Param("branch") Long branch, @Param("firmid") Long firmid,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot,@Param("gid") Long gid);

		    @Query(value = "select distinct s.refid as itemId, pm.name as itemName, s.gid as godownId, g.name as godownName, s.lot as lot from stbin s left join products_master pm on pm.id = s.refid  left join godown g on g.id = s.gid where s.branch = :branch and (:itemId is null or s.refid = :itemId) and (:godownId is null or s.gid = :godownId) and s.gid is not null "
	        + " and s.accounting_date  between :startDate and :endDate ",nativeQuery = true)		    
			List<GoDownSummaryList> findGodownProductFromStbin(@Param("branch") Long branch,@Param("itemId") Long itemId,@Param("godownId") Long godownId,@Param("startDate") Date startDate,@Param("endDate")Date endDate);
		    
		    @Query(value = "select sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot  from  stbin bal left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid "
		    		+ "	 WHERE (:branch is null OR bal.branch =:branch) AND  bal.entry_type =:type AND (bal.refid  IN(:refId)) AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.cid = :firmId)  group by trim(q.pf)  ", nativeQuery = true)
		    List<StockQtyWithLot> findByRefidQtyWithLotPf(@Param("branch") Long branch, @Param("type") String type,
		                                              @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
		   
		    @Query(value = "select sum(s.qty * sk.conversion ) as qty , trim(q.pf) as lot   from stbin s  left join stkoillist  sk on sk.refid = s.refid  left join qtyrefid q  on q.refid =s.refid  "
		    		+ "WHERE (:branch is null OR s.branch =:branch) AND (:dept is null or s.dept =:dept) AND (:firmId is null or s.cid = :firmId) AND  s.trntype =:trnType AND s.entry_type =:type AND (s.refid IN(:refId)) "
		    		+ "	AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by  trim(q.pf) ", nativeQuery = true)
		    List<StockQtyWithLot> findBYRecRefWithLotPf(@Param("branch") Long branch, @Param("type") String type,@Param("trnType") String trnType, @Param("dept") Long dept,  
		            @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
		    
		    @Query(value = "select sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot from stbin bal left join material_issue mi on mi.trn_no = bal.trnno left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid   "
		            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmId is null or bal.cid = :firmId )AND (:branch is null or bal.branch = :branch) AND bal.trntype = :trntype AND bal.entry_type = :type AND (bal.refid IN(:refId)) "
		            + " AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by trim(q.pf) ", nativeQuery = true)
		 List<StockQtyWithLot> findBYInterRefidWithLotPf(@Param("branch") Long branch, @Param("type") String type,@Param("trntype") String trntype,  
		           @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

		    @Query(value = "select  sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot  from stbin bal left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid   "
		            + " WHERE (:branch is null OR bal.branch =:branch) AND bal.trntype = :type  AND (bal.refid IN(:refId)) AND (:firmId is null or bal.cid = :firmId ) AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by  trim(q.pf) ", nativeQuery = true)
		    List<StockQtyWithLot> findBYTotalRefIdWithLotsPf(@Param("branch") Long branch, @Param("type") String type,
		                                             @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate ,@Param("firmId") Long firmId);
		 
		    @Query(value = "select  sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot   from  stbin bal left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid  "
		            + " WHERE (:branch is null OR bal.branch =:branch) AND  bal.entry_type =:type AND ( bal.sjid IN(:sjId)) AND (bal.refid  IN(:refId)) AND bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.cid = :firmId)  group by  trim(q.pf) ", nativeQuery = true)
		    List<StockQtyWithLot> findByRefidQtyWithLotSjidPf(@Param("branch") Long branch, @Param("type") String type,
		                                             @Param("sjId") List<Long> sjId, @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

		    @Query(value = "select sum(s.qty * sk.conversion ) as qty , trim(q.pf) as lot   from stbin s left join stkoillist  sk on sk.refid = s.refid  left join qtyrefid q  on q.refid =s.refid  "
		            + " WHERE (:branch is null OR s.branch =:branch) AND (:dept is null or s.dept =:dept) AND (:firmId is null or s.cid = :firmId) AND  s.trntype =:trnType AND s.entry_type =:type AND (s.sjid IN(:sjId)) AND (s.refid IN(:refId)) "
		            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by  trim(q.pf) ", nativeQuery = true)
		    List<StockQtyWithLot> findBYRecRefWithLotSjidPf(@Param("branch") Long branch, @Param("type") String type,@Param("trnType") String trnType, @Param("dept") Long dept,  
		            @Param("sjId") List<Long> sjId,@Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
			
		    @Query(value = "select sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot  from stbin bal left join material_issue mi on mi.trn_no = bal.trnno left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid  "
		            + " WHERE mi.issue_branch_id != mi.request_branch_id AND  (:firmId is null or bal.cid = :firmId )AND (:branch is null or bal.branch = :branch) AND bal.trntype = :trntype AND bal.entry_type = :type   AND ( bal.sjid IN(:sjId)) AND (bal.refid IN(:refId)) "
		            + " AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by trim(q.pf) ", nativeQuery = true)
		 List<StockQtyWithLot> findBYInterRefidWithLotSjidPf(@Param("branch") Long branch, @Param("type") String type,@Param("trntype") String trntype,  
		            @Param("sjId") List<Long> sjId,  @Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);

		    
		    @Query(value = "select sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot  from stbin bal left join stkoillist s  on s.refid = bal.refid  left join qtyrefid q  on q.refid =bal.refid  "
		            + " WHERE (:branch is null OR bal.branch =:branch) AND bal.trntype = :type  AND (bal.sjid IN(:sjId)) AND (bal.refid IN(:refId)) AND (:firmId is null or bal.cid = :firmId ) AND bal.accounting_date  BETWEEN :fromDate AND :toDate  group by  trim(q.pf) ", nativeQuery = true)
		    List<StockQtyWithLot> findBYTotalRefIdWithLotsSjidPf(@Param("branch") Long branch, @Param("type") String type,
		                                             @Param("sjId") List<Long> sjId,@Param("refId") List<Long> refId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate ,@Param("firmId") Long firmId);
		 
		    @Query(value = "select SUM(s.qty * s.lot) as qty  from stbin s WHERE s.branch =:branch AND s.dept = :dept AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid = :itemIds AND  s.accounting_date  BETWEEN :fromDate AND :toDate  ", nativeQuery = true)
		    Double findBYPackingListSesame(@Param("branch") Long branch,@Param("type") String type, @Param("trnType") String trnType,@Param("itemIds") Long itemIds, @Param("fromDate") Date fromDate,@Param("toDate") Date toDate,@Param("dept") Long dept);

		    @Query(value = "select SUM(s.qty * s.lot) as qty  from stbin s  WHERE s.branch =:branch  AND s.entry_type ='Stock' AND s.refid = :itemIds  AND  s.accounting_date  BETWEEN :fromDate AND :toDate  ", nativeQuery = true)
		    Double findBYPackingListSplit(@Param("branch") Long branch,@Param("itemIds") Long itemIds, @Param("fromDate") Date fromDate,@Param("toDate") Date toDate);

		    @Query(value = "select sum(s.qty * s.lot) as qty , s.refid as itemId  from stbin s  "
		            + " WHERE s.branch =:branch AND s.dept =:dept AND  s.trntype =:trnType AND s.entry_type =:type AND s.refid in (:itemIds) and s.sjid!=:sjid "
		            + " AND s.accounting_date  BETWEEN :fromDate AND :toDate  group by s.refid , s.trntype , s.entry_type ", nativeQuery = true)
		    List<DayEndStockList> findBYQcWithOutJournals(@Param("branch") Long branch, @Param("dept") Long dept,
		                                   @Param("type") String type, @Param("trnType") String trnType,
		                                   @Param("itemIds") List<Long> ids,
		                                   @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("sjid")Long sjid);
}
