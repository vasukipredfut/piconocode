
package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.IssueTrnCheck;
import com.aria.dto.MrnPendingList;
import com.aria.dto.TransactionList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.MaterialIssueList;
import com.aria.model.MaterialIssue;

import jakarta.transaction.Transactional;

@Repository
public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, Long> {

	@Query(value = "SELECT mi.id as id, max(mi.request_name) as requestName, max(mi.trn_no) as trnNo,   "
			+ "  mi.request_id as requestId, mi.request_date as requestDate , mi.status as status,  rb.name as requestFromBranch ,   "
			+ "  rd.name as requestFromDepartment, ed.employeename as requestedPerson , string_agg(distinct ri.item_name,',') itemNames  "
			+ "  FROM material_issue mi LEFT JOIN request_item ri on ri.request_id = mi.request_id AND ri.branch_id = mi.issue_branch_id AND ri.dept_id = mi.issue_dept_id "
			+ "  LEFT JOIN branch rb ON rb.id = mi.request_branch_id LEFT JOIN department_details rd ON rd.id = mi.request_dept_id  "
			+ "  LEFT JOIN issue_items it ON it.issue_item_id = mi.id LEFT JOIN branch mi_ob ON mi_ob.id = mi.issue_branch_id  "
			+ "  LEFT JOIN department_details mi_od ON mi_od.id = mi.issue_dept_id LEFT JOIN employee_details ed ON ed.id = mi.created_by "
			+ " WHERE (:firmId is null OR mi.org_id=:firmId) AND (mi.issue_branch_id=:branchId) AND (mi.issue_dept_id =:deptId) "
			+ " AND (:requestId is null or cast(mi.request_id as TEXT) LIKE (cast(:requestId as TEXT) || '%')) "
			+ " AND (:itemName is null or (lower(it.item_name) LIKE ('%' || lower(:itemName) || '%')) ) "
			+ " AND (:branchName is null or (lower(rb.name) LIKE ('%' || lower(:branchName) || '%')) ) "
			+ " AND (:deptName is null or (lower(rd.name) LIKE ('%' || lower(:deptName) || '%')) ) "
			+ " AND (mi.request_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )"
			+ " AND (:status is null or (lower(mi.status) LIKE ('%' || lower(:status) || '%'))) GROUP By mi.id, rb.name, rd.name, ed.employeename ORDER BY mi.id desc", nativeQuery = true)
	Page<MaterialIssueList> materialIssueList(@Param("firmId") Long firmId, @Param("branchId") Long branchId, @Param("deptId") Long deptId,
			@Param("requestId") Long requestId, @Param("itemName") String itemName,
			@Param("branchName") String branchName, @Param("deptName") String deptName,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("status") String status, Pageable pageable);

	@Query(value = "select mi.* from material_issue mi join issue_items ii on mi.id = ii.issue_item_id where ii.issued_from_batch =:issuedFromBatch ", nativeQuery = true)
	Optional<MaterialIssue> findBy(String issuedFromBatch);


	@Query(value = "select * from material_issue mi WHERE mi.trn_no=:trnNo", nativeQuery = true)
	Optional<MaterialIssue> findByIssue(@Param("trnNo") String trnNo);

	@Query(value = "select * from material_issue mi WHERE mi.id=:issueId", nativeQuery = true)
	Optional<MaterialIssue> findByIssueId(@Param("issueId") Long issueId);


	@Query(value = "  SELECT mi.trn_no as trnNo, :transactionType as trnName, mi.issue_branch_id as issueBranchId, " +
			" mi.issue_dept_id as issueDeptId, max(s.trn_date) as trnDate " +
			" FROM material_issue mi LEFT JOIN stbin s ON s.trnno = mi.trn_no " +
			" WHERE s.trntype = 'I' AND ( cast(:shiftDate as date) is null or  mi.issue_date = :shiftDate) " +
			" AND (:branch is null or mi.issue_branch_id = :branch ) " +
			" AND (s.sal_status IS NULL or s.sal_status <>'Completed') " +
			" AND (:dept is null or mi.issue_dept_id = :dept ) AND mi.trn_no  IS NOT NULL " +
			" GROUP BY mi.trn_no , mi.issue_branch_id , mi.issue_dept_id ", nativeQuery = true)
	List<TransactionList> getTransactionListFilterBySTK_OUT(@Param("shiftDate") Date shiftDate,
															@Param("transactionType") String transactionType,
															@Param("branch") Long branch, @Param("dept") Long dept);

	@Query(value = " SELECT DISTINCT m.id as refTrnId, m.trn_no as trnNo, :transactionType as trnName, m.issue_branch_id as issueBranchId,  " +
			" m.request_branch_id as requestBranchId, m.issue_dept_id as issueDeptId, m.request_dept_id as requestDeptId, sum(s.qty) as qty , " +
			" s.trn_date as trnDate  FROM material_issue m  LEFT JOIN stbin s ON s.trnno = m.trn_no   " +
			" WHERE m.issue_branch_id = m.request_branch_id AND (cast(:shiftDate as date ) is null or m.issue_date = :shiftDate )  " +
			" AND (:branch is null or m.issue_branch_id = :branch) AND (:dept is null or m.issue_dept_id = :dept )  " +
			" AND m.trn_no IS NOT NULL AND s.entry_type='MATERIAL_TRANSACTION' AND s.trntype = 'I' AND ( m.sal_status IS NULL OR  m.sal_status <> 'Completed') " +
			" GROUP BY m.id, m.trn_no, m.issue_branch_id, m.request_branch_id, m.issue_dept_id, m.request_dept_id, s.trn_date", nativeQuery = true)
	List<TransactionList> getTransactionListFilterByINTER_OUT(@Param("shiftDate") Date shiftDate,
															  @Param("transactionType") String transactionType,
															  @Param("branch") Long branch, @Param("dept") Long dept);




	@Query(value = " SELECT  mi.id, max(mi.org_id) as firmId, max(o.name) as firmName,  mi.trn_no as trnNo, mi.issue_date as trnDate, mi.status as issStatus, " +
			" mi.request_branch_id as requestBranchId, max(rb.name) as requestBranchName,  mi.issue_branch_id as issueBranchId, max(ib.name) as issueBranchName, " +
			" mi.request_dept_id as requestDeptId, max(rd.name) as requestDeptName,  mi.issue_dept_id as issueDeptId, max(ib.name) as issueDeptName, " +
			" ii.item_id as itemId, max(pm.name) as productName,  COALESCE( SUM( CAST (ii.allocated_quantity as DOUBLE PRECISION) ),0.0) as qty,  " +
			" ii.select_lot as lot, ii.issued_from_batch as batchNo ,  " +
			" ( case  " +
			" when max(mi.status) ='Issue' or max(mi.status) ='Pending_Quantity' then 'Issued' else 'Pending'  " +
			" end ) as issueStatus ,  " +
			" ( case  " +
			" when max(obi.status) ='Completed'  then 'Moved' else 'Pending'  " +
			" end ) as otherBranchIssueStatus ,  " +
			"  ( case  " +
			" when max(ibr.status) ='VERIFIED'  then 'Received' else 'Pending'  " +
			" end ) as intraBranchStatus ,  " +
			" ( case  " +
			" when max(wib.status) ='Issued'  then 'Distributed' else 'Pending'  " +
			" end ) as withinBranchStatus ,  " +
			" mi.id as issueId , max(wib.id) as withinBranchDisButionId, max(obi.id) as otherbranchIssueId, max(ibr.id) as intraBranchRecId  " +
			" FROM material_issue mi LEFT JOIN other_branch_issue obi ON obi.issue_id = mi.id AND obi.trn_no = mi.trn_no   " +
			" LEFT JOIN intra_branch_rec ibr ON ibr.trn_no = mi.trn_no LEFT JOIN with_in_branch_distribution wib on wib.issue_id = mi.id AND wib.trn_no = mi.trn_no  " +
			" LEFT JOIN organisation o ON o.id = mi.org_id LEFT JOIN   issue_items ii on ii.issue_item_id = mi.id       " +
			" LEFT JOIN branch ib  ON ib.id = mi.issue_branch_id LEFT JOIN branch rb  ON rb.id = mi.request_branch_id  " +
			" LEFT JOIN department_details id  ON id.id = mi.issue_dept_id LEFT JOIN department_details rd  ON rd.id = mi.request_dept_id   " +
			" LEFT JOIN products_master pm ON ii.item_id = pm.id  LEFT JOIN binstock b  ON b.refid = ii.item_id  AND b.ltrnno = mi.trn_no   " +
			" WHERE  (mi.status <> 'Pending'  ) AND b.locktype ='BU' AND b.lockstatus ='Y' AND ii.issued_from_batch IS NOT NULL   " +
			" AND (:issueBranchId is NULL OR mi.issue_branch_id =:issueBranchId )  " +
			" AND (:requestBranchName is null or (lower(rb.name) LIKE ('%' || lower(:requestBranchName) || '%')))  " +
			" AND (:requestBranchId is NULL OR mi.request_branch_id=:requestBranchId )  " +
			" AND (:issueBranchName is NULL OR ( lower(ib.name) LIKE('%' || lower(:issueBranchName) || '%')))  " +
			" AND ( cast(:shiftDate as date) is null OR mi.accounting_date = :shiftDate )  " +
			" AND (:productName is NULL OR ( lower(pm.name) LIKE('%'|| lower(:productName)  || '%')))  " +
			" AND (:trnNo is NULL OR ( lower(mi.trn_no) LIKE('%' || lower(:trnNo) ||  '%')  ) )  " +
			" GROUP BY mi.id , ii.item_id  , mi.trn_no , mi.issue_date , mi.request_branch_id , mi.issue_branch_id , mi.request_dept_id, " +
			" mi.issue_dept_id, ii.select_lot, ii.issued_from_batch " +
			" ORDER BY mi.id DESC  " , nativeQuery = true)
	Page<MrnPendingList> getMrnPendingList(@Param("requestBranchId") Long requestBranchId,
										   @Param("requestBranchName") String requestBranchName,
										   @Param("issueBranchId") Long issueBranchId,
										   @Param("issueBranchName") String issueBranchName,
										   @Param("shiftDate") Date shiftDate,
										   @Param("trnNo") String trnNo,
										   @Param("productName") String productName, Pageable pageable);



	@Query(value = "select * from material_issue mi WHERE mi.request_id=:requestId AND mi.request_name='Blending_Request' AND mi.status<>'Pending' ", nativeQuery = true)
	List<MaterialIssue> findByRequestId(@Param("requestId") Long requestId);



	@Transactional
	@Modifying
	@Query(value = "update material_issue mi set status='BlendingExpired' FROM blending_request_preprocess_bin_map b " +
			" where mi.status in ('Pending' , 'Pending_Quantity') and mi.request_id = b.type_id and " +
			" b.type ='Blending_Material_Request' and  b.blending_detail_id =:blendingId" , nativeQuery = true)
	public void updateExpiredBlendingRequest(@Param("blendingId") Long blendingId);

	@Query(value = "SELECT mi.id as issueId, mi.request_id as requestId, ii.item_id as itemId, " +
			" ii.select_lot as lot , obr.status as status " +
			" FROM material_issue mi " +
			" LEFT JOIN issue_items ii on ii.issue_item_id = mi.id " +
			" LEFT JOIN other_branch_receipt obr on obr.trn_no = mi.trn_no " +
			" WHERE  mi.request_id = :requestId AND ii.item_id =:itemId AND ii.select_lot =:lot AND ii.active  = 0 " +
			" AND mi.status  NOT IN ('Pending','BlendingExpried','REJECTED','') " +
			" AND ( obr.status IS NULL OR obr.status NOT IN ('Issued') ) " +
			" GROUP BY mi.request_id, ii.item_id, ii.select_lot , mi.id ,  obr.status LIMIT 1",nativeQuery = true)
	IssueTrnCheck findBy(@Param("requestId") Long requestId,
							   @Param("itemId") Long itemId,
							   @Param("lot") String lot);



	@Query(value = "select count(1) from material_issue mi WHERE mi.request_id=:requestId ", nativeQuery = true)
	Long getCount(@Param("requestId") Long requestId);



	
	@Query(value = "select * from material_issue mi WHERE mi.request_id=:requestId  order by id  desc limit 1  ", nativeQuery = true)
	MaterialIssue findByrequestId(Long requestId);

}
