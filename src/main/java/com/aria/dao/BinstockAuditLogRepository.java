package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;


import com.aria.dto.ProcessReportProductLotWithStockList;
import com.aria.dto.StockQtyWithLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.DayEndStockList;
import com.aria.dto.StockLedger;
import com.aria.model.BinstockAuditLog;

@Repository
public interface BinstockAuditLogRepository extends JpaRepository<BinstockAuditLog, Long> {

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE bi.item_id=:itemId AND bi.bill_id =:billId AND bi.branch_id =:branch AND bi.entry_type =:entryType order by id desc limit 1 ", nativeQuery = true)
    Optional<BinstockAuditLog> findBy(@Param("itemId") Long itemId, @Param("billId") Long billId,
                                      @Param("branch") Long branch, @Param("entryType") String entryType);

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE bi.item_id=:itemId AND bi.bill_id =:billId AND bi.branch_id =:branch AND bi.entry_type =:entryType AND bi.qty =:allotedQty AND bi.lot =:lot", nativeQuery = true)
    Optional<BinstockAuditLog> findBy(@Param("itemId") Long itemId, @Param("billId") Long billId,
                                      @Param("branch") Long branch, @Param("entryType") String entryType, @Param("allotedQty") Double allotedQty,
                                      @Param("lot") Double lot);

    @Query(value = "SELECT bi.id as id, bi.accouting_date as accountingDate, bi.update_on as txnDate, bi.lot as lot, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,  bi.trn_type as trnType, bi.qty as qty, bi.balance as balance FROM binstock_audit_log bi WHERE "
            + " (:productId is null or bi.item_id = :productId) " + "AND (:lot is null or bi.lot = :lot) "
            + "AND (:companyId is null or bi.firm_id = :companyId) "
            + "AND (:branchId is null or bi.branch_id = :branchId) "
            + "AND (cast(:start as date) is null or (bi.accouting_date >=:start)) "
            + "AND (cast(:end as date) is null or (bi.accouting_date <=:end)) ORDER BY bi.id ASC", nativeQuery = true)
    Page<StockLedger> getStockLedger(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                     @Param("productId") Long productId, @Param("lot") Double lot, @Param("start") Date start,
                                     @Param("end") Date end, Pageable pageable);


    /*@Query(value = " SELECT max(bi.id) as id, max(bi.accouting_date) as accountingDate, max(bi.update_on) as txnDate, " +
            " bi.lot as lot, bi.trnno as txnNo,  bi.entry_type as type, max(bi.naration) as narration, " +
            " bi.trn_type as trnType, SUM(bi.qty) as qty, max(bi.balance) as balance " +
            " FROM binstock_audit_log bi  " +
            " WHERE (bi.entry_type NOT IN ('OPENING','CLOSING')   )" +
            " AND (:productId is null or bi.item_id = :productId)  " +
            " AND (:lot is null or bi.lot = :lot) " +
            " AND (:companyId is null or bi.firm_id = :companyId) " +
            " AND (:branchId is null or bi.branch_id = :branchId) " +
            " AND (cast(:start as date) is null or (bi.accouting_date >=:start)) " +
            " AND (cast(:end as date) is null or (bi.accouting_date <=:end)) " +
            " GROUP BY bi.trnno, bi.lot, bi.entry_type, bi.trn_type ORDER BY max(bi.id) asc", nativeQuery = true)
    List<StockLedger> getStockLedger(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                     @Param("productId") Long productId, @Param("lot") Double lot, @Param("start") Date start,
                                     @Param("end") Date end);*/

    @Query(value =
            "SELECT MAX(bi.id) AS id, " +
                    "       bi.accouting_date AS accountingDate, " +
                    "       MAX(bi.update_on) AS txnDate, " +
                    "       bi.lot AS lot, " +
                    "       bi.trnno AS txnNo, " +
                    "       bi.entry_type AS type, " +
                    "       MAX(bi.naration) AS narration, " +
                    "       bi.trn_type AS trnType, " +
                    "       SUM(bi.qty) AS qty, " +
                    "       MAX(bi.balance) AS balance " +
                    "FROM binstock_audit_log bi " +
                    "WHERE bi.entry_type NOT IN ('OPENING','CLOSING') " +
                    "AND (:productId IS NULL OR bi.item_id = :productId) " +
                    "AND (:lot IS NULL OR bi.lot = :lot) " +
                    "AND (:companyId IS NULL OR bi.firm_id = :companyId) " +
                    "AND (:branchId IS NULL OR bi.branch_id = :branchId) " +
                    "AND (cast(:start as date) IS NULL or (bi.accouting_date >=:start) )" +
                    "AND (cast(:end as date) IS NULL OR (bi.accouting_date <=:end ) )" +
                    "GROUP BY bi.trnno, bi.lot, bi.entry_type, bi.trn_type , bi.accouting_date " +
                    "ORDER BY MAX(bi.id) ASC",
            nativeQuery = true)
    List<StockLedger> getStockLedger(
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId,
            @Param("productId") Long productId,
            @Param("lot") Double lot,
            @Param("start") Date start,
            @Param("end") Date end);




