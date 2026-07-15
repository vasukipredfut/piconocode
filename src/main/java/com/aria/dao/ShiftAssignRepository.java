package com.aria.dao;

import com.aria.dto.EmployeeShiftProjection;
import com.aria.dto.ShiftAssignUserList;
import com.aria.dto.ShiftRotationMappingList;
import com.aria.dto.UserList;
import com.aria.model.ShiftAssign;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftAssignRepository extends JpaRepository<ShiftAssign, Long> {




   /* @Query(value = "SELECT sa.* FROM shift_assign sa left join shifts sh on sh.id = sa.shift_id  WHERE (:empType is null or sa.emp_type = :empType) "
            + " AND (:branchId is null or sa.branch_id = :branchId) "
            + " AND (:departmentId is null or sa.department_id = :departmentId) "
            + " AND (:shiftName is null or (sh.category =:shiftName)) "
            + " AND (     ( sa.from_date BETWEEN (cast(:dateFrom as date)) AND (cast(:dateTo as date)) ) OR   "
            + " ( sa.to_date BETWEEN (cast(:dateFrom as date)) AND (cast(:dateTo as date)) ) )", nativeQuery = true)
    List<ShiftAssign> viewfilterlist(@Param("empType") int empType,
                                     @Param("branchId") Long branchId,
                                     @Param("departmentId") Long departmentId,
                                     @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
                                     @Param("shiftName") String shiftName);*/

    @Query(value = "SELECT sa.* FROM shift_assign sa left join shifts sh on sh.id = sa.shift_id  WHERE (:empType is null or sa.emp_type = :empType) "
            + " AND (:empId is null or sa.emp_id =:empId) "
            + " AND (:branchId is null or sa.branch_id =:branchId) "
            + " AND (:departmentId is null or sa.department_id = :departmentId) "
            + " AND (:shiftName is null or (sh.category =:shiftName)) "
            + " AND ( sa.shift_date BETWEEN (cast(:dateFrom as date)) AND (cast(:dateTo as date)) ) ", nativeQuery = true)
    List<ShiftAssign> viewfilterlist(@Param("empType") int empType,
                                     @Param("empId") Long empId,
                                     @Param("branchId") Long branchId,
                                     @Param("departmentId") Long departmentId,
                                     @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
                                     @Param("shiftName") String shiftName);


    @Query(value = "SELECT sa.emp_id as empId, max(ed.employeename)  as empName    " +
            "FROM shift_assign sa left join shift_rotation sr on sr.id  = sa.rotation_id   " +
            "left join shifts sh on sh.id = sa.shift_id   " +
            "LEFT JOIN employee_details ed on ed.id = sa.emp_id WHERE sa.emp_type = 0   " +
            "AND (:employeeCategory is null or sr.category = :employeeCategory)  " +
            "AND (:branchId is null or sa.branch_id = :branchId)   " +
            "AND (:departmentId is null or sa.department_id = :departmentId)   " +
            "AND (:shiftName is null or (sh.category =:shiftName))   " +
            "AND ( sa.shift_date BETWEEN (cast(:dateFrom as date)) AND (cast(:dateTo as date)) )   " +
            "GROUP BY sa.emp_id  ", nativeQuery = true)
    List<ShiftAssignUserList> viewUserlist(@Param("employeeCategory") String employeeCategory,
                                           @Param("branchId") Long branchId,
                                           @Param("departmentId") Long departmentId,
                                           @Param("dateFrom") Date dateFrom,
                                           @Param("dateTo") Date dateTo,
                                           @Param("shiftName") String shiftName);


    @Query(value = "SELECT sa.emp_id as empId, max(ed.name)  as empName    " +
            "FROM shift_assign sa left join shift_rotation sr on sr.id  = sa.rotation_id   " +
            "left join  shifts sh on sh.id = sa.shift_id   " +
            "LEFT JOIN emast_daily ed on ed.id = sa.emp_id WHERE sa.emp_type = 1   " +
            "AND (:employeeCategory is null or sr.category = :employeeCategory)  " +
            "AND (:branchId is null or sa.branch_id = :branchId)   " +
            "AND (:departmentId is null or sa.department_id = :departmentId)   " +
            "AND (:shiftName is null or (sh.category =:shiftName))   " +
            "AND ( sa.shift_date BETWEEN (cast(:dateFrom as date)) AND (cast(:dateTo as date)) )   " +
            "GROUP BY sa.emp_id ", nativeQuery = true)
    List<ShiftAssignUserList> viewEmastUserlist(@Param("employeeCategory") String employeeCategory,
                                                @Param("branchId") Long branchId,
                                                @Param("departmentId") Long departmentId,
                                                @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
                                                @Param("shiftName") String shiftName);


    @Query(value = "SELECT sa FROM ShiftAssign sa WHERE (:empId is null or sa.emp_id = :empId) "
            + "AND (:branchId is null or sa.branch_id = :branchId)"
            + "AND (:departmentId is null or sa.department_id = :departmentId) "
            + "AND (cast(:date as date) is null or (:date BETWEEN sa.from_date AND sa.to_date)) ")
    List<ShiftAssign> checkWeekOff(@Param("empId") Long empId, @Param("branchId") Long branchId,
                                   @Param("departmentId") Long departmentId,
                                   @Param("date") Date date);


    @Query(value = "SELECT distinct u.id as empId, u.employeename as employeeName,  design.name as designation , wd.id as weekOff, wd.weekday as weekOffName, wd.weekday as currentWeekOff FROM shift_assign sa LEFT JOIN employee_details u ON u.id = sa.emp_id LEFT JOIN designation design ON design.id=u.desi_id LEFT JOIN weekoff wo ON wo.emp_id = sa.emp_id LEFT JOIN weekday wd ON wd.id = wo.weekoffday " +
            "WHERE  sa.emp_id = u.id  and sa.shift_id =:shiftId and sa.rotation_id =:rotationId", nativeQuery = true)
    List<ShiftRotationMappingList> getEmployeeList(@Param("shiftId") Long shiftId, @Param("rotationId") Long rotationId);


    @Query(value = "SELECT * FROM shift_assign  WHERE rotation_id =:rotationId", nativeQuery = true)
    List<ShiftAssign> findByRotationId(@Param("rotationId") Long rotationId);

    @Query(value = "SELECT * FROM shift_assign  WHERE rotation_id =:rotationId LIMIT 1", nativeQuery = true)
    ShiftAssign findByRotId(@Param("rotationId") Long rotationId);


    @Query(value = "SELECT * FROM shift_assign sa WHERE (:empId is null or sa.emp_id = :empId) AND ( sa.emp_type =:emptype) "
            + " AND ( ( sa.from_date BETWEEN (cast(:fromDate as date)) AND (cast(:toDate as date))  ) OR ( sa.to_date BETWEEN (cast(:fromDate as date)) AND (cast(:toDate as date))  ) ", nativeQuery = true)
    List<ShiftAssign> checkUpdateShift(@Param("empId") Long empId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("emptype") int emptype);

    @Query(value = "SELECT * FROM shift_assign sa WHERE sa.emp_id =:empId AND sa.emp_type =:empType AND (cast(:shiftDate as date) is null or (sa.shift_date =:shiftDate))   ", nativeQuery = true)
    Optional<ShiftAssign> findBy(@Param("empId") Long empId, @Param("shiftDate") Date shiftDate, @Param("empType") int empType);


    @Query(value = "SELECT sa.emp_id as id, max(ed.name) as employeeName  "
            + " FROM shift_assign sa left join  shifts sh on sh.id = sa.shift_id "
            + " LEFT JOIN emast_daily ed on ed.id = sa.emp_id WHERE sa.emp_type = 1 "
            + " AND (cast(:shiftDate as date) is null or (sa.shift_date =:shiftDate)) "
            + " AND (:shiftId is null or (sa.shift_id =:shiftId)) "
            + " GROUP BY sa.emp_id ", nativeQuery = true)
    List<UserList> shiftRestrictEmastStaffList(@Param("shiftDate") Date shiftDate,
                                               @Param("shiftId") Long shiftId);

    @Query(value = "SELECT sa.emp_id as id, max(ed.employeename) as employeeName  "
            + " FROM shift_assign sa left join  shifts sh on sh.id = sa.shift_id "
            + " LEFT JOIN employee_details ed on ed.id = sa.emp_id WHERE sa.emp_type = 0 "
            + " AND (cast(:shiftDate as date) is null or (sa.shift_date =:shiftDate)) "
            + " AND (:shiftId is null or (sa.shift_id =:shiftId)) "
            + " GROUP BY sa.emp_id ", nativeQuery = true)
    List<UserList> shiftRestrictStaffList(@Param("shiftDate") Date shiftDate,
                                          @Param("shiftId") Long shiftId);
    
    @Query(value="select * from shift_assign sa where sa.emp_id=:empid and sa.shift_date =:shiftdate",nativeQuery = true)
    ShiftAssign getShifts(@Param("empid")Long empid,@Param("shiftdate") LocalDate shiftdate);
    
    //new query
    @Query(value="select ed.id as empId , ed.bioid as bioId , ed.employeename as employeename,sa.shift_date as shiftdate,s.\"name\" as shiftname , s.intime  as StartTime , s.outtime as endTime "
    		+ "from shift_assign sa left join employee_details ed on ed.id=sa.emp_id \r\n"
    		+ "left join shifts s on s.id=sa.shift_id where sa.shift_value ='S' AND sa.emp_type = 0 and sa.emp_id in (:eId) and sa.shift_date BETWEEN :fromdate AND :todate ",nativeQuery = true)
    List<EmployeeShiftProjection> findAllListForStaff(@Param("fromdate")LocalDate fromdate,@Param("todate") LocalDate todate,@Param("eId") List<Long>eId);
    

    @Query(value="select ed.id as empId , ed.bioid as bioId , ed.name as employeename,sa.shift_date as shiftdate,s.\"name\" as shiftname ,s.intime  as StartTime , s.outtime as endTime "
    		+ "from shift_assign sa left join emast_daily ed  on ed.id=sa.emp_id \r\n"
    		+ "left join shifts s on s.id=sa.shift_id where  sa.shift_value ='S' AND sa.emp_type = 1 and sa.emp_id in (:eId)  and sa.shift_date BETWEEN :fromdate AND :todate",nativeQuery = true)
    List<EmployeeShiftProjection> findAllListForWages(@Param("fromdate")LocalDate fromdate,@Param("todate") LocalDate todate,@Param("eId") List<Long>eId);

    @Query(value=" SELECT DISTINCT ed.id FROM shift_assign sa JOIN employee_details ed ON ed.id = sa.emp_id "
    + " WHERE (:dept is null or sa.department_id = :dept ) and (:name is null or lower(ed.employeename ) like('%'|| lower(:name) || '%')) and sa.shift_value = 'S' AND sa.emp_type = 0 AND sa.shift_date BETWEEN :fromdate AND :todate ",nativeQuery = true)
	Page<Long> findAllEmployee(LocalDate fromdate, LocalDate todate, String name,Long dept, Pageable pageable);
   
    @Query(value=" SELECT DISTINCT ed.id FROM shift_assign sa JOIN emast_daily ed ON ed.id = sa.emp_id "
    	    + " WHERE(:dept is null or sa.department_id = :dept ) and (:name is null or lower(ed.name ) like('%'|| lower(:name) || '%')) and  sa.shift_value = 'S' AND sa.emp_type = 1 AND sa.shift_date BETWEEN :fromdate AND :todate ",nativeQuery = true)
    		Page<Long> findAllWages(LocalDate fromdate, LocalDate todate,String name,Long dept, Pageable pageable);




    // explicit JPQL query because entity property is named `emp_id` (underscored)
    // using a derived method would cause Spring Data to look for `empId` property
    // which doesn't exist, hence the PropertyReferenceException at runtime.
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END " +
           "FROM ShiftAssign sa " +
           "WHERE sa.emp_id = :empId AND sa.shift_date = :shiftDate AND sa.emp_type = :empType")
    boolean existsByEmpIdAndShiftDateAndEmpType(@Param("empId") Long empId,
                                                 @Param("shiftDate") Date shiftDate,
                                                 @Param("empType") int empType);

    @Query(value="select sa.shift_id from shift_assign sa where sa.emp_id=:empid and sa.shift_date =:shiftdate limit 1",nativeQuery = true)
	Long findShiftIdByEmpId( @Param("empid") Long empid,@Param("shiftdate") Date shiftdate);

}
