
package com.aria.dao;

import com.aria.dto.IntraBranchReceiptList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.IntraBranchReceipt;

import java.util.Date;

@Repository
public interface IntraBranchReceiptRepository extends JpaRepository<IntraBranchReceipt,Long> {


    @Query(value = "select ibr.id as id, ibr.hup_id as hupId , ibr.trn_no as trnNo, ibr.docket_no as docketNo , ibr.status as status, ibr.issue_date as issueDate , rb.name as requestFromBranch , rd.name as requestFromDepartment, ib.name as issueFromBranch , id.name as issueFromDepartment, ied.employeename as issuePerson from intra_branch_receipt ibr "
            + "LEFT JOIN branch rb ON rb.id = ibr.request_branch_id LEFT JOIN department_details rd ON rd.id = ibr.request_dept_id LEFT JOIN branch ib ON ib.id = ibr.issue_branch_id LEFT JOIN department_details id ON id.id = ibr.issue_dept_id LEFT JOIN employee_details ied ON ied.id = ibr.user_id  "
            + "WHERE (:hupId is null or (lower(ibr.hup_id) LIKE ('%' || lower(:hupId) || '%')) )"
            + "AND (:docketNo is null or (lower(ibr.docket_no) LIKE ('%' || lower(:docketNo) || '%')) )"
            + "AND (:requestDeptName is null or (lower(rd.name) LIKE ('%' || lower(:requestDeptName) || '%')) )"
            + "AND (:issueBranchName is null or (lower(ib.name) LIKE ('%' || lower(:issueBranchName) || '%')) )"
            + "AND (ibr.issue_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )  ORDER BY ibr.id desc", nativeQuery = true)
    Page<IntraBranchReceiptList> intraBranchReceiptList(@Param("hupId") String hupId, @Param("docketNo") String docketNo, @Param("requestDeptName") String requestDeptName, @Param("issueBranchName") String issueBranchName,
                                                      @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);


}