    @Query(value =
            " SELECT MIN(bi.id) AS id, " +
                    "             bi.accouting_date AS accountingDate, " +
                    "             MAX(bi.update_on) AS txnDate, " +
                    "             bi.lot AS lot, " +
                    "             bi.trnno AS txnNo, " +
                    "             bi.entry_type AS type, " +
                    "             MAX(bi.naration) AS narration, " +
                    "             bi.trn_type AS trnType, " +
                    "             SUM(bi.qty) AS qty             " +
                    " FROM binstock_audit_log bi " +
                    " WHERE bi.entry_type = :entryType " +
                    " AND (:productId IS NULL OR bi.item_id = :productId) " +
                    " AND (:lot IS NULL OR bi.lot = :lot) " +
                    " AND (:companyId IS NULL OR bi.firm_id = :companyId) " +
                    " AND (:branchId IS NULL OR bi.branch_id = :branchId) " +
                    " AND (CAST(:date AS date) IS NULL OR (bi.accouting_date =:date) ) " +
                    " GROUP BY bi.accouting_date,  bi.lot, bi.trnno , bi.item_id, bi.branch_id , " +
                    " bi.firm_id, bi.entry_type, bi.trn_type " +
                    " ORDER BY MIN(bi.id) ASC ",
            nativeQuery = true)
    StockLedger getStockLedger(
            @Param("companyId") Long companyId,
            @Param("branchId") Long branchId,
            @Param("productId") Long productId,
            @Param("lot") Double lot,
            @Param("date") Date date,
            @Param("entryType") String entryType);



    @Query(value = "SELECT bi.id as id, bi.accouting_date as accountingDate, bi.update_on as txnDate, bi.lot as lot, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,  bi.trn_type as trnType, bi.qty as qty, bi.balance as balance FROM binstock_audit_log bi WHERE "
            + " (:productId is null or bi.item_id = :productId) " + "AND (bi.entry_type <> 'OPENING') "
            + "AND (:lot is null or bi.lot = :lot) " + "AND (:companyId is null or bi.firm_id = :companyId) "
            + "AND (:branchId is null or bi.branch_id = :branchId) "
            + "AND (cast(:start as date) is null or (bi.accouting_date >=:start)) "
            + "AND (cast(:end as date) is null or (bi.accouting_date <=:end)) ORDER BY bi.id ASC", nativeQuery = true)
    Page<StockLedger> getStockLedgerNotInOpening(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                                 @Param("productId") Long productId, @Param("lot") Double lot, @Param("start") Date start,
                                                 @Param("end") Date end, Pageable pageable);

    /*
     * @Query(value =
     * "SELECT bi.id, bi.accouting_date as accountingDate, bi.update_on as txnDate, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,  bi.trn_type as trntype, bi.qty as qty, bi.balance as balance FROM binstock_audit_log bi WHERE "
     * + " (:productId is null or bi.item_id = :productId) " +
     * "AND (:companyId is null or bi.firm_id = :companyId) " +
     * "AND (:branchId is null or bi.branch_id = :branchId) " +
     * "AND (cast(:start as date) is null or (bi.accouting_date >=:start)) " +
     * "AND (cast(:end as date) is null or (bi.accouting_date <=:end)) ORDER BY bi.id ASC LIMIT 1"
     * , nativeQuery = true ) StockLedger getStockLedgerOpening(@Param("companyId")
     * Long companyId, @Param("branchId") Long branchId, @Param("productId") Long
     * productId, @Param("start") Date start, @Param("end") Date end );
     */

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE  bi.accouting_date =:date order by accouting_date desc limit 1", nativeQuery = true)
    Optional<BinstockAuditLog> findBy(@Param("date") Date date);

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE bi.item_id=:itemId AND bi.batchno =:batchNo AND bi.branch_id =:branch AND bi.entry_type =:entryType Limit 1", nativeQuery = true)
    Optional<BinstockAuditLog> findBy(@Param("itemId") Long itemId, @Param("batchNo") String batchNo,
                                      @Param("branch") Long branch, @Param("entryType") String entryType);

