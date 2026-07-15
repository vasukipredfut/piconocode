package com.aria.dao;


import com.aria.dto.*;
import com.aria.model.DaySalary;
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
public interface DaySalaryRepo extends JpaRepository<DaySalary, Long> {

    @Query(value = " SELECT SUM(da.basic) as basic, SUM(da.da) as da , SUM(da.allowance) as allowance , "
            + " da.emp_id as empId , max(da.employee_name) as empName FROM day_salary da where da.emp_id =:empId "
            + "AND (da.shift_date between cast(:fromDate  as date) AND cast(:toDate  as date))  GROUP BY da.emp_id ", nativeQuery = true)
    DailyWagesSumSalary findBy(@Param("empId") Long empId,
                               @Param("fromDate") Date fromDate,
                               @Param("toDate") Date toDate);

    @Query(value = " SELECT SUM(da.basic) as basic FROM day_salary da where da.emp_id =:empId "
            + "AND (da.shift_date IN  ( cast(:shiftDates  as date) ) ) GROUP BY da.emp_id ", nativeQuery = true)
    double findByLeaveBasic(@Param("empId") Long empId,
                            @Param("shiftDates") List<Date> shiftDates);

    @Query(value = " SELECT SUM(da.da) as da FROM day_salary da where da.emp_id =:empId "
            + "AND (da.shift_date IN  ( cast(:shiftDates  as date) ) ) GROUP BY da.emp_id ", nativeQuery = true)
    double findByLeaveDa(@Param("empId") Long empId,
                         @Param("shiftDates") List<Date> shiftDates);


    @Query(value = " select  ds.emp_id as empId , ds.employee_name as employeeName ,  ds.experience as experience , ds.designation_id as designationId , "
            + " ds.designation_name as designationName, ds.sex as gender, ds.joining_date as joiningDate , ds.basic as basic, ds.da as da, ds.allowance as allowance , "
            + " ds.per_day_total as perDayTotal , ds.shift_date as shiftDate ,    ds.dept as dept , dd.name as deptName , ds.branch as branchId , "
            + " b.name as branchName, ds.created_by as createdBy, ed.employeename as createdByName , ds.created_date as createdOn , ds.shift_id as shiftId , "
            + " ds.pshift as pShiftId, p.name as pShiftName,  s.name as shiftName    from day_salary ds left join department_details dd  on dd.id = ds.dept "
            + " left join branch b on b.id = ds.branch left join employee_details ed on ed.id = ds.created_by left join pshifts p on p.id = ds.pshift "
            + " left join shifts s on s.id = ds.shift_id   where (:branch is null OR ds.branch=:branch) AND (:dept is null OR ds.dept=:dept) "
            + " AND (ds.shift_date between cast(:fromDate  as date) AND cast(:toDate  as date))   ", nativeQuery = true)
    List<WagesSalary> findBy(@Param("fromDate") Date fromDate,
                             @Param("toDate") Date toDate,
                             @Param("branch") Long branch,
                             @Param("dept") Long dept);


    @Query(value = " select  ds.emp_id as empId , ds.employee_name as employeeName ,  ds.experience as experience , ds.designation_id as designationId , "
            + " ds.designation_name as designationName, ds.sex as gender, ds.joining_date as joiningDate , ds.basic as basic, ds.da as da, ds.allowance as allowance , "
            + " ds.per_day_total as perDayTotal , ds.shift_date as shiftDate ,    ds.dept as dept , dd.name as deptName , ds.branch as branchId , "
            + " b.name as branchName, ds.created_by as createdBy, ed.employeename as createdByName , ds.created_date as createdOn , ds.shift_id as shiftId , "
            + " ds.pshift as pShiftId, p.name as pShiftName,  s.name as shiftName    from day_salary ds left join department_details dd  on dd.id = ds.dept "
            + " left join branch b on b.id = ds.branch left join employee_details ed on ed.id = ds.created_by left join pshifts p on p.id = ds.pshift "
            + " left join shifts s on s.id = ds.shift_id   where (:branch is null OR ds.branch=:branch) AND (:dept is null OR ds.dept=:dept) "
            + " AND (ds.shift_date between cast(:fromDate  as date) AND cast(:toDate  as date))   ", nativeQuery = true)
    Page<WagesSalary> findBy(@Param("fromDate") Date fromDate,
                             @Param("toDate") Date toDate,
                             @Param("branch") Long branch,
                             @Param("dept") Long dept, Pageable pageable);


