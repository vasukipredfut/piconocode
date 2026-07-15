package com.aria.dao;

import com.aria.dto.GDCDetailsList;
import com.aria.dto.OtherBranchIssueList;
import com.aria.model.OtherBranchIssue;
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
public interface OtherBranchIssueRepository extends JpaRepository<OtherBranchIssue, Long> {


    @Query(value = "select obi.id as id, obi.material_with_in_branch_id as materialWithInBranchId , obi.status as status, obi.trn_no as trnNo , rb.name as requestFromBranch , rd.name as requestFromDepartment, ib.name as issueFromBranch , id.name as issueFromDepartment,obi.issue_id as materialIssueId from other_branch_issue obi LEFT JOIN branch rb ON rb.id = obi.request_branch_id LEFT JOIN department_details rd "
            + "ON rd.id = obi.request_dept_id LEFT JOIN branch ib ON ib.id = obi.issue_branch_id LEFT JOIN department_details id ON id.id = obi.issue_dept_id "
            + "WHERE  (:mrnNo is null or CAST(obi.trn_no as TEXT) LIKE (cast(:mrnNo as TEXT) || '%')) "
            + "AND (:firmId is null or obi.org_id=:firmId) "
            + "AND (:issueBranchId is null or obi.issue_branch_id=:issueBranchId) "
            + "AND (:requestBranchName is null or (lower(rb.name) LIKE ('%' || lower(:requestBranchName) || '%')) )"
            + "AND (:issueDeptName is null or (lower(id.name) LIKE ('%' || lower(:issueDeptName) || '%')) )"
            + "AND (obi.dispatch_on BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) "
            + "AND (:status is null or (lower(obi.status) LIKE ('%' || lower(:status) || '%'))) ORDER BY obi.id desc", nativeQuery = true)
    Page<OtherBranchIssueList> otherBranchIssueList( @Param("firmId") Long firmId, @Param("issueBranchId") Long issueBranchId,
                                                    @Param("mrnNo") String mrnNo, @Param("requestBranchName") String requestBranchName,
                                                    @Param("issueDeptName") String issueDeptName,
                                                    @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("status") String status, Pageable pageable);


    @Query(value = "SELECT * FROM other_branch_issue ob WHERE ob.request_id =:requestId AND ob.issue_id=:issueId AND ob.trn_no =:trnNo ", nativeQuery = true)
    Optional<OtherBranchIssue> findBy(@Param("requestId") Long requestId,
                                      @Param("issueId") Long issueId,
                                      @Param("trnNo") String trnNo);







    @Query(value = " SELECT oi.gdc_no as gdcNo, oi.issue_branch_id as issueBranchId, request_branch_id as requestBranchId, "
            + " max(request_id) as requestId FROM other_branch_issue oi where gdc_no  = :gdcNo "
            + " GROUP BY  oi.gdc_no , oi.issue_branch_id , request_branch_id   ", nativeQuery = true)
    List<GDCDetailsList> gdcList(@Param("gdcNo") String gdcNo);


    @Query(value = " SELECT * FROM other_branch_issue oi WHERE gdc_no = :gdcNo "
            + "   AND  (:issueBranchId is null OR oi.issue_branch_id =:issueBranchId ) AND (:reqBranchId is null OR request_branch_id=:reqBranchId)   ", nativeQuery = true)
    List<OtherBranchIssue> findBy(@Param("gdcNo") String gdcNo,
                                      @Param("issueBranchId") Long issueBranchId ,
                                      @Param("reqBranchId") Long reqBranchId);



    @Query(value = " SELECT * FROM other_branch_issue oi WHERE id in(:ids) ", nativeQuery = true)
    List<OtherBranchIssue> findBy(@Param("ids") List<Long> ids);

}
