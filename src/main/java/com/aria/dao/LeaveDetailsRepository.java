package com.aria.dao;

import com.aria.dto.LeaveDetailsList;
import com.aria.dto.LeaveRegisterList;
import com.aria.model.LeaveDetails;

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
public interface LeaveDetailsRepository extends JpaRepository<LeaveDetails, Long> {

    @Query(value = "SELECT * FROM leave_details ld WHERE ld.user_id = :userId ORDER BY ld.id DESC ", nativeQuery = true)
    Page<LeaveDetails> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT SUM (ld.noOfDays) FROM LeaveDetails ld "
            + "WHERE (ld.userId = :userId AND ld.leaveType = :leaveType AND ld.status =:status) "
            + "GROUP BY ld.userId, ld.leaveType")
    Integer getLeaveCountByUserIdAndLeaveType(@Param("userId") Long userId, @Param("leaveType") String leaveType,
                                              @Param("status") String status);

    @Query(value = "SELECT * FROM leave_details ld WHERE (:employeeId is null or ld.user_id = :employeeId)"
            + "AND (cast(:date as date) is null or (ld.leave_from <= :date AND ld.leave_to >= :date ))"
            + "LIMIT 1", nativeQuery = true)
    Optional<LeaveDetails> checkLeave(@Param("employeeId") Long employeeId, @Param("date") Date date);

	/*@Query(value = "SELECT ld  FROM LeaveDetails ld "
			+ "WHERE (:employeeName is null or (lower(ld.employeeName) LIKE ('%' || lower(:employeeName) || '%')) ) "
			+ "AND  (:deptId is null or ld.deptId =:deptId ) "
			+ "AND  (:leaveType is null or (lower(ld.leaveType) LIKE ('%' || lower(:leaveType) || '%')) ) "
			+ "AND (:leaveStatus is null or (lower(ld.status) LIKE ('%' || lower(:leaveStatus) || '%')) ) "
			+ "AND (cast(:dateFrom as timestamp) is null or (ld.leaveFrom >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (ld.leaveTo <= :dateTo))")
	Page<LeaveDetails> findAllLeave(@Param("deptId") Long deptId,
									@Param("employeeName") String employeeName,
									@Param("leaveType") String leaveType,
									@Param("leaveStatus") String leaveStatus,
									@Param("dateFrom") Date dateFrom,
									@Param("dateTo") Date dateTo, Pageable pageable);*/


    @Query(value = "SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , d.name as designationName, " +
            " ld.leave_from as leaveFrom, ld.leave_to  as leaveTo, ld.leave_type as leaveType, ld.leave_reason as leaveReason , " +
            " ld.status as status, ld.reason_for_rejection as reasonForRejection , ld.no_of_days as noOfDays  from leave_details ld " +
            " left join employee_details ed on ed.id = ld.user_id left join designation d on d.id = ed.desi_id "
            + " WHERE (:employeeName is null or (lower(ed.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + " AND (:designationName is null or (lower(d.name) LIKE ('%' || lower(:designationName) || '%')) ) "
            + " AND  (:deptId is null or ld.dept_id =:deptId ) "
            + " AND  (:leaveType is null or (lower(ld.leave_type) LIKE ('%' || lower(:leaveType) || '%')) ) "
            + " AND (:leaveStatus is null or (lower(ld.status) LIKE ('%' || lower(:leaveStatus) || '%')) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (ld.leave_from >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (ld.leave_to <= :dateTo))", nativeQuery = true)
    Page<LeaveDetailsList> findAllLeave(@Param("deptId") Long deptId,
                                        @Param("employeeName") String employeeName,
                                        @Param("designationName") List<String> designationName,
                                        @Param("leaveType") String leaveType,
                                        @Param("leaveStatus") String leaveStatus,
                                        @Param("dateFrom") Date dateFrom,
                                        @Param("dateTo") Date dateTo, Pageable pageable);


    @Query(value = "SELECT * FROM leave_details ld WHERE ld.user_id = :userId ", nativeQuery = true)
    List<LeaveDetails> findBy(@Param("userId") Long userId);


    @Query(value = "SELECT COALESCE(SUM (ld.no_of_days),0) FROM leave_details ld "
            + "WHERE (ld.user_id = :userId AND ld.status ='Approve') AND ( ld.leave_type !='LeaveWithWages' OR  ld.leave_type !='Maternity' ) AND ( ld.leave_from >=:start AND ld.leave_to <=:end) "
            + "GROUP BY ld.user_id, ld.leave_type", nativeQuery = true)
    Integer getLeaveCountByUserIdAndLeaveType(@Param("userId") Long userId,
                                              @Param("start") Date start,
                                              @Param("end") Date end);


    @Query(value = " SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , d.name as designationName, "
            + " ld.leave_from as leaveFrom, ld.leave_to  as leaveTo, ld.leave_type as leaveType, ld.leave_reason as leaveReason , "
            + " ld.status as status, ld.reason_for_rejection as reasonForRejection , ld.no_of_days as noOfDays  from leave_details ld "
            + " left join employee_details ed on ed.id = ld.user_id left join designation d on d.id = ed.desi_id "
            + " WHERE (:employeeName is null or (lower(ed.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + " AND ( d.name IN ( :designationList ) )"
            + " AND  (:deptId is null or ld.dept_id =:deptId ) "
            + " AND  (:leaveType is null or (lower(ld.leave_type) LIKE ('%' || lower(:leaveType) || '%')) ) "
            + " AND (:leaveStatus is null or (lower(ld.status) LIKE ('%' || lower(:leaveStatus) || '%')) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (ld.leave_from >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (ld.leave_to <= :dateTo)) ORDER BY ld.id DESC ", nativeQuery = true)
    Page<LeaveDetailsList> findByIN(@Param("deptId") Long deptId,
                                    @Param("employeeName") String employeeName,
                                    @Param("designationList") List<String> designationList,
                                    @Param("leaveType") String leaveType,
                                    @Param("leaveStatus") String leaveStatus,
                                    @Param("dateFrom") Date dateFrom,
                                    @Param("dateTo") Date dateTo, Pageable pageable);


    @Query(value = " SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , d.name as designationName, "
            + " ld.leave_from as leaveFrom, ld.leave_to  as leaveTo, ld.leave_type as leaveType, ld.leave_reason as leaveReason , "
            + " ld.status as status, ld.reason_for_rejection as reasonForRejection , ld.no_of_days as noOfDays  from leave_details ld "
            + " left join employee_details ed on ed.id = ld.user_id left join designation d on d.id = ed.desi_id "
            + " WHERE (:employeeName is null or (lower(ed.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + " AND ( d.name NOT IN ( :designationList ) ) "
            + " AND ( ed.desi_id NOT IN ( 0 ) ) "
            + " AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND (:leaveType is null or (lower(ld.leave_type) LIKE ('%' || lower(:leaveType) || '%')) ) "
            + " AND (:leaveStatus is null or (lower(ld.status) LIKE ('%' || lower(:leaveStatus) || '%')) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (ld.leave_from >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (ld.leave_to <= :dateTo)) ORDER BY ld.id DESC ", nativeQuery = true)
    Page<LeaveDetailsList> findByNOTIN(@Param("deptId") Long deptId,
                                       @Param("employeeName") String employeeName,
                                       @Param("designationList") List<String> designationList,
                                       @Param("leaveType") String leaveType,
                                       @Param("leaveStatus") String leaveStatus,
                                       @Param("dateFrom") Date dateFrom,
                                       @Param("dateTo") Date dateTo, Pageable pageable);


    @Query(value = " SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , d.name as designationName, "
            + " ld.leave_from as leaveFrom, ld.leave_to  as leaveTo, ld.leave_type as leaveType, ld.leave_reason as leaveReason , "
            + " ld.status as status, ld.reason_for_rejection as reasonForRejection , ld.no_of_days as noOfDays  from leave_details ld "
            + " left join employee_details ed on ed.id = ld.user_id left join designation d on d.id = ed.desi_id "
            + " WHERE (:employeeName is null or (lower(ed.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + " AND ( d.name NOT IN ( :designationList ) ) "
            + " AND ( ed.desi_id NOT IN ( 0 ) ) "
            + " AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND (:leaveType is null or (lower(ld.leave_type) LIKE ('%' || lower(:leaveType) || '%')) ) "
            + " AND (  ( (lower(ld.status) LIKE ('%' || lower('Request') || '%')) ) OR ( (lower(ld.status) LIKE ('%' || lower('Verify') || '%')) ) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (ld.leave_from >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (ld.leave_to <= :dateTo)) ORDER BY ld.id DESC ", nativeQuery = true)
    Page<LeaveDetailsList> findByNOTINPlantMan(@Param("deptId") Long deptId,
                                               @Param("employeeName") String employeeName,
                                               @Param("designationList") List<String> designationList,
                                               @Param("leaveType") String leaveType,
                                               @Param("dateFrom") Date dateFrom,
                                               @Param("dateTo") Date dateTo, Pageable pageable);


    @Query(value = " SELECT  distinct ld.user_id as userId  from leave_details ld left join employee_details ed on ed.id = ld.user_id "
            + " WHERE (:branch is null OR ld.branch_id=:branch) AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND (:empId is null or ld.user_id =:empId ) "
            + " AND (cast(:dateFrom as date) is null or ( DATE(ld.leave_from) >= DATE(:dateFrom) ) ) "
            + " AND (cast(:dateTo as date) is null or ( DATE(ld.leave_to) <= DATE(:dateTo) ) ) ", nativeQuery = true)
    List<Long> findBy(@Param("branch") Long branch, @Param("deptId") Long deptId, @Param("empId") Long empId,
                      @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);


    @Query(value = " SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , ld.leave_type as leaveType, ld.leave_reason as leaveReason, "
            + " DATE(:shiftDate) as shiftDate , v.employeename as verifyByName , ld.verified_date as verifiedOn , ap.employeename as approvedByName , ld.approved__date as approvedOn "
            + " from leave_details ld  left join employee_details ed on ed.id = ld.user_id left join employee_details v on v.id = ld.verified_by "
            + " left join employee_details ap on ap.id = ld.approved_by   "
            + " WHERE (:empId is null or ld.user_id =:empId ) "
            + " AND (:branch is null or ld.branch_id =:branch ) "
            + " AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND ( ld.user_id IN(:ids) ) "
            + " AND ( DATE(:shiftDate) BETWEEN  DATE(ld.leave_from) AND DATE(ld.leave_to)  ) " , nativeQuery = true)
    List<LeaveRegisterList> findBy(@Param("branch") Long branch, @Param("deptId") Long deptId,
                                                @Param("empId") Long empId,
                                   @Param("shiftDate") Date shiftDate, @Param("ids") List<Long> ids);

    @Query(value = " SELECT  distinct ld.user_id as userId  from leave_details ld left join employee_details ed on ed.id = ld.user_id "
            + " WHERE ld.approved_by IS NULL AND ld.rejected_by IS NULL AND (:branch is null OR ld.branch_id=:branch) "
            + " AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND (:empId is null or ld.user_id =:empId ) "
            + " AND (cast(:dateFrom as date) is null or ( DATE(ld.leave_from) >= DATE(:dateFrom) ) ) "
            + " AND (cast(:dateTo as date) is null or ( DATE(ld.leave_to) <= DATE(:dateTo) ) ) ", nativeQuery = true)
    List<Long> findByNotApp(@Param("branch") Long branch, @Param("deptId") Long deptId, @Param("empId") Long empId,
                                         @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

    @Query(value = " SELECT ld.id as id, ld.user_id as userId , ed.employeename as employeeName , ld.leave_type as leaveType, ld.leave_reason as leaveReason, "
            + " DATE(:shiftDate) as shiftDate , s.name as shiftName , v.employeename as verifyByName , ld.verified_date as verifiedOn ,  "
            + " ap.employeename as approvedByName , ld.approved__date as approvedOn from leave_details ld  left join employee_details ed on ed.id = ld.user_id "
            + " left join employee_details v on v.id = ld.verified_by left join employee_details ap on ap.id = ld.approved_by  "
            + " left join shift_assign sa on sa.emp_id = ld.user_id and DATE(sa.shift_date) = DATE(:shiftDate)  "
            + " left join shifts s on s.id = sa.shift_id  "
            + " WHERE ld.approved_by IS NULL AND ld.rejected_by IS NULL  "
            + " AND (:branch is null or ld.branch_id =:branch ) AND (:deptId is null or ld.dept_id =:deptId ) "
            + " AND ( ld.user_id IN(:ids) ) "
            + " AND ( DATE(:shiftDate) BETWEEN  DATE(ld.leave_from) AND DATE(ld.leave_to)  ) " , nativeQuery = true)
    List<LeaveRegisterList> findByNotInApproved( @Param("branch") Long branch, @Param("deptId") Long deptId,
                                                 @Param("shiftDate") Date shiftDate, @Param("ids") List<Long> ids);


    @Query(value = " SELECT * FROM leave_details ld WHERE ld.user_id = :userId  "
            + " AND ( ( DATE(:dateFrom) BETWEEN  DATE(ld.leave_from) AND DATE(ld.leave_to)  ) "
            + " OR ( DATE(:dateTo) BETWEEN  DATE(ld.leave_from) AND DATE(ld.leave_to)  ) ) ", nativeQuery = true)
    List<LeaveDetails> findBy(@Param("userId") Long userId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);


    @Query(value = "SELECT * FROM leave_details ld WHERE ld.id = :id ", nativeQuery = true)
    LeaveDetails findLeaveDetailsById(@Param("id") Long id);
}