    @Query(value = " SELECT * FROM day_salary ds where ds.emp_id =:empId " +
            "AND ds.shift_date =:shiftDate AND ds.branch=:branch AND ds.dept =:dept AND ds.shift_id =:shiftId  AND ds.pshift =:pShiftId LIMIT 1", nativeQuery = true)
    Optional<DaySalary> findBy(@Param("empId") Long empId,
                               @Param("branch") Long branch,
                               @Param("dept") Long dept,
                               @Param("shiftId") Long shiftId,
                               @Param("pShiftId") Long pShiftId,
                               @Param("shiftDate") Date shiftDate);


    @Query(value = " SELECT ds.shift_date as shiftDate, ds.shift_id as shiftId, ds.pshift as pshift, "
            + " max(s.category) as shiftType, max(p.name) as pshiftName , ds.dept as deptId, "
            + " max(dd.name) as deptName , ds.dacatid as daCatId, ds.branch as branchId , "
            + " max(b.name) as branchName from day_salary ds left join shifts s on s.id = ds.shift_id "
            + " left join pshifts p on p.id = ds.pshift left join department_details dd on dd.id = ds.dept  "
            + " left join branch b on b.id = ds.branch "
            + " where (:branch is null OR ds.branch=:branch) AND (:dept is null OR ds.dept=:dept) "
            + " AND (  cast(:fromDate  as date)  is null OR ds.shift_date >=:fromDate )"
            + " AND (  cast(:toDate  as date)  is null OR ds.shift_date <=:toDate )"
            + " group by ds.branch , ds.dept, ds.shift_id, ds.shift_date, ds.shift_type, ds.pshift , ds.dacatid  "
            + " order By ds.shift_date DESC ", nativeQuery = true)
    Page<WagesTransactionList> findByList(@Param("branch") Long branch,
                                          @Param("dept") Long dept,
                                          @Param("fromDate") Date fromDate,
                                          @Param("toDate") Date toDate, Pageable pageable);


    @Query(value = " SELECT * FROM day_salary ds "
            + " where (:branch is null OR ds.branch=:branch) "
            + " AND (:dept is null OR ds.dept=:dept)  "
            + " AND (:shiftId is null OR ds.shift_id=:shiftId)  "
            + " AND (:pshiftId is null OR ds.pshift=:pshiftId)  "
            + " AND (:dacatId is null OR ds.dacatid=:dacatId)  "
            + " AND (:shiftType is null OR ds.shift_type=:shiftType)  "
            + " AND ( cast(:shiftDate  as date) is null OR ds.shift_date =:shiftDate )  ", nativeQuery = true)
    List<DaySalary> findBy(@Param("branch") Long branch,
                           @Param("dept") Long dept,
                           @Param("shiftDate") Date shiftDate,
                           @Param("shiftId") Long shiftId,
                           @Param("pshiftId") Long pshiftId,
                           @Param("dacatId") Long dacatId,
                           @Param("shiftType") String shiftType);


    @Query(value = " select  ds.emp_id as empId , ds.employee_name as employeeName ,  ds.experience as experience , u.desigid as designationId , "
            + " u.designation as designationName, ds.sex as gender, ds.joining_date as joiningDate , ds.basic as basic, ds.da as da, ds.allowance as allowance , "
            + " ds.per_day_total as perDayTotal , ds.shift_date as shiftDate ,    ds.dept as dept , dd.name as deptName , ds.branch as branchId , "
            + " b.name as branchName, ds.created_by as createdBy, ed.employeename as createdByName , ds.created_date as createdOn , ds.shift_id as shiftId , "
            + " ds.pshift as pShiftId, p.name as pShiftName,  s.name as shiftName , ds.check_in as checkIn, ds.check_out as checkOut, ds.total_working_hrs as totalworkinghrs    from day_salary ds left join department_details dd  on dd.id = ds.dept "
            + " left join branch b on b.id = ds.branch left join emast_daily u on u.id = ds.emp_id  left join employee_details ed on ed.id = ds.created_by left join pshifts p on p.id = ds.pshift "
            + " left join shifts s on s.id = ds.shift_id   where (:branch is null OR ds.branch=:branch) AND (:dept is null OR ds.dept=:dept) "
            + " AND (  cast(:fromDate  as date)  is null OR ds.shift_date >=:fromDate )"
            + " AND (  cast(:toDate  as date)  is null OR ds.shift_date <=:toDate )", nativeQuery = true)
    List<WagesSalary> findByList(@Param("fromDate") Date fromDate,
                                 @Param("toDate") Date toDate,
                                 @Param("branch") Long branch,
                                 @Param("dept") Long dept);


