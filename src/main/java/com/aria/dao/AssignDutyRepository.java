package com.aria.dao;

import com.aria.dto.AssignUserListForSal;
import com.aria.dto.DutyAssignUserList;
import com.aria.dto.dwagesList;
import com.aria.model.AssignDuty;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignDutyRepository extends JpaRepository<AssignDuty, Long> {

    @Query(value = "SELECT *  FROM assign_duty ad WHERE ad.allotment_id =:allotmentId and ad.staff_id =:userId", nativeQuery = true)
    Optional<AssignDuty> findByAllotmentIdANDUserId(@Param("allotmentId") Long allotmentId, @Param("userId") Long userId);

    @Query(value = "SELECT *  FROM assign_duty ad WHERE ad.allotment_id =:allotmentId ", nativeQuery = true)
    List<AssignDuty> findAllByAllotmentId(@Param("allotmentId") Long allotmentId);

    @Query(value = "SELECT *  FROM assign_duty ad WHERE ad.allotment_id =:allotmentId and ad.assign_branch_id=:branchId and ad.assign_dept_id=:deptId", nativeQuery = true)
    List<AssignDuty> findAllBy(@Param("allotmentId") Long allotmentId, @Param("branchId") Long branchId, @Param("deptId") Long deptId);

    @Query(value = "SELECT *  FROM assign_duty ad WHERE ad.staff_id =:userId AND ad.emp_type=:empType ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<AssignDuty> findByAssignDuty(@Param("userId") Long userId, @Param("empType") int empType);

    @Query(value = "SELECT *  FROM assign_duty ad WHERE ad.staff_id =:userId AND ad.emp_type=:empType AND  ( cast(:assignDate  as date) is null OR (ad.assigned_date =:assignDate) )  ORDER BY id DESC LIMIT 1 ", nativeQuery = true)
    Optional<AssignDuty> findByAssignDuty(@Param("userId") Long userId, @Param("empType") int empType, @Param("assignDate") Date assignDate);

    @Query(value = "select ad.staff_id  as id, max(ed.name) from assign_duty ad left join emast_daily ed on ed.id = ad.staff_id  "
            + " where ad.staff_id in (select e.id from emast_daily e  WHERE (e.status='' OR e.status='A' OR e.status='0')) "
            + " AND ad.emp_type = 1 "
            + " AND (ad.assigned_date BETWEEN cast(:fromDate  as date) AND  cast(:toDate  as date) ) "
            + " group by ad.staff_id ", nativeQuery = true)
    List<AssignUserListForSal> findByAssignUserListForSal(@Param("fromDate") Date fromDate,
                                                          @Param("toDate") Date toDate);

    @Query(value = "select count(ad.staff_id) as count from assign_duty ad where "
            + " ad.staff_id =:userId  and ad.emp_type = 1 "
            + " and ( cast(:fromDate  as date) is null OR (ad.assigned_date >=:fromDate) )"
            + " AND (cast(:toDate  as date) is null OR (ad.assigned_date <=:toDate) ) "
            + " GROUP BY ad.staff_id ", nativeQuery = true)
    Integer findByAssignUserListCount(@Param("userId") Long userId,
                                      @Param("fromDate") Date fromDate,
                                      @Param("toDate") Date toDate);

    @Query(value = "select ad.staff_id  as id, max(ed.name) from assign_duty ad left join emast_daily ed on ed.id = ad.staff_id  "
            + " where ad.staff_id in (select e.id from emast_daily e  WHERE (e.status='' OR e.status='A' OR e.status='0')) "
            + " and (ad.contract <> 'Y' or ad.contract ='') and ad.emp_type = 1 "
            + " and ( cast(:fromDate  as date) is null OR (ad.assigned_date >=:fromDate) )"
            + " AND (cast(:toDate  as date) is null OR (ad.assigned_date <=:toDate) ) "
            + " group by ad.staff_id ", nativeQuery = true)
    List<AssignUserListForSal> findByAssignUserListForContractSal(@Param("fromDate") Date fromDate,
                                                                  @Param("toDate") Date toDate);

    @Query(value = "select * from assign_duty ad WHERE ad.staff_id =:userId AND ad.emp_type =:empType "
            + " AND (cast(:toDate  as date) is null OR (ad.assigned_date =:date) ) ", nativeQuery = true)
    List<AssignDuty> findBy(@Param("userId") Long userId,
                            @Param("empType") int empType,
                            @Param("date") Date date);


    @Query(value = "select ad.staff_id  as userId, max(ed.name) as empName , max(ed.branch) as branch,  max(ed.company) as orgId from assign_duty ad left join emast_daily ed on ed.id = ad.staff_id  "
            + " where ad.staff_id in (select e.id from emast_daily e  WHERE (e.status='' OR e.status='A' OR e.status='0')) "
            + " AND ed.dacatid <> 6  AND ad.emp_type = 1 AND (:branch is null OR ed.branch=:branch) AND (:dept is null OR ed.department =:dept) "
            + " AND (ad.assigned_date BETWEEN cast(:fromDate  as date) AND  cast(:toDate  as date) ) "
            + " group by ad.staff_id ", nativeQuery = true)
    List<AssignUserListForSal> findByAssignUserListForFactAct(@Param("fromDate") Date fromDate,
                                                              @Param("toDate") Date toDate,
                                                              @Param("branch") Long branch,
                                                              @Param("dept") Long dept );


    @Query(value = "select ad.staff_id  as userId, max(ed.name) as empName , max(ed.branch) as branch , max(ed.company) as orgId from assign_duty ad left join emast_daily ed on ed.id = ad.staff_id  "
            + " where ad.staff_id in (select e.id from emast_daily e  WHERE (e.status='' OR e.status='A' OR e.status='0')) "
            + " AND ed.dacatid = 6  AND ad.emp_type = 1 AND (:branch is null OR ed.branch=:branch) AND (:dept is null OR ed.department =:dept) "
            + " AND (ad.assigned_date BETWEEN cast(:fromDate  as date) AND  cast(:toDate  as date) ) "
            + " group by ad.staff_id ", nativeQuery = true)
    List<AssignUserListForSal> findByAssignUserListForApprenticeAct(@Param("fromDate") Date fromDate,
                                                              @Param("toDate") Date toDate,
                                                              @Param("branch") Long branch,
                                                              @Param("dept") Long dept );



    @Query(value = "SELECT Distinct ed.id as dWagesEmpId, ed.name as dWagesEmpName  FROM assign_duty ad "
            + " LEFT JOIN duty_allotment da ON ad.allotment_id = da.id LEFT JOIN emast_daily ed ON ad.staff_id = ed.id "
            + " LEFT JOIN shifts s ON da.shift_id = s.id LEFT JOIN luzone lz ON ad.assign_duty_id = lz.id LEFT JOIN salary_structure ss "
            + "  ON (ss.id = lz.male OR ss.id = lz.female) WHERE ad.emp_type = 1 "
            + " AND (:branch is null or ad.assign_branch_id = :branch) "
            + " AND (:dept is null or ad.assign_dept_id = :dept) "
            + " AND ( cast(:shiftDate  as date) is null OR (da.shift_date  =:shiftDate) )  "
            + " AND s.id =:shiftId AND s.category =:shiftType  AND  ss.wtype = 'CONTRACT' ", nativeQuery = true)
    List<dwagesList> getDWagesEmpList(@Param("branch") Long branch, @Param("dept") Long dept,
                                      @Param("shiftType") String shiftType, @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);



    @Query(value = "select ad.staff_id as empId, ed.employeename as empName  from assign_duty  ad left join duty_allotment da on da.id = ad.allotment_id left join " +
            "employee_details  ed on ed.id = ad.staff_id where ad.emp_type = 0 and DATE(da.shift_date) = DATE(:shiftDate)  order by ad.id desc ", nativeQuery = true)
    List<DutyAssignUserList> findAllByStaff (@Param("shiftDate") Date shiftDate);

    @Query(value = "select ad.staff_id as empId, ed.name as empName  from assign_duty  ad left join duty_allotment da on da.id = ad.allotment_id left join " +
            " emast_daily ed on ed.id = ad.staff_id where ad.emp_type = 1 AND DATE(da.shift_date) = DATE(:shiftDate) order by ad.id desc;", nativeQuery = true)
    List<DutyAssignUserList> findAllByWages (@Param("shiftDate") Date shiftDate);
}