    @Query(value = "SELECT bi.id, bi.accouting_date as accountingDate, bi.update_on as txnDate, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,  bi.trn_type as trntype, "
            + " bi.qty as qty, bi.balance as balance FROM binstock_audit_log bi WHERE "
            + " (bi.entry_type = 'OPENING') " + " AND (:productId is null or bi.item_id = :productId) "
            + " AND (:lot is null or bi.lot = :lot) " + " AND (:companyId is null or bi.firm_id = :companyId) "
            + " AND (:branchId is null or bi.branch_id = :branchId) "
            + " AND (cast(:start as date) is null or (bi.accouting_date =:start)) ORDER BY bi.id ASC LIMIT 1", nativeQuery = true)
    StockLedger getStockLedgerOpening(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                      @Param("productId") Long productId, @Param("lot") Double lot, @Param("start") Date start);

    @Query(value = "select sum(bal.qty * bal.lot) as qty , bal.item_id as itemId  from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type =:type AND bal.item_id  in (:itemIds) AND bal.accouting_date  BETWEEN :fromDate AND :toDate  group by bal.item_id , bal.trn_type , bal.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBY(@Param("branch") Long branch, @Param("type") String type,
                                 @Param("itemIds") List<Long> ids, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE bi.item_id=:itemId AND bi.batchno =:batchNo AND bi.branch_id =:branch AND bi.trnno =:trnNo AND bi.entry_type =:entryType Limit 1", nativeQuery = true)
    Optional<BinstockAuditLog> findBy(@Param("itemId") Long itemId, @Param("batchNo") String batchNo,
                                      @Param("branch") Long branch, @Param("entryType") String entryType, @Param("trnNo") String trnNo);

    Optional<BinstockAuditLog> findByTrnno(@Param("trnNo") String trnNo);

    @Query(value = "SELECT *  FROM binstock_audit_log bi WHERE bi.trnno=:trnNo", nativeQuery = true)
    List<BinstockAuditLog> findAllByTrnNo(@Param("trnNo") String trnNo);


    @Query(value = "SELECT *  FROM binstock_audit_log bi " +
            " WHERE bi.batch_id=:batchId AND bi.batchno =:batchNo AND bi.entry_type =:trnType ", nativeQuery = true)
    List<BinstockAuditLog> getDeleteLogList(@Param("batchId") Long batchId,
                                            @Param("batchNo") String batchNo ,
                                            @Param("trnType") String trnType );


    @Query(value = "SELECT bi.id as id, bi.accouting_date as accountingDate, bi.update_on as txnDate,         " +
            " bi.lot as lot, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,          " +
            " bi.trn_type as trnType, COALESCE(SUM(bi.qty),0.0) as qty, bi.balance as balance , s.gname as binNo        " +
            " FROM binstock_audit_log bi    " +
            " left join stbin s on s.trnno = bi.trnno AND s.trntype = bi.trn_type    " +
            " AND s.refid = bi.item_id AND s.qty = bi.qty       " +
            " WHERE (bi.entry_type = 'PRODN'  )       " +
            " AND (:productId is null or bi.item_id = :productId)          " +
            " AND (:companyId is null or bi.firm_id = :companyId)       " +
            " AND (:branchId is null or bi.branch_id = :branchId)   " +
            " AND (:lot is null or bi.lot = :lot)    " +
            " AND (cast(:start as date) is null or (bi.accouting_date >=:start))       " +
            " AND (cast(:end as date) is null or (bi.accouting_date <=:end))       " +
            " GROUP BY bi.id, bi.accouting_date, bi.update_on, bi.lot, bi.trnno, bi.entry_type,    " +
            " bi.naration, bi.trn_type, bi.balance, s.gname    " +
            " ORDER BY bi.id asc ", nativeQuery = true)
    List<StockLedger> getProductionLedger(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                          @Param("productId") Long productId, @Param("lot") Double lot, @Param("start") Date start,
                                          @Param("end") Date end);


    @Query(value = "SELECT bi.id as id, bi.accouting_date as accountingDate, bi.update_on as txnDate,         " +
            " bi.lot as lot, bi.trnno as txnNo,  bi.entry_type as type, bi.naration as narration,          " +
            " bi.trn_type as trnType, COALESCE(SUM(bi.qty),0.0) as qty, bi.balance as balance , s.gname as binNo        " +
            " FROM binstock_audit_log bi    " +
            " left join stbin s on s.trnno = bi.trnno AND s.trntype = bi.trn_type    " +
            " AND s.refid = bi.item_id AND s.qty = bi.qty       " +
            " WHERE (bi.entry_type =:entryType )  " +
            " AND (:productId is null or bi.item_id = :productId)          " +
            " AND (:companyId is null or bi.firm_id = :companyId)       " +
            " AND (:branchId is null or bi.branch_id = :branchId)   " +
            " AND (:lot is null or bi.lot = :lot)    " +
            " AND (cast(:accountingDate as date) is null or (bi.accouting_date =:accountingDate))       " +
            " GROUP BY bi.id, bi.accouting_date, bi.update_on, bi.lot, bi.trnno, bi.entry_type,    " +
            " bi.naration, bi.trn_type, bi.balance, s.gname    " +
            " ORDER BY bi.id asc ", nativeQuery = true)
    List<StockLedger> getProductionLedgerOpeningOrClosing(@Param("companyId") Long companyId, @Param("branchId") Long branchId,
                                                          @Param("productId") Long productId, @Param("lot") Double lot,
                                                          @Param("entryType") String entryType, @Param("accountingDate") Date accountingDate);


    @Query(value = "SELECT * FROM binstock_audit_log b where b.trnno=:trnNo", nativeQuery = true)
    List<BinstockAuditLog> findByTrnNo(@Param("trnNo") String trnNo);


    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type =:type AND bal.item_id =:itemId AND bal.accouting_date  BETWEEN :fromDate AND :toDate AND  (:gid is null OR bal.godown_id = :gid) group by bal.item_id , bal.trn_type , bal.entry_type , bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> findBYRefIdWithLot(@Param("branch") Long branch, @Param("type") String type,
                                             @Param("itemId") Long itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("gid") Long gid);





    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type ='OPENING' AND bal.item_id =:itemId AND bal.accouting_date =:date AND bal.lot in (:lots) "
            + " group by bal.item_id ,  bal.entry_type , bal.lot ", nativeQuery = true)
    List<ProcessReportProductLotWithStockList> findBYOpeningStock(@Param("branch") Long branch, @Param("itemId") Long itemId, @Param("lots") List<Double> lots, @Param("date") Date date);



    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type ='CLOSING' AND bal.item_id =:itemId AND bal.accouting_date =:date AND bal.lot in (:lots) "
            + " group by bal.item_id ,  bal.entry_type , bal.lot ", nativeQuery = true)
    List<ProcessReportProductLotWithStockList> findBYClosingStock( @Param("branch") Long branch, @Param("itemId") Long itemId, @Param("lots") List<Double> lots, @Param("date") Date date);
  
    @Query(value = "select sum(bal.qty) as qty   from binstock_audit_log bal  "
            + " WHERE  (:branch is null or bal.branch_id =:branch) AND (:firmid is null or bal.firm_id = :firmid) AND  bal.entry_type =:type AND bal.item_id =:itemId  AND EXTRACT(YEAR FROM bal.accouting_date) = :year "
            + " AND EXTRACT(MONTH FROM bal.accouting_date) = :month  AND (:lot is null OR bal.lot = :lot)AND (:gid is null OR bal.godown_id = :gid) group by bal.item_id  , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQty(@Param("branch")Long branch,@Param("firmid")Long firmid ,@Param("type") String type,@Param("itemId") Long itemId,@Param("year") Long year,@Param("month") Long month,@Param("lot") Double lot,@Param("gid") Long gid);

    
    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type =:type AND bal.item_id IN(:itemId) AND bal.accouting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.firm_id = :firmId) group by bal.item_id , bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> findBYRefIdAndSjidWithLot(@Param("branch") Long branch, @Param("type") String type,
                                             @Param("itemId") List<Long> itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
    @Query(value = "select sum(bal.qty ) as qty , bal.item_id as itemId ,bal.lot as lot from binstock_audit_log bal  "
            + " WHERE bal.branch_id =:branch AND  bal.entry_type =:type AND (:firmId is null or bal.firm_id = :firmId) AND bal.item_id  in (:itemIds) AND bal.lot in (:lot) AND bal.accouting_date  BETWEEN :fromDate AND :toDate  group by bal.item_id , bal.trn_type , bal.entry_type ,bal.lot ", nativeQuery = true)
    List<DayEndStockList> findBYStockRept(@Param("branch") Long branch, @Param("type") String type,
                                 @Param("itemIds") List<Long> ids, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("lot") List<Double> lot,@Param("firmId") Long firmId);
    @Query(value = "select distinct bal.lot as lot, bal.item_id as itemId from binstock_audit_log bal where bal.branch_id = :branchId AND bal.item_id IN (:productId) AND bal.accouting_date = :date ", nativeQuery = true)
    List<DayEndStockList> findByLot(List<Long> productId, Long branchId, Date date);

    @Query(value = "select sum(bal.qty) as qty   from binstock_audit_log bal  "
            + " WHERE  (:branch is null or bal.branch_id =:branch) AND (:firmid is null or bal.firm_id = :firmid) AND  bal.entry_type =:type AND bal.item_id =:itemId  AND ( bal.accouting_date BETWEEN :fromDate AND :todate )  AND (:lot is null OR bal.lot = :lot)AND (:gid is null OR bal.godown_id = :gid) group by bal.item_id  , bal.lot ", nativeQuery = true)
	Double findBYRefIdWithQtyByDate(@Param("branch")Long branch,@Param("firmid")Long firmid ,@Param("type") String type,@Param("itemId") Long itemId,@Param("fromDate") Date fromDate,@Param("todate") Date todate,@Param("lot") Double lot,@Param("gid") Long gid);

    @Query(value ="select sum(bal.qty * bal.lot) as qty , bal.refid as itemId  from stbin bal  left join material_issue mi on mi.trn_no = bal.trnno WHERE mi.issue_branch_id != mi.request_branch_id and mi.issue_branch_id =:branch AND  bal.trntype =:trntype AND bal.entry_type  =:type AND bal.refid in (:itemIds) AND bal.accounting_date   BETWEEN :fromDate AND :toDate  group by bal.refid , bal.trntype , bal.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYUnitIssue(@Param("branch") Long branch, @Param("trntype") String trntype, @Param("type") String type,
                                 @Param("itemIds") List<Long> ids, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value ="select sum(bal.qty * bal.lot) as qty , bal.refid as itemId  from stbin bal  left join material_issue mi on mi.trn_no = bal.trnno  WHERE mi.issue_branch_id != mi.request_branch_id and  mi.request_branch_id =:branch AND  bal.trntype =:trntype AND bal.entry_type  =:type AND bal.refid in (:itemIds) AND bal.accounting_date   BETWEEN :fromDate AND :toDate  group by bal.refid , bal.trntype , bal.entry_type ", nativeQuery = true)
    List<DayEndStockList> findBYUniTReceipt(@Param("branch") Long branch, @Param("trntype") String trntype, @Param("type") String type,
                                 @Param("itemIds") List<Long> ids, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value = "select sum(bal.qty * s.conversion ) as qty , trim(q.pf) as lot  from binstock_audit_log bal  left join stkoillist s  on s.refid = bal.item_id  left join qtyrefid q  on q.refid =bal.item_id "
    		+ " WHERE bal.branch_id =:branch AND  bal.entry_type =:type AND bal.item_id IN(:itemId) AND bal.accouting_date  BETWEEN :fromDate AND :toDate AND  (:firmId is null OR bal.firm_id = :firmId) group by trim(q.pf) ", nativeQuery = true)
    List<StockQtyWithLot> findBYRefIdAndSjidWithLotPf(@Param("branch") Long branch, @Param("type") String type,
                                             @Param("itemId") List<Long> itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("firmId") Long firmId);
    
    @Query(value = "select  COALESCE(SUM( distinct bal.lot), 0) AS lot, bal.item_id as itemId ,bal.branch_id as branchId from binstock_audit_log bal where bal.item_id IN (:productId) AND bal.accouting_date = :date group by bal.item_id , bal.branch_id ", nativeQuery = true)
    List<DayEndStockList> findByLotForall(List<Long> productId, Date date);

    @Query(value = "select sum(bal.qty) as qty , bal.lot as lot  from stbin bal WHERE bal.branch  =:branch AND  bal.entry_type =:type AND bal.refid  =:itemId and bal.accounting_date  BETWEEN :fromDate AND :toDate AND  (:gid is null OR bal.gid = :gid) and bal.trntype =:trntype group by bal.refid , bal.trntype , bal.entry_type , bal.lot ", nativeQuery = true)
    List<StockQtyWithLot> findBYRefIdWithLottran(@Param("branch") Long branch, @Param("type") String type,
                                             @Param("itemId") Long itemId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,@Param("gid") Long gid,@Param("trntype") String trntype);

}