    @Query(value = " SELECT * FROM day_salary ds where ds.shift_date =:shiftDate AND ds.branch=:branch AND ds.dept =:dept AND ds.pshift =:pShiftId LIMIT 1", nativeQuery = true)
    Optional<DaySalary> findBy(@Param("branch") Long branch,
                               @Param("dept") Long dept,
                               @Param("pShiftId") Long pShiftId,
                               @Param("shiftDate") Date shiftDate);


    @Query(value = " SELECT * FROM day_salary ds where ds.branch=:branch AND ds.dept =:dept " +
            "AND ds.shift_date =:shiftDate AND ds.shift_id =:shiftId  AND ds.pshift =:pShiftId AND ds.dacatid <> 6 LIMIT 1", nativeQuery = true)
    Optional<DaySalary> findByWage(@Param("branch") Long branch,
                                   @Param("dept") Long dept,
                                   @Param("shiftId") Long shiftId,
                                   @Param("pShiftId") Long pShiftId,
                                   @Param("shiftDate") Date shiftDate);

    @Query(value = " SELECT * FROM day_salary ds where ds.branch=:branch AND ds.dept =:dept " +
            "AND ds.shift_date =:shiftDate AND ds.shift_id =:shiftId  AND ds.pshift =:pShiftId AND ds.dacatid = 6 LIMIT 1", nativeQuery = true)
    Optional<DaySalary> findByWageApp(@Param("branch") Long branch,
                                      @Param("dept") Long dept,
                                      @Param("shiftId") Long shiftId,
                                      @Param("pShiftId") Long pShiftId,
                                      @Param("shiftDate") Date shiftDate);


    @Query(value = " SELECT * FROM day_salary ds "
            + " WHERE (:branch is null OR ds.branch=:branch) "
            + " AND (:dept is null OR ds.dept=:dept)  "
            + " AND (:shiftId is null OR ds.shift_id=:shiftId)  "
            + " AND (:pshiftId is null OR ds.pshift=:pshiftId)  "
            + " AND (ds.dacatid <> 6)  "
            + " AND (:shiftType is null OR ds.shift_type=:shiftType)  "
            + " AND ( cast(:shiftDate  as date) is null OR ds.shift_date =:shiftDate )  ", nativeQuery = true)
    List<DaySalary> findByWages(@Param("branch") Long branch,
                                @Param("dept") Long dept,
                                @Param("shiftDate") Date shiftDate,
                                @Param("shiftId") Long shiftId,
                                @Param("pshiftId") Long pshiftId,
                                @Param("shiftType") String shiftType);

    @Query(value = " SELECT * FROM day_salary ds "
            + " WHERE (:branch is null OR ds.branch=:branch) "
            + " AND (:dept is null OR ds.dept=:dept) "
            + " AND (:shiftId is null OR ds.shift_id=:shiftId)  "
            + " AND (:pshiftId is null OR ds.pshift=:pshiftId)  "
            + " AND ( ds.dacatid = 6 ) "
            + " AND (:shiftType is null OR ds.shift_type=:shiftType)  "
            + " AND ( cast(:shiftDate  as date) is null OR ds.shift_date =:shiftDate ) ", nativeQuery = true)
    List<DaySalary> findByWageApp(@Param("branch") Long branch,
                                  @Param("dept") Long dept,
                                  @Param("shiftDate") Date shiftDate,
                                  @Param("shiftId") Long shiftId,
                                  @Param("pshiftId") Long pshiftId,
                                  @Param("shiftType") String shiftType);


}
