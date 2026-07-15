package com.aria.dao;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.BillInward;
import com.aria.model.Cluster;
import com.aria.model.MaterialRequest;
import com.aria.model.Wcshift;

@Repository
public interface TransactionRepository extends JpaRepository<MaterialRequest, Long> {
/*

    @Query("SELECT tr.id, tr.trnno, tr.refid, tr.reftable FROM MaterialRequest tr WHERE tr.trndept = :dept AND tr.trnbranch = :branch AND tr.trnst BETWEEN :startTime AND :endTime AND tr.trnstatus = 'Y'")
    List<MaterialRequest> findIntraMaterialMovementResults(
            @Param("dept") String dept,
            @Param("branch") String branch,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    @Query("SELECT tr.id, tr.trnno, tr.refid, tr.reftable FROM MaterialRequest tr WHERE tr.fmbranch <> :branch AND tr.todept = :dept AND tr.tobranch = :branch AND tr.trnst BETWEEN :startTime AND :endTime AND tr.trnstatus = 'Y'")
    List<MaterialRequest> findInterMaterialMovementInwardResults(
            @Param("dept") String dept,
            @Param("branch") String branch,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    @Query("SELECT tr.id, tr.trnno, tr.refid, tr.reftable FROM MaterialRequest tr WHERE tr.fmdept = :dept AND tr.fmbranch = :branch AND tr.fmist BETWEEN :startTime AND :endTime AND tr.tobranch <> :branch AND tr.trnstatus = 'Y'")
    List<MaterialRequest> findInterMaterialMovementOutwardResults(
            @Param("dept") String dept,
            @Param("branch") String branch,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);

    @Query("SELECT b.id, b.party, b.billno, b.billdt, b.trnno, b.ldate, b.lslno FROM BillInward b WHERE b.ldate = :trndate AND b.vtype = 'DP' AND b.branch = :branch")
    List<BillInward> findStockInwardPartyPurchase1Results(
            @Param("trndate") Date trndate,
            @Param("branch") String branch);

    @Query("SELECT b.id, b.party, b.billno, b.billdt, b.trnno, b.ldate, b.lslno FROM BillInward b WHERE b.bentdt = :trndate AND b.vtype = ''")
    List<BillInward> findStockInwardPartyPurchase2Results(@Param("trndate") Date trndate);

    @Query("SELECT w.id, w.sjid, w.trnno FROM WcShiftsDto w WHERE w.shiftdate = :trndate AND w.pshift = :pshiftid AND w.dept = :dept AND w.branch = :branch")
    List<Wcshift> findStockJournalProductionProcessResults(
            @Param("trndate") Date trndate,
            @Param("pshiftid") String pshiftid,
            @Param("dept") String dept,
            @Param("branch") String branch);

    @Query("SELECT c.id FROM Cluster c WHERE c.department = :dept AND c.branch = :branch")
    List<Cluster> findDuringQCInProcessResults(
            @Param("dept") String dept,
            @Param("branch") String branch);

    @Query("SELECT pd.id, pd.schid, pd.schname, pd.batchno FROM ProcessData pd WHERE pd.shiftdate = :trndate AND pd.cluster = :cid")
    List<ProcessData> findIncentiveResults(
            @Param("trndate") Date trndate,
            @Param("cid") String cid);


    @Query("SELECT tr.trnno, tr.trnName, tr.trndate, br1.branchName AS frmBranch, br2.branchName AS toBranch, dept1.deptName AS fromDept, dept2.deptName AS toDept, item.qty, tr.refid " +
            "FROM MaterialRequest tr " +
            "JOIN Branch br1 ON tr.fmbranch = br1.branchId " +
            "JOIN Branch br2 ON tr.tobranch = br2.branchId " +
            "JOIN Dept dept1 ON tr.fmdept = dept1.deptId " +
            "JOIN Dept dept2 ON tr.todept = dept2.deptId " +
            "WHERE tr.fmbranch <> :branch AND tr.todept = :dept AND tr.tobranch = :branch" +
            "AND ps.shiftId = :shiftId AND ps.shiftdate = :shiftdate")
    List<MaterialRequest> getTransactionDetailsQuery(
            @Param("shiftId") Long shiftId,
            @Param("shiftDate") Date shiftDate,
            @Param("transactionType") String transactionType,
            @Param("branch") String branch,
            @Param("dept") String dept
    );
*/


}
