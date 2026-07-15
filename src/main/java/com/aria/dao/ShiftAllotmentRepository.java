package com.aria.dao;


import com.aria.dto.ShiftAllotmentList;
import com.aria.model.ShiftAllotment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftAllotmentRepository extends JpaRepository<ShiftAllotment, String> {

    @Query(value = "SELECT * FROM shift_allotment sa WHERE (:type is null or sa.emp_type =:type ) ", nativeQuery = true)
    Page<ShiftAllotment> findAll(@Param("type") int type, Pageable pageable);

    @Query(value = "select sa.id as id, sh.id as shiftId , sa.created_date as exemptedShiftDate , sh.name as exemptedShiftName , sh.category as shiftCategory, ed.id as empId , ed.employeename as empName, ed.employeetype as empCategory , dt.id as deptID , dt.name as deptName from shift_allotment sa "
            + "LEFT JOIN shifts sh ON sh.id = sa.shift_id LEFT JOIN employee_details ed ON ed.id = sa.emp_id LEFT JOIN department_details dt ON dt.id = sa.dept_id "
            + "WHERE  sa.emp_type = 0 "
            + "AND (:empName is null or (lower(ed.employeename) LIKE ('%' || lower(:empName) || '%')))"
            + "AND (:deptId is null or sa.dept_id=:deptId) "
            + "AND (:shiftName is null or sh.category=:shiftName) "
            + "AND (:type is null or sa.emp_type =:type ) ", nativeQuery = true)
    Page<ShiftAllotmentList> findAllEmp(@Param("empName") String empName,
                                        @Param("deptId") Long deptId,
                                        @Param("shiftName") String shiftName,
                                        @Param("type") int type, Pageable pageable);

    @Query(value = "select sa.id as id, sh.id as shiftId , sa.created_date as exemptedShiftDate , sh.name as exemptedShiftName , sh.category as shiftCategory , ed.id as empId , ed.name as empName, ed.category as empCategory , dt.id as deptID , dt.name as deptName from shift_allotment sa "
            + "LEFT JOIN shifts sh ON sh.id = sa.shift_id LEFT JOIN emast_daily ed ON ed.id = sa.emp_id LEFT JOIN department_details dt ON dt.id = sa.dept_id "
            + "WHERE  sa.emp_type = 1 "
            + "AND (:empName is null or (lower(ed.name) LIKE ('%' || lower(:empName) || '%'))) "
            + "AND (:deptId is null or sa.dept_id=:deptId) "
            + "AND (:shiftName is null or sh.category=:shiftName) "
            + "AND (:type is null or sa.emp_type =:type ) ", nativeQuery = true)
    Page<ShiftAllotmentList> findAllEmast(@Param("empName") String empName,
                                      @Param("deptId") Long deptId,
                                      @Param("shiftName") String shiftName,
                                      @Param("type") int type,
                                      Pageable pageable);

    @Query(value = "SELECT * FROM shift_allotment sa WHERE sa.shift_id=:shiftId AND sa.emp_id =:userId ", nativeQuery = true)
    List<ShiftAllotment> findAll(@Param("shiftId") Long shiftId, @Param("userId") Long userId);
    
    @Query(value = "SELECT * FROM shift_allotment sa WHERE sa.shift_id in (:shiftId) AND sa.emp_id =:userId and sa.emp_type=:emptype ", nativeQuery = true)
    List<ShiftAllotment> findAllShift(@Param("shiftId") List<Long> shiftId, @Param("userId") Long userId, @Param("emptype") Integer emptype);
}
