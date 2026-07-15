package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.TransactionList;
import com.aria.dto.journalTypeList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Wcshift;

@Repository
public interface WcShiftsRepository extends JpaRepository<Wcshift, Long> {

    @Query(value = "SELECT * FROM wcshift w where w.pshift=:pshift and w.dept =:dept and w.branch =:branch and w.proid=:proId", nativeQuery = true)
    List<Wcshift> findAlls(@Param("proId") Long proId, @Param("dept") Long dept, @Param("branch") Long branch,
                           @Param("pshift") String pshift);

    @Query(value = "SELECT * FROM wcshift w where w.sjid =:journalId  "
            + " AND (:pshiftId is null or  w.pshiftid=:pshiftId ) " + " AND (:dept is null or w.dept =:dept ) "
            + " AND (:branch is null or w.branch =:branch ) " + " AND ( w.estatus <> 'Y' ) "
            + " AND (:proId is null or w.proid=:proId ) "
            + " AND (cast(:shiftDate as date) is null  or w.shiftdate = cast(:shiftDate  as date) ) "
            + " AND (:batchNo is null or w.batchno =:batchNo) limit 1 ", nativeQuery = true)
    Optional<Wcshift> findBy(@Param("journalId") Long journalId, @Param("pshiftId") Long pshiftId,
                             @Param("proId") Long proId, @Param("dept") Long dept, @Param("branch") Long branch,
                             @Param("shiftDate") Date shiftDate, @Param("batchNo") String batchNo);


    @Query(value = "SELECT * FROM wcshift w where w.trnno=:trnNo", nativeQuery = true)
    Optional<Wcshift> findByTrnNo(@Param("trnNo") String trnNo);

    @Query(value = "SELECT DISTINCT w.wcid as workCenterId, jmm.machine_name as workCenterName, "
            + " j.name as journalName, w.batchno as batchNo, pmh.name as producedProductName "
            + " FROM wcshift w JOIN journal_machine_mapping jmm ON w.wcid = jmm.machine_id "
            + " JOIN journal j ON w.sjid = j.id JOIN products_master pmh ON j.batchproxid = pmh.id "
            + " WHERE w.trnno = :trnNo", nativeQuery = true)
    List<journalTypeList> getJournalDetails(@Param("trnNo") String trnNo);


    @Query(value = "SELECT DISTINCT w.id as refTrnId, w.trnno as trnNo, :transactionType as trnName, " +
            "             w.branch as issueBranchId, w.dept as issueDeptId, " +
            "             STRING_AGG(DISTINCT CONCAT(w.proid, ' - ', pm.name), ', ') AS refIdProductName  FROM wcshift w " +
            "             LEFT JOIN journal_entry je  on je.trnno = w.trnno JOIN products_master pm ON w.proid = pm.id " +
            "             WHERE (je.sal_status IS NULL OR je.sal_status <> 'Completed') and w.trnno IS NOT NULL " +
            "              AND w.shiftdate = :shiftDate  " +
            "             AND w.branch = :branch AND w.dept = :dept  " +
            "             GROUP BY w.id, w.trnno, w.branch, w.dept ORDER by w.id DESC ", nativeQuery = true)
    List<TransactionList> getTransactionListFilterByJOURNAL(@Param("shiftDate") Date shiftDate, @Param("transactionType") String transactionType,
                                                            @Param("branch") Long branch, @Param("dept") Long dept);




}
