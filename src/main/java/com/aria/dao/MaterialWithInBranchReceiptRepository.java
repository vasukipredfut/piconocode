package com.aria.dao;

import com.aria.dto.TransactionList;
import com.aria.dto.WithInBranchReceiptList;
import com.aria.model.MaterialWithInBranchReceipt;
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
public interface MaterialWithInBranchReceiptRepository extends JpaRepository<MaterialWithInBranchReceipt, Long> {


    @Query(value = "select wi.id as id,  string_agg (ri.item_name,',') as itemNames,   max(wi.trn_no) as trnNo ,  wi.request_id as requestId, wi.request_date as requestDate , wi.status as status,  rb.name as requestFromBranch , rd.name as requestFromDepartment, ib.name as issueFromBranch , id.name as issueFromDepartment from material_within_branch_receipt wi LEFT JOIN branch rb ON rb.id = wi.request_branch_id LEFT JOIN department_details rd " +
            "ON rd.id = wi.request_dept_id LEFT JOIN branch ib ON ib.id = wi.issue_branch_id LEFT JOIN department_details id ON id.id = wi.issue_dept_id LEFT JOIN within_branch_receipt_items ri ON ri.within_id = wi.id "
            + "WHERE (:mrnNo is null OR CAST(wi.trn_no AS TEXT) LIKE ('%' || CAST(:mrnNo AS TEXT) || '%')) "
            + "AND (:itemName is null or (lower(ri.item_name) LIKE ('%' || lower(:itemName) || '%')) )"
            + "AND (:requestBranchName is null or (lower(rb.name) LIKE ('%' || lower(:requestBranchName) || '%')) )"
            + "AND (:issueBranchName is null or (lower(ib.name) LIKE ('%' || lower(:issueBranchName) || '%')) )"
            + "AND (wi.request_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) GROUP By wi.id, rb.name, rd.name , ib.name, id.name ORDER BY wi.id desc", nativeQuery = true)
    Page<WithInBranchReceiptList> withInBranchReceiveList(@Param("mrnNo") String mrnNo, @Param("itemName") String itemName, @Param("requestBranchName") String requestBranchName, @Param("issueBranchName") String issueBranchName,
                                                          @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);




    Optional<MaterialWithInBranchReceipt> findByTrnNo(@Param("trnNo") String trnNo);
}
