package com.aria.dao;


import com.aria.dto.TransactionList;
import com.aria.dto.WithInBranchReceiveList;
import com.aria.model.WithInBranchReceive;
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
public interface WithInBranchReceiveRepository extends JpaRepository<WithInBranchReceive, Long> {


    @Query(value = "select wi.id as id,  string_agg (ri.item_name,',') as itemNames,  max(wi.trn_no)  as trnNo, "
    		+ " max(wi.request_id) as requestId, max(wi.request_date) as requestDate , max(wi.status) as status,  rb.name as requestFromBranch , "
    		+ " rd.name as requestFromDepartment, ib.name as issueFromBranch , id.name as issueFromDepartment "
    		+ " from with_in_branch_receive wi LEFT JOIN branch rb ON rb.id = wi.request_branch_id LEFT JOIN department_details rd "
    		+ " ON rd.id = wi.request_dept_id LEFT JOIN branch ib ON ib.id = wi.issue_branch_id "
    		+ " LEFT JOIN department_details id ON id.id = wi.issue_dept_id LEFT JOIN within_branch_receive_items ri ON ri.within_id = wi.id "
    		+ " WHERE (:mrnNo is null OR CAST(wi.trn_no AS TEXT) LIKE ('%' || CAST(:mrnNo AS TEXT) || '%')) "
            + " AND (:itemName is null or (lower(ri.item_name) LIKE ('%' || lower(:itemName) || '%')) ) "
            + " AND (:firmId is null or wi.request_org_id=:firmId) AND (:branch is null or rb.id=:branch)  AND (:dept is null or rd.id=:dept) "
            + " AND (:requestDeptName is null or (lower(rd.name) LIKE ('%' || lower(:requestDeptName) || '%')) )"
            + " AND (:issueDeptName is null or (lower(id.name) LIKE ('%' || lower(:issueDeptName) || '%')) )"
            + " AND (wi.request_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )"
            + " AND (:status is null or (lower(wi.status) LIKE ('%' || lower(:status) || '%'))) GROUP By wi.id, rb.name, rd.name , ib.name, id.name ORDER BY wi.id desc", nativeQuery = true)
    Page<WithInBranchReceiveList> withInBranchReceiveList(@Param("firmId") Long firmId, @Param("branch") Long branch, @Param("dept") Long dept,
            @Param("mrnNo") String mrnNo, @Param("itemName") String itemName, @Param("requestDeptName") String requestDeptName, @Param("issueDeptName") String issueDeptName,
                                                    @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("status") String status,Pageable pageable);

    @Query("SELECT w FROM WithInBranchReceive w JOIN FETCH w.withInBranchReceiveItems WHERE w.id = :id")
	Optional<WithInBranchReceive> findByIdWithItems(long id);

    @Query(value = "SELECT * FROM with_in_branch_receive ob WHERE ob.request_id =:requestId AND ob.issue_id=:issueId AND ob.trn_no =:trnNo ", nativeQuery = true)
    Optional<WithInBranchReceive> findBy(@Param("requestId") Long requestId,
                                      @Param("issueId") Long issueId,
                                      @Param("trnNo") String trnNo);

    @Query(value = " SELECT m.id as refTrnId , m.trn_no as trnNo, :transactionType as trnName, m.issue_branch_id as issueBranchId, " +
            "m.request_branch_id as requestBranchId, m.issue_dept_id as issueDeptId, m.request_dept_id as requestDeptId, " +
            "m.received_date as trnDate FROM with_in_branch_receive m " +
            "left join  within_branch_receive_items s ON s.within_id = m.id " +
            "WHERE m.request_branch_id = m.issue_branch_id " +
            "AND ( CAST(:shiftDate as date) is null or m.received_date = :shiftDate ) " +
            "AND (:branch is null or m.request_branch_id = :branch)" +
            "AND (:dept is null or m.request_dept_id = :dept ) " +
            "AND m.trn_no IS NOT NULL AND (s.sal_status IS NULL  or s.sal_status <> 'Completed' ) " +
            "GROUP BY   m.id ,m.trn_no , m.issue_branch_id , m.request_branch_id, m.issue_dept_id , m.request_dept_id", nativeQuery = true)
    List<TransactionList> getTransactionListFilterByINTRA(@Param("shiftDate") Date shiftDate,
                                                          @Param("transactionType") String transactionType,
                                                          @Param("branch") Long branch, @Param("dept") Long dept);




    Optional<WithInBranchReceive> findByTrnNo(@Param("trnNo") String trnNo);
}
