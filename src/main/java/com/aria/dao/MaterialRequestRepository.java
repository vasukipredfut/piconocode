
package com.aria.dao;

import com.aria.dto.BlendingRequestDetails;
import com.aria.dto.JournalOrMrnNotClosingList;
import com.aria.dto.MaterialRequestList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.MaterialRequest;

import java.util.Date;
import java.util.List;


@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {


    @Query(value = "select mr.id as id, mr.request_name as requestName, mr.request_description as requestDescription, mr.request_date as requestDate, mr.status as status, rb.id as requestFromBranchId, rb.name as requestFromBranch , rd.id as requestFromDepartmentId, rd.name as requestFromDepartment "
            + " from material_request mr LEFT JOIN branch rb ON rb.id = mr.branch_id LEFT JOIN department_details rd ON rd.id = mr.dept_id  "
            + " WHERE (:firmId is null OR mr.org_id=:firmId) AND (rb.id=:branchId) AND (rd.id =:deptId) "
            + " AND (:id is null or CAST(mr.id as TEXT)  LIKE (:id || '%')) "
            + " AND (:description is null or (lower(mr.request_description) LIKE ('%' || lower(:description) || '%')) )"
            + " AND (:branchName is null or (lower(rb.name) LIKE ('%' || lower(:branchName) || '%')) )"
            + " AND (:deptName is null or (lower(rd.name) LIKE ('%' || lower(:deptName) || '%')) )"
            + " AND (cast(:requestDate as date) is null or (mr.request_date =:requestDate))"
            + " AND (:status is null or (lower(mr.status) LIKE ('%' || lower(:status) || '%'))) order by mr.id desc ", nativeQuery = true)
    Page<MaterialRequestList> materialRequestList(@Param("firmId") Long firmId, @Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("id") Long id, @Param("description") String description, @Param("branchName") String branchName, @Param("deptName") String deptName,
                                                  @Param("requestDate") Date requestDate, @Param("status") String status, Pageable pageable);

    @Query(value = "select * from material_request where branch_id=:branchId and dept_id=:deptId", nativeQuery = true)
    List<MaterialRequest> findByRequestById(Long branchId, Long deptId);

   /* @Query(value = " SELECT mi.id as issueId, mi.request_id as requestId , mi.issue_date as issueDate,  " +
            "mi.request_branch_id as requestBranchId, max(rb.name) as requestBranchName,      " +
            "max(mr.org_id) as orgId ,  mi.trn_no as trnNo , mi.request_date as requestDate , " +
            "mi.issue_branch_id as issueBranchId,     max(ib.name) as issueBranchName,  " +
            "mi.issue_dept_id as issueDeptId, max(idd.name) as issueDeptName , " +
            "mi.request_dept_id as requestDeptId, max(rdd.name) as requestDeptName , " +
            "mi.status as issueStatus   , max(wi.received_date) as withInBranchReceiveDate, " +
            "max(wi.status) as withInBranchStatus,  max(ob.status) as otherBranchStatus,  " +
            "max(ob.received_date) as otherBranchReceiveDate      " +
            "FROM material_issue mi left join other_branch_issue obi on obi.issue_id  = mi.id  " +
            "left join stbin s on s.trnno = mi.trn_no      " +
            "left join branch ib  on ib.id = mi.issue_branch_id      " +
            "left join branch rb on rb.id = mi.request_branch_id   " +
            "left join department_details idd on idd.id = mi.issue_dept_id   " +
            "left join department_details rdd on rdd.id = mi.request_dept_id   " +
            "left join material_request mr on mr.id = mi.request_id      " +
            "left join with_in_branch_receive wi on mr.id = wi.request_id      " +
            "left join other_branch_receipt ob on mr.id = ob.request_id      " +
            "where (mi.request_branch_id != mi.issue_branch_id )  " +
            "AND mi.status <> 'REJECTED' AND  obi.status <> 'REJECTED'      " +
            "and mi.trn_no not in (  " +
            "  select s.trnno from stbin s  " +
            "  where DATE(s.accounting_date)=DATE(:requestDate) AND s.trntype='R' AND s.entry_type='MATERIAL_TRANSACTION' " +
            " )     " +
            "AND (cast(:requestDate as date) is null or (mi.issue_date =:requestDate))      " +
            "AND (mr.branch_id =:branchId  OR  mi.issue_branch_id =:branchId )     " +
            "group by mi.id , mi.request_id , mi.issue_date ,  mi.trn_no ,  " +
            " mi.request_date , mi.issue_branch_id , mi.issue_dept_id , mi.request_dept_id", nativeQuery = true)
    List<JournalOrMrnNotClosingList> findBy(@Param("branchId") Long branchId,
                                            @Param("requestDate") Date requestDate);
*/

    @Query(value = " SELECT mi.id as issueId, mi.request_id as requestId , mi.issue_date as issueDate,   " +
            "            mi.request_branch_id as requestBranchId, max(rb.name) as requestBranchName,       " +
            "            max(mr.org_id) as orgId ,  mi.trn_no as trnNo , mi.request_date as requestDate ,  " +
            "            mi.issue_branch_id as issueBranchId,     max(ib.name) as issueBranchName,   " +
            "            mi.issue_dept_id as issueDeptId, max(idd.name) as issueDeptName ,  " +
            "            mi.request_dept_id as requestDeptId, max(rdd.name) as requestDeptName ,  " +
            "            mi.status as issueStatus   , max(wi.received_date) as withInBranchReceiveDate,  " +
            "            max(wi.status) as withInBranchStatus,  max(ob.status) as otherBranchStatus,   " +
            "            max(ob.received_date) as otherBranchReceiveDate       " +
            "            FROM material_issue mi  " +
            "            left join other_branch_issue obi on obi.issue_id  = mi.id   " +
            "            left join stbin s on s.trnno = mi.trn_no       " +
            "            left join branch ib  on ib.id = mi.issue_branch_id       " +
            "            left join branch rb on rb.id = mi.request_branch_id    " +
            "            left join department_details idd on idd.id = mi.issue_dept_id    " +
            "            left join department_details rdd on rdd.id = mi.request_dept_id    " +
            "            left join material_request mr on mr.id = mi.request_id       " +
            "            left join with_in_branch_receive wi on mr.id = wi.request_id       " +
            "            left join other_branch_receipt ob on mr.id = ob.request_id       " +
            "            where (mi.request_branch_id != mi.issue_branch_id )   " +
            "            AND mi.status <> 'REJECTED' AND  obi.status <> 'REJECTED'       " +
            "            and mi.trn_no not in (   " +
            "                      select distinct s.trnno from stbin s " +
            "                       where s.trntype = 'R' and s.entry_type = 'MATERIAL_TRANSACTION' and s.trnno in " +
            "           ( select distinct cmi.trn_no from material_issue cmi where " +
            "                        cmi.request_branch_id <> cmi.issue_branch_id and cmi.issue_date = DATE(:requestDate)) " +
            "           )      " +
            "            AND (cast(:requestDate as date) is null or (mi.issue_date =:requestDate))       " +
            "            AND (mr.branch_id =:branchId  OR  mi.issue_branch_id =:branchId )      " +
            "            group by mi.id , mi.request_id , mi.issue_date ,  mi.trn_no ,   " +
            "            mi.request_date , mi.issue_branch_id , mi.issue_dept_id , mi.request_dept_id", nativeQuery = true)
    List<JournalOrMrnNotClosingList> findBy(@Param("branchId") Long branchId,
                                            @Param("requestDate") Date requestDate);



    @Query(
            value =
                    " SELECT " +
                            "   mi.id AS issueId, " +
                            "   mi.request_id AS requestId, " +
                            "   mi.issue_date AS issueDate, " +
                            "   mi.request_branch_id AS requestBranchId, " +
                            "   MAX(rb.name) AS requestBranchName, " +
                            "   MAX(mr.org_id) AS orgId, " +
                            "   mi.trn_no AS trnNo, " +
                            "   mi.request_date AS requestDate, " +
                            "   mi.issue_branch_id AS issueBranchId, " +
                            "   MAX(ib.name) AS issueBranchName, " +
                            "   mi.issue_dept_id AS issueDeptId, " +
                            "   MAX(idd.name) AS issueDeptName, " +
                            "   mi.request_dept_id AS requestDeptId, " +
                            "   MAX(rdd.name) AS requestDeptName, " +
                            "   mi.status AS issueStatus, " +
                            "   MAX(wi.received_date) AS withInBranchReceiveDate, " +
                            "   MAX(wi.status) AS withInBranchStatus, " +
                            "   MAX(ob.status) AS otherBranchStatus, " +
                            "   MAX(ob.received_date) AS otherBranchReceiveDate " +
                            " FROM material_issue mi " +
                            " LEFT JOIN other_branch_issue obi ON obi.issue_id = mi.id " +
                            " LEFT JOIN stbin s ON s.trnno = mi.trn_no " +
                            " LEFT JOIN branch ib ON ib.id = mi.issue_branch_id " +
                            " LEFT JOIN branch rb ON rb.id = mi.request_branch_id " +
                            " LEFT JOIN department_details idd ON idd.id = mi.issue_dept_id " +
                            " LEFT JOIN department_details rdd ON rdd.id = mi.request_dept_id " +
                            " LEFT JOIN material_request mr ON mr.id = mi.request_id " +
                            " LEFT JOIN with_in_branch_receive wi ON mr.id = wi.request_id " +
                            " LEFT JOIN other_branch_receipt ob ON mr.id = ob.request_id " +
                            " WHERE mi.request_branch_id <> mi.issue_branch_id " +
                            "   AND mi.status <> 'REJECTED' " +
                            "   AND obi.status <> 'REJECTED' " +
                            "   AND mi.trn_no NOT IN ( " +
                            "       SELECT DISTINCT s.trnno " +
                            "       FROM stbin s " +
                            "       WHERE s.trntype = 'R' " +
                            "         AND s.entry_type = 'MATERIAL_TRANSACTION' " +
                            "         AND s.trnno IN ( " +
                            "             SELECT DISTINCT cmi.trn_no " +
                            "             FROM material_issue cmi " +
                            "             WHERE cmi.request_branch_id <> cmi.issue_branch_id " +
                            "               AND cmi.issue_date = DATE(:requestDate) " +
                            "         ) " +
                            "   ) " +
                            "   AND (cast(:requestDate as date) is null or (mi.issue_date =:requestDate)) " +
                            "   AND ( mr.branch_id IN (:branchList) " +
                            "         OR mi.issue_branch_id IN (:branchList) ) " +
                            " GROUP BY " +
                            "   mi.id, mi.request_id, mi.issue_date, mi.trn_no, " +
                            "   mi.request_date, mi.issue_branch_id, mi.issue_dept_id, mi.request_dept_id ",
            nativeQuery = true
    )
    List<JournalOrMrnNotClosingList> findBy(
            @Param("branchList") List<Long> branchList,
            @Param("requestDate") Date requestDate
    );



    @Query(value = "select mr.id as requestId, mi.id  as issueId , mr.branch_id as requestBranch, mr.dept_id as requestDept ,"
            + " mi.issue_branch_id as issuebranch , mi.issue_dept_id as issuedept, mi.trn_no as trnNo  from material_request mr  left join"
            + " material_issue mi on mi.request_id = mr.id and mi.request_branch_id = mr.branch_id and mi.request_dept_id = mr.dept_id"
            + " left join issue_items ii on ii.issue_item_id = mi.id where mi.id IS NOT NULL and mi.issue_branch_id IS NOT NULL and "
            + " mi.issue_dept_id IS NOT NULL and mr.request_name ='Blending_Request' and mi.request_id = mr.id "
            + " and mr.request_description ='Blending_Request' and mr.branch_id =:requestBranchId and mr.dept_id =:dept "
            + " and mi.issue_branch_id =:issueBranchId and mi.issue_dept_id =:dept "
            + " group by mi.id, mr.id, mr.branch_id, mr.dept_id, mi.issue_branch_id Limit 1 ", nativeQuery = true)
    BlendingRequestDetails findBy(@Param("requestBranchId") Long requestBranchId,
                                  @Param("dept") Long dept,
                                  @Param("issueBranchId") Long issueBranchId);

}
