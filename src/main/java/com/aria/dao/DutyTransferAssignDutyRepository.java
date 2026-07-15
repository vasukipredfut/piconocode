package com.aria.dao;

import com.aria.dto.DutyTransferAssignDutyList;
import com.aria.model.DutyTransferAssignDuty;

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
public interface DutyTransferAssignDutyRepository extends JpaRepository<DutyTransferAssignDuty, Long> {


	@Query(value = "SELECT *  FROM duty_transfer_assign_duty d WHERE d.transfer_id =:transferId and d.user_id =:userId", nativeQuery = true)
	Optional<DutyTransferAssignDuty> findByTransferIdANDUserId (@Param("transferId") Long transferId, @Param("userId") Long userId);


	@Query(value = "SELECT *  FROM duty_transfer_assign_duty d WHERE d.transfer_id =:transferId ", nativeQuery = true)
	List<DutyTransferAssignDuty> findAllByTransferId (@Param("transferId") Long transferId);

	@Query(value = "SELECT *  FROM duty_transfer_assign_duty d WHERE d.created_date > CURRENT_DATE and user_id =: empId ", nativeQuery = true)
	List<DutyTransferAssignDuty> findAllByuserId (@Param("created_date") Date created_date, @Param("empId") Long empId);


	@Query(value = "select dtad.id as id, dtad.transfer_id as transferId, staff.id as staffId, staff.employeename as employeeName, cd.id as currentDeptId, cd.name as currentDeptName , ad.id as assignDeptId, ad.name as assignDeptName, cbr.id as currentBranchId, cbr.name as currentBranchName, abr.id as assignBranchId, " +
			"abr.name as assignBranchName, dt.shift_date as assignedDate FROM duty_transfer_assign_duty dtad LEFT JOIN employee_details staff ON staff.id = dtad.user_id LEFT JOIN duty_transfer dt ON dt.id = dtad.transfer_id LEFT JOIN department_details cd ON cd.id = dtad.current_dept_id LEFT JOIN department_details ad ON ad.id = dtad.assign_dept_id " +
			"LEFT JOIN branch cbr ON cbr.id = dtad.current_branch_id LEFT JOIN branch abr ON abr.id = dtad.assign_branch_id "
			+ "WHERE dtad.emp_type=0 AND (:branchId is null OR dt.branch_id =:branchId) AND (:deptId is null OR dt.dept_id=:deptId) "
			+ "AND (:employeeName is null or (lower(staff.employeename) LIKE ('%' || lower(:employeeName) || '%'))) "
			+ "AND (:assignedBranchName is null or (lower(abr.name) LIKE ('%' || lower(:assignedBranchName) || '%')) ) "
			+ "AND (:fromDepartmentName is null or (lower(cd.name) LIKE ('%' || lower(:fromDepartmentName) || '%')) ) "
			+ "AND (:toDepartmentName is null or (lower(ad.name) LIKE ('%' || lower(:toDepartmentName) || '%')) ) "
			+ "AND (cast(:fromDate as timestamp) is null or (dt.shift_date >= :fromDate)) "
			+ "AND (cast(:toDate as timestamp) is null or (dt.shift_date <= :toDate)) ORDER BY  dtad.id DESC  ", nativeQuery = true)
	Page<DutyTransferAssignDutyList> getDutyTransferAssignDutyFilterListEmp(@Param("branchId") Long branchId , @Param("deptId") Long deptId , @Param("employeeName") String employeeName,
																		 @Param("assignedBranchName") String assignedBranchId, @Param("fromDepartmentName") String fromDepartmentName,
																		 @Param("toDepartmentName") String toDepartmentName, @Param("fromDate") Date fromDate,
																		 @Param("toDate") Date toDate, Pageable pageable);


	@Query(value = "select dtad.id as id, dtad.transfer_id as transferId, staff.id as staffId, staff.name as employeeName, cd.id as currentDeptId, cd.name as currentDeptName , ad.id as assignDeptId, ad.name as assignDeptName, cbr.id as currentBranchId, cbr.name as currentBranchName, abr.id as assignBranchId, " +
			"abr.name as assignBranchName, dt.shift_date as assignedDate FROM duty_transfer_assign_duty dtad LEFT JOIN emast_daily staff ON staff.id = dtad.user_id LEFT JOIN duty_transfer dt ON dt.id = dtad.transfer_id LEFT JOIN department_details cd ON cd.id = dtad.current_dept_id LEFT JOIN department_details ad ON ad.id = dtad.assign_dept_id " +
			"LEFT JOIN branch cbr ON cbr.id = dtad.current_branch_id LEFT JOIN branch abr ON abr.id = dtad.assign_branch_id "
			+ "WHERE dtad.emp_type = 1 AND (:branchId is null OR dt.branch_id =:branchId) AND (:deptId is null OR dt.dept_id=:deptId) "
			+ "AND (:employeeName is null or (lower(staff.name) LIKE ('%' || lower(:employeeName) || '%'))) "
			+ "AND (:assignedBranchName is null or (lower(abr.name) LIKE ('%' || lower(:assignedBranchName) || '%')) ) "
			+ "AND (:fromDepartmentName is null or (lower(cd.name) LIKE ('%' || lower(:fromDepartmentName) || '%')) ) "
			+ "AND (:toDepartmentName is null or (lower(ad.name) LIKE ('%' || lower(:toDepartmentName) || '%')) ) "
			+ "AND (cast(:fromDate as timestamp) is null or (dt.shift_date >= :fromDate)) "
			+ "AND (cast(:toDate as timestamp) is null or (dt.shift_date <= :toDate)) ORDER BY dtad.id DESC ", nativeQuery = true)
	Page<DutyTransferAssignDutyList> getDutyTransferAssignDutyFilterListEmast(@Param("branchId") Long branchId , @Param("deptId") Long deptId , @Param("employeeName") String employeeName,
																			@Param("assignedBranchName") String assignedBranchId, @Param("fromDepartmentName") String fromDepartmentName,
																			@Param("toDepartmentName") String toDepartmentName, @Param("fromDate") Date fromDate,
																			@Param("toDate") Date toDate, Pageable pageable);
}
