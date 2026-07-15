package com.aria.dao;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.aria.dto.UserList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ListShiftRotation;
import com.aria.model.ShiftRotation;

@Repository
public interface ShiftRotationRepository extends JpaRepository<ShiftRotation, Long> {

    @Query(value = "SELECT *  FROM shift_rotation s WHERE s.shift1_id =:shiftId OR s.shift2_id =:shiftId OR s.shift3_id =:shiftId  ORDER BY id DESC limit 1", nativeQuery = true)
    ShiftRotation findByShiftId(@Param("shiftId") Long shiftId);


    @Query(value = " SELECT  max(sr.category) as category, max(sr.from_date) as fromDate, max(sr.to_date) as toDate, max(sa.created_date) as createdDate,  "
            + " sa.rotation_id as rotationId, dept.name as departmentName,  max(sa.emp_type) as empType FROM shift_assign  sa, "
            + " shift_rotation sr, employee_details emp, department_details dept WHERE sr.id = sa.rotation_id AND emp.id=sa.emp_id and dept.id = sa.department_id  "
            + " AND (:branchId is null or sa.branch_id =:branchId) "
            + " AND (:departmentId is null or sa.department_id =:departmentId) "
            + " AND (:empType is null or sa.emp_type =:empType) "
            + " AND (:employeeType is null or lower(emp.employeetype) = lower(:employeeType)) "
            + " AND (cast(:dateFrom as timestamp) is null or (sa.created_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (sa.created_date <= :dateTo)) "
            + " group by dept.name, sa.rotation_id order by sa.rotation_id DESC ", nativeQuery = true)
    Page<ListShiftRotation> filterShiftRotationEmpList(@Param("branchId") Long branchId,
                                                       @Param("departmentId") Long departmentId,
                                                       @Param("employeeType") String employeeType,
                                                       @Param("dateFrom") LocalDateTime dateFrom,
                                                       @Param("dateTo") LocalDateTime dateTo,
                                                       @Param("empType") int empType, Pageable pageable);


    @Query(value = " SELECT  max(sr.category) as category , max(sr.from_date) as fromDate, max(sr.to_date) as toDate, max(sa.created_date) as createdDate, sa.rotation_id as rotationId, "
            + " dept.name as departmentName,   max(sa.emp_type) as empType FROM shift_assign  sa, "
            + " shift_rotation sr, employee_details emp, department_details dept WHERE sr.id = sa.rotation_id AND emp.id=sa.emp_id and dept.id = sa.department_id  "
            + " AND (:branchId is null or sa.branch_id = :branchId) "
            + " AND (:departmentId is null or sa.department_id = :departmentId) "
            + " AND (:empType is null or sa.emp_type =:empType) "
            + " AND (:employeeType is null or lower(emp.category) = lower(:employeeType)) "
            + " AND (cast(:dateFrom as timestamp) is null or (sa.created_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (sa.created_date <= :dateTo))"
            + "group by dept.name, sa.rotation_id order by sa.rotation_id DESC ", nativeQuery = true)
    Page<ListShiftRotation> filterShiftRotationEmastDailyList(@Param("branchId") Long branchId,
                                                              @Param("departmentId") Long departmentId,
                                                              @Param("employeeType") String employeeType,
                                                              @Param("dateFrom") LocalDateTime dateFrom,
                                                              @Param("dateTo") LocalDateTime dateTo,
                                                              @Param("empType") int empType, Pageable pageable);


    @Query(value = "select u.id as id,  u.employeename as employeeName, u.gender as gender,     " +
            "design.id as designationId, design.name as designationName, u.employeetype as EmployeeType ,     " +
            "br.id as branchId, br.name as branchName, dpt.name as departmentName, dpt.id as departmentId      " +
            "FROM shift_assign sa LEFT JOIN  shift_rotation sr on sr.id = sa.rotation_id   LEFT JOIN shifts sh ON sh.id = sa.shift_id     " +
            "LEFT JOIN employee_details u ON u.id = sa.emp_id LEFT JOIN branch br ON br.id=u.branch_id     " +
            "LEFT JOIN department_details dpt ON dpt.id=u.dept_id  LEFT JOIN designation design ON design.id=u.desi_id      " +
            "WHERE  sa.emp_type =0     " +
            "AND (:employeeCategory is null or sr.category =:employeeCategory)     " +
            "AND (:gender is null or u.gender =:gender)     " +
            "AND (:shiftId is null or sh.id = :shiftId)     " +
            "AND (:branchId is null or br.id = :branchId)     " +
            "AND (:departmentId is null or dpt.id = :departmentId)     " +
            "AND (:shiftType is null or lower(sh.category) = lower(:shiftType))     " +
            "AND (cast(:shiftDate as date) is null or  sa.shift_date =:shiftDate )", nativeQuery = true)
    List<UserList> filterShiftRotationUserList(@Param("employeeCategory") String employeeCategory,
                                               @Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
                                               @Param("shiftType") String shiftType, @Param("shiftId") Long shiftId,
                                               @Param("shiftDate") Date shiftDate, @Param("gender") String gender);


    @Query(value = " SELECT  u.id as id, u.name as employeeName, u.category as EmployeeType , u.gender as gender,     " +
            "u.desigid as designationId , u.designation as designationName , br.id as branchId,     " +
            "max(br.name) as branchName, max(dpt.name) as departmentName, dpt.id as departmentId     " +
            "FROM shift_assign sa LEFT JOIN  shift_rotation sr on sr.id = sa.rotation_id     " +
            "LEFT JOIN shifts sh ON sh.id = sa.shift_id LEFT JOIN emast_daily u ON u.id = sa.emp_id     " +
            "LEFT JOIN branch br ON br.id=u.branch LEFT JOIN department_details dpt ON dpt.id=u.department      " +
            "WHERE  sa.emp_type = 1     " +
            "AND (:employeeCategory is null or sr.category = :employeeCategory)     " +
            "AND (:gender is null or u.gender =:gender)     " +
            "AND (:shiftId is null or sh.id = :shiftId)     " +
            "AND (:branchId is null or br.id = :branchId)     " +
            "AND (:departmentId is null or dpt.id = :departmentId)     " +
            "AND (:shiftType is null or lower(sh.category) = lower(:shiftType))     " +
            "AND (cast(:shiftDate as date) is null or  sa.shift_date =:shiftDate )    " +
            "group by u.id, br.id , dpt.id , sa.shift_id , sa.shift_date ", nativeQuery = true)
    List<UserList> filterShiftRotationEmastUserList(@Param("employeeCategory") String employeeCategory,
                                                    @Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
                                                    @Param("shiftType") String shiftType, @Param("shiftId") Long shiftId,
                                                    @Param("shiftDate") Date shiftDate, @Param("gender") String gender);


}
