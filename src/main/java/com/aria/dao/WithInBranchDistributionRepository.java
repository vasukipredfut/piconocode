package com.aria.dao;

import com.aria.dto.IntraBranchReceiptList;
import com.aria.model.OtherBranchIssue;
import com.aria.model.WithInBranchDistribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface WithInBranchDistributionRepository extends JpaRepository<WithInBranchDistribution, Long> {


    @Query(value = "Select ibr.id as id, max(ibr.hup_id) as hupId , max(ibr.request_id) as requestId, max(ibr.docket_no) as docketNo , "
            + " max(ibr.status) as status, max(ibr.issue_date) as issueDate , rb.name as requestFromBranch , "
            + " rd.name as requestFromDepartment, ib.name as issueFromBranch , id.name as issueFromDepartment, "
            + " ied.employeename as issuePerson, max(ibr.issue_id) as issueId from with_in_branch_distribution ibr "
            + " LEFT JOIN branch rb ON rb.id = ibr.request_branch_id LEFT JOIN department_details rd ON rd.id = ibr.request_dept_id "
            + " LEFT JOIN branch ib ON ib.id = ibr.issue_branch_id LEFT JOIN department_details id ON id.id = ibr.issue_dept_id "
            + " LEFT JOIN employee_details ied ON ied.id = ibr.user_id "
            + " WHERE (:hupId is null or (lower(ibr.hup_id) LIKE ('%' || lower(:hupId) || '%')) )"
            + " AND (:docketNo is null or (lower(ibr.docket_no) LIKE ('%' || lower(:docketNo) || '%')) )"
            + " AND (:requestDeptName is null or (lower(rd.name) LIKE ('%' || lower(:requestDeptName) || '%')) )"
            + " AND (:issueBranchName is null or (lower(ib.name) LIKE ('%' || lower(:issueBranchName) || '%')) ) "
            + " AND (:requestBranchId is null or ibr.request_branch_id=:requestBranchId ) "
            + " AND (:firmId is null or ibr.request_org_id=:firmId ) "
            + " AND (:deptId is null or ibr.request_dept_id=:deptId ) "
            + " AND (ibr.issue_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )"
            + " AND (:status is null or (lower(ibr.status) LIKE ('%' || lower(:status) || '%'))) GROUP By ibr.id, rb.name, rd.name, ib.name, id.name,  ied.employeename ORDER BY ibr.id desc", nativeQuery = true)
    Page<IntraBranchReceiptList> withInBranchDistributionList(@Param("firmId") Long firmId,
                                                              @Param("requestBranchId") Long requestBranchId,
                                                              @Param("deptId") Long deptId,
                                                              @Param("hupId") String hupId,
                                                              @Param("docketNo") String docketNo,
                                                              @Param("requestDeptName") String requestDeptName,
                                                              @Param("issueBranchName") String issueBranchName,
                                                              @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
                                                              @Param("status") String status, Pageable pageable);


    @Query(value = "SELECT * FROM with_in_branch_distribution ob WHERE ob.request_id =:requestId AND ob.issue_id=:issueId AND ob.trn_no =:trnNo ", nativeQuery = true)
    Optional<WithInBranchDistribution> findBy(@Param("requestId") Long requestId,
                                              @Param("issueId") Long issueId,
                                              @Param("trnNo") String trnNo);
}
