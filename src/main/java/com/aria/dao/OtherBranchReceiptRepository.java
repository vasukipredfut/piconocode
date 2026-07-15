
package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.TransactionList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.OtherBranchReceiptList;
import com.aria.model.OtherBranchReceipt;

@Repository
public interface OtherBranchReceiptRepository extends JpaRepository<OtherBranchReceipt, Long> {

	@Query(value = "SELECT obr.id AS id, obr.hup_id AS hupId, obr.request_id AS requestId, obr.docket_no AS docketNo, obr.status AS status, obr.issue_date AS issueDate, rb.name AS requestFromBranch, rd.name AS requestFromDepartment, ib.name AS issueFromBranch, id.name AS issueFromDepartment, ied.employeename AS issuePerson "
			+ "FROM other_branch_receipt obr " + "LEFT JOIN branch rb ON rb.id = obr.request_branch_id "
			+ "LEFT JOIN department_details rd ON rd.id = obr.request_dept_id "
			+ "LEFT JOIN branch ib ON ib.id = obr.issue_branch_id "
			+ "LEFT JOIN department_details id ON id.id = obr.issue_dept_id "
			+ "LEFT JOIN employee_details ied ON ied.id = obr.user_id  "
			+ "WHERE (:hupId IS NULL OR LOWER(obr.hup_id) LIKE ('%' || LOWER(:hupId) || '%')) "
			+ "AND (:docketNo IS NULL OR LOWER(obr.docket_no) LIKE ('%' || LOWER(:docketNo) || '%')) "
			+ "AND (:issueDeptName IS NULL OR LOWER(id.name) LIKE ('%' || LOWER(:issueDeptName) || '%')) "
			+ "AND (:issueBranchName IS NULL OR LOWER(ib.name) LIKE ('%' || LOWER(:issueBranchName) || '%')) "
			+ "AND (:firmId IS NULL OR obr.request_org_id = :firmId) "
			+ "AND (:branchId IS NULL OR obr.request_branch_id = :branchId) "
			+ "AND (:deptId IS NULL OR obr.request_dept_id = :deptId) "
			+ "AND (obr.issue_date BETWEEN CAST(:dateFrom AS DATE) AND CAST(:dateTo AS DATE) OR (CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)) "
			+ "AND (:status IS NULL OR LOWER(obr.status) LIKE ('%' || LOWER(:status) || '%')) "
			+ "ORDER BY obr.id DESC", nativeQuery = true)
	Page<OtherBranchReceiptList> otherBranchReceiptList(@Param("firmId") Long firmId, @Param("branchId") Long branchId, @Param("deptId") Long deptId,
			@Param("hupId") String hupId, @Param("docketNo") String docketNo,
			@Param("issueDeptName") String issueDeptName, @Param("issueBranchName") String issueBranchName,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("status") String status,
			Pageable pageable);

	@Query(value = "SELECT * FROM with_in_branch_distribution ob WHERE ob.request_id =:requestId AND ob.issue_id=:issueId AND ob.trn_no =:trnNo ", nativeQuery = true)
	Optional<OtherBranchReceipt> findBy(@Param("requestId") Long requestId, @Param("issueId") Long issueId,
			@Param("trnNo") String trnNo);

	Optional<OtherBranchReceipt> findByTrnNo(@Param("trnNo") String trnNo);


	@Query(value = " SELECT  obr.id as refTrnId, obr.trn_no as trnNo,  :transactionType as trnName, obr.issue_branch_id as issueBranchId, " +
			" obr.request_branch_id as requestBranchId, obr.issue_dept_id as issueDeptId, " +
			" obr.request_dept_id as requestDeptId,  obr.received_date as trnDate " +
			" FROM other_branch_receipt obr INNER JOIN stbin s ON s.trnno = obr.trn_no AND s.entry_type ='MATERIAL_TRANSACTION' AND s.trntype='R' " +
			" WHERE ( CAST(:shiftDate as date) is null or DATE(obr.received_date) = DATE(:shiftDate))" +
			" AND (:branch  is null or obr.request_branch_id = :branch ) AND (:dept is null or obr.request_dept_id = :dept ) " +
			" AND obr.trn_no  IS NOT NULL AND (  s.sal_status IS NULL  OR  s.sal_status <> 'Completed')   " +
			" GROUP BY obr.id, obr.trn_no, obr.issue_branch_id, obr.request_branch_id, obr.issue_dept_id, obr.request_dept_id, s.trn_date   ", nativeQuery = true)
	List<TransactionList> getTransactionListFilterByINTER_IN(@Param("shiftDate") Date shiftDate,
															 @Param("transactionType") String transactionType, @Param("branch") Long branch, @Param("dept") Long dept);


}
