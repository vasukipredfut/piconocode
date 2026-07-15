package com.aria.dao;

import com.aria.dto.DWagesSalarySummary;
import com.aria.dto.IAssignDutyFilterListResponse;
import com.aria.model.DutyAllotment;
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
public interface DutyAllotmentRepository extends JpaRepository<DutyAllotment, Long> {


    @Query(value = "SELECT *  FROM duty_allotment d WHERE d.transfer_id =:transferId ", nativeQuery = true)
    Optional<DutyAllotment> findByTransferId(@Param("transferId") Long transferId);


    @Query(value = "SELECT *  FROM duty_allotment d WHERE d.id =:id ", nativeQuery = true)
    Optional<DutyAllotment> findById(@Param("id") Long id);


    @Query(value = "SELECT dt.id as allotmentId, ed.employeename as employeeName , ed.id as employeeId, ad.assign_duty as assignedDuty , ad.current_duty as currentDuty, dt.shift as assignedShift ,  dt.shift_date as assignedDate,  ad.assign_dept_id as assignDeptId  FROM duty_allotment dt LEFT JOIN assign_duty ad ON ad.allotment_id = dt.id LEFT JOIN employee_details ed ON ed.id = ad.staff_id "
            + "WHERE ad.emp_type=0 "
            + "AND (:employeeName is null or (lower(ed.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + "AND  (:branchId is null or ad.assign_branch_id =:branchId ) "
            + "AND  (:deptId is null or ad.assign_dept_id =:deptId ) "
            + "AND  (:shift is null or (lower(dt.shift) LIKE ('%' || lower(:shift) || '%')) ) "
            + "AND (dt.shift_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ORDER BY ad.id DESC ", nativeQuery = true)
    Page<IAssignDutyFilterListResponse> getDutyAllotmentFilterListByEmp(@Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("employeeName") String employeeName, @Param("shift") String shift, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

    @Query(value = " SELECT dt.id as allotmentId, ed.name as employeeName , ed.id as employeeId, ad.assign_duty as assignedDuty , ad.status as status , "
            + " ad.current_duty as currentDuty, dt.shift as assignedShift ,  dt.shift_date as assignedDate, ad.assign_dept_id as assignDeptId "
            + " FROM duty_allotment dt LEFT JOIN assign_duty ad ON ad.allotment_id = dt.id LEFT JOIN emast_daily ed ON ed.id = ad.staff_id "
            + " WHERE ad.emp_type=1 AND  (:branchId is null or ad.assign_branch_id =:branchId ) "
            + " AND (:employeeName is null or (lower(ed.name) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + " AND  (:deptId is null or ad.assign_dept_id =:deptId ) "
            + " AND  (:shift is null or (lower(dt.shift) LIKE ('%' || lower(:shift) || '%')) ) "
            + " AND (dt.shift_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) ORDER BY ad.id DESC ", nativeQuery = true)
    Page<IAssignDutyFilterListResponse> getDutyAllotmentFilterListByEmast(@Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("employeeName") String employeeName, @Param("shift") String shift, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

/*
    @Query(value = " select da.id as allotmentId, da.shift_date as shiftDate,  da.shift as shiftType, da.shift_id as shiftId,  s.pshift_id as pShiftId,"
            + " ad.assign_dept_id as workedDept , dd.name as workedDeptName  , ad.assign_branch_id as workedBranch, "
            + " b.name as workedBranchName, ad.staff_id as empId, ad.emp_type as emp_type, ad.assign_duty_id  as luzone, "
            + " ad.contract as contract from duty_allotment da left join assign_duty ad on da.id = ad.allotment_id "
            + " left join shifts s on s.id = da.shift_id  left join luzone l on l.id = ad.assign_duty_id "
            + " left join branch b on b.id = ad.assign_branch_id left join department_details dd on dd.id = ad.assign_dept_id "
            + " WHERE ad.staff_id IS NOT NULL AND ad.assign_duty_id IS NOT NULL AND ad.emp_type=1 "
            + " AND  (:branchId is null or ad.assign_branch_id =:branchId ) "
            + " AND  (:deptId is null or ad.assign_dept_id =:deptId ) "
            + " AND  (:shiftId is null or da.shift_id =:shiftId ) "
            + " AND  (:shiftType is null or (lower(da.shift) LIKE ('%' || lower(:shiftType) || '%')) ) "
            + " AND  (:shiftName is null or (lower(s.name) LIKE ('%' || lower(:shiftName) || '%')) ) "
            + " AND ( cast(:shiftDate  as date) is null OR (da.shift_date =:shiftDate) )  ", nativeQuery = true)
    List<DWagesSalarySummary> findBy(@Param("branchId") Long branchId,
                                     @Param("deptId") Long deptId,
                                     @Param("shiftName") String shiftName,
                                     @Param("shiftId") Long shiftId,
                                     @Param("shiftDate") Date shiftDate,
                                     @Param("shiftType") String shiftType);*/
    @Query(value = " select da.id as allotmentId, da.shift_date as shiftDate,  da.shift as shiftType, da.shift_id as shiftId,  s.pshift_id as pShiftId," +
            "             ad.assign_dept_id as workedDept , add.name as workedDeptName  , ad.assign_branch_id as workedBranch, " +
            "             ab.name as workedBranchName, dd.id as fromDept , dd.name as fromDeptName  , b.id as fromBranch, " +
            "             b.name as fromBranchName, ad.staff_id as empId, ad.emp_type as emp_type, ad.assign_duty_id  as luzone, " +
            "             ad.contract as contract from duty_allotment da left join assign_duty ad on da.id = ad.allotment_id " +
            "             left join emast_daily ed on ed.id = ad.staff_id left join shifts s on s.id = da.shift_id  " +
            "             left join luzone l on l.id = ad.assign_duty_id " +
            "             left join branch b on b.id = ed.branch left join department_details dd on dd.id = ed.department " +
            "             left join branch ab on ab.id = ad.assign_branch_id left join department_details add on add.id = ad.assign_dept_id " +
            "             WHERE ad.staff_id IS NOT NULL AND ad.assign_duty_id IS NOT NULL AND ad.emp_type=1       " +
            "            AND  (:branchId is null or b.id =:branchId ) " +
            "            AND  (:deptId is null or dd.id =:deptId ) " +
            "             AND  (:shiftId is null or da.shift_id =:shiftId ) "
            + " AND  (:shiftType is null or (lower(da.shift) LIKE ('%' || lower(:shiftType) || '%')) ) "
            + " AND  (:shiftName is null or (lower(s.name) LIKE ('%' || lower(:shiftName) || '%')) ) "
            + " AND ( cast(:shiftDate  as date) is null OR (da.shift_date =:shiftDate) )  ", nativeQuery = true)
    List<DWagesSalarySummary> findBy(@Param("branchId") Long branchId,
                                     @Param("deptId") Long deptId,
                                     @Param("shiftName") String shiftName,
                                     @Param("shiftId") Long shiftId,
                                     @Param("shiftDate") Date shiftDate,
                                     @Param("shiftType") String shiftType);

}
