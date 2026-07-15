package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.TransactionList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.GoDownExternal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoDownExternalRepository extends JpaRepository<GoDownExternal, Long> {

    Optional<GoDownExternal> findByTrnNo(String trnNo);


    @Query(value = " SELECT * FROM godownexternal m " +
            " WHERE (:status is null OR m.status =:status )  " +
            " AND (:branchId is null or m.from_branch_id =:branchId)  " +
            " AND (:firmId is null or m.firm_Id =:firmId)  " +
            " AND (:trnNo is null OR m.trn_no=:trnNo ) " +
            " AND (cast(:startDate as date) is null or ( DATE(m.dispatch_on) >= :startDate)) " +
            " AND (cast(:endDate as date) is null or ( DATE(m.dispatch_on) <= :endDate)) ", nativeQuery = true)
    Page<GoDownExternal> findByStatusOrderByIdDesc(@Param("status") String status,
                                                   @Param("branchId") Long branchId,
                                                   @Param("firmId") Long firmId,
                                                   @Param("trnNo") String trnNo,
                                                   @Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   Pageable pageable);


    @Query(value = " SELECT * FROM godownexternal m " +
            " WHERE (m.status IN(:status) )  " +
            " AND (:branchId is null or m.from_branch_id =:branchId)  " +
            " AND (:firmId is null or m.firm_Id =:firmId)  " +
            " AND (:trnNo is null OR m.trn_no=:trnNo ) " +
            " AND (cast(:startDate as date) is null or ( DATE(m.dispatch_on) >= :startDate) ) "+
            " AND (cast(:endDate as date) is null or ( DATE(m.dispatch_on) <= :endDate)) ", nativeQuery = true)
    Page<GoDownExternal> findByStatusInOrderByIdDesc(@Param("status") List<String> status,
                                                     @Param("branchId") Long branchId,
                                                     @Param("firmId") Long firmId,
                                                     @Param("trnNo") String trnNo,
                                                     @Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     Pageable pageable);


    @Query(value = "SELECT * FROM goDownExternal m " +
            " WHERE (:branchId is null or m.from_branch_id =:branchId)  " +
            " AND (:trnNo is null OR m.trn_no=:trnNo ) " +
            " AND (:firmId is null or m.firm_Id =:firmId)  " +
            " AND (cast(:startDate as date) is null or ( DATE(m.dispatch_on) >= :startDate)) "+
            " AND (cast(:endDate as date) is null or ( DATE(m.dispatch_on) <= :endDate)) " +
            " order by m.id desc ", nativeQuery = true)
    Page<GoDownExternal> findByAll(@Param("firmId") Long firmId,
                                   @Param("branchId") Long branchId,
                                   @Param("trnNo") String trnNo,
                                   @Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   Pageable pageable);


    @Query(value = " SELECT m.id as refTrnId , m.trn_no as trnNo, :transactionType as trnName, m.issue_branch_id as issueBranchId, " +
            "m.from_branch_id as requestBranchId, m.issue_dept_id as issueDeptId, m.from_dept_id as requestDeptId, " +
            "m.dispatch_on as trnDate FROM godownexternal m " +
            "left join  request_items_godown rig ON rig.godown_external_id = m.id " +
            "WHERE m.status<>'Pending' AND ( CAST(:shiftDate as date) is null or DATE(m.dispatch_on) = :shiftDate ) " +
            "AND (:branchId is null or m.issue_branch_id =:branchId) AND (:deptId is null or m.issue_dept_id =:deptId) " +
            "AND m.trn_no IS NOT NULL AND (rig.sal_status IS NULL  or rig.sal_status <> 'Completed' ) " +
            "GROUP BY   m.id , m.trn_no , m.issue_branch_id , m.from_branch_id, m.issue_dept_id , m.from_dept_id " +
            "order by m.id ASC", nativeQuery = true)
    List<TransactionList> getTransactionListFilterByEXTOUT(@Param("shiftDate") Date shiftDate,
                                                           @Param("transactionType") String transactionType,
                                                           @Param("branchId") Long branch, @Param("deptId") Long deptId);


    @Query(value = " SELECT m.id as refTrnId , m.trn_no as trnNo, :transactionType as trnName, m.issue_branch_id as issueBranchId, " +
            " m.from_branch_id as requestBranchId, m.issue_dept_id as issueDeptId, m.from_dept_id as requestDeptId, " +
            " m.dispatch_on as trnDate FROM godownexternal m " +
            " LEFT JOIN stbin s ON s.trnno = m.trn_no AND s.trntype ='R' LEFT JOIN products_master pm ON s.refid = pm.id " +
            " WHERE m.status='completed' AND ( CAST(:shiftDate as date) is null or DATE(m.dispatch_on) = :shiftDate ) " +
            " AND (:branchId is null or m.from_branch_id =:branchId) AND (:deptId is null or m.from_dept_id =:deptId) " +
            " AND m.trn_no IS NOT NULL AND (s.sal_status IS NULL  or s.sal_status <> 'Completed' ) " +
            " GROUP BY   m.id , m.trn_no , m.issue_branch_id , m.from_branch_id, m.issue_dept_id , m.from_dept_id " +
            " order by m.id ASC", nativeQuery = true)
    List<TransactionList> getTransactionListFilterByEXTIN(@Param("shiftDate") Date shiftDate,
                                                          @Param("transactionType") String transactionType,
                                                          @Param("branchId") Long branch, @Param("deptId") Long deptId);
}
