package com.aria.dao;

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

import com.aria.dto.AttendanceTimelineList;
import com.aria.dto.AttendanceTimelineProjection;
import com.aria.dto.EmployeeOverViewList;
import com.aria.model.Attendance;
import com.aria.model.AttendanceTimeline;

@Repository
public interface AttendanceTimelineRepository extends JpaRepository<AttendanceTimeline, Long> {

//	@Query(value = "SELECT *  FROM attendance_timeline at WHERE at.attendance_id = :attendanceId ORDER BY attendance_time DESC LIMIT 1", nativeQuery = true)
//	Optional<AttendanceTimeline> findLatestByAttendanceId(@Param("attendanceId") Long attendanceId);
	
	@Query(value = "SELECT *  FROM attendance_timeline at WHERE at.attendanceid = :attendanceId ORDER BY attendancetime DESC LIMIT 1", nativeQuery = true)
	Optional<AttendanceTimeline> findLatestByAttendanceId(@Param("attendanceId") Long attendanceId);

	@Query(value = "SELECT *  FROM attendance_timeline at " + "LEFT JOIN biomast bt ON bt.sno = at.biomast_id "
			+ "WHERE (at.attendance_id = :attendanceId) AND (bt.location = :location) ORDER BY attendance_time DESC LIMIT 1", nativeQuery = true)
	Optional<AttendanceTimeline> findLatestByAttendanceIdAndLocation(@Param("attendanceId") Long attendanceId,
			@Param("location") String location);

	@Query(value = "SELECT *  FROM attendance_timeline at "
			+ "WHERE (at.attendance_id = :attendanceId) ORDER BY attendance_time ASC", nativeQuery = true)
	List<AttendanceTimeline> findByAttendanceIdSortByAttendanceTime(@Param("attendanceId") Long attendanceId);

//	@Query(value = "SELECT DISTINCT a  FROM attendance a JOIN FETCH a.attendanceTimelines", countQuery = "SELECT COUNT(DISTINCT a) FROM attendance a JOIN FETCH a.attendanceTimelines")
//	Page<Attendance> findAllSortByAttendanceTime(Pageable pageable);

	@Query(value = "select at.id as id, at.attendancedate as shiftdate,at.intime as intime,at.outtime as outtime,CAST(TIMEDIFF(at.outtime, at.intime) AS CHAR) AS totalworkhours,\r\n"
			+ "			at.biomast_id as bioid,ed.employeename  as employeename,b.\"location\" as fingerprintstation,s.\"name\" as shift,at.reason as reason\r\n"
			+ "			from attendance_timeline at left join employee_details ed on ed.id=at.userid left join biomast b on b.sno=at.biomast_id \r\n"
			+ "			left join instrulog i on i.slno = at.instrulogid \r\n"
			+ "			left join shift_assign sa on sa.shift_date = i.\"date\"  and sa.emp_id =at.userid left join shifts s on s.id=sa.shift_id \r\n"
			+ "			where   (at.intime is not null and at.outtime is not null)\r\n"
			+ "			order by at.id asc", nativeQuery = true)
	List<AttendanceTimelineProjection> findByEmployeenameAndfromdateAndTodate();
	@Query(value="select i.date as createddate,b.\"location\" as location, b.machineip as ipaddress,at.intime as intime,at.outtime as outtime,TIME_FORMAT(TIMEDIFF(at.outtime, at.intime), '%H:%i') as totalworkingdays,\r\n"
			+ "			ed.employeename as employeename,at.reason as reason,s.name as shiftname\r\n"
			+ "			from attendance_timeline at left join instrulog i on i.slno=at.instrulogid left join shift_assign sa on sa.shift_date=i.date and sa.emp_id =at.userid\r\n"
			+ "			left join shifts s on s.id =sa.shift_id left join biomast b on b.sno = at.biomast_id\r\n"
			+ "			left join employee_details ed on ed.bioid = i.enrollnumber \r\n"
			+ "			where (at.intime is not null and at.outtime is not null)  and ed.employeename =:employeename\r\n"
			+ "			and i.date between :fromdate and :todate\r\n"
			+ "			order by at.id asc",nativeQuery = true)
	List<AttendanceTimelineList>  findListByEmployeenameandShiftdate(@Param("employeename")String employeename,
			@Param("fromdate") LocalDate fromdate,@Param("todate") LocalDate todate);
	@Query(value="select count(*) from attendance_timeline t \r\n"
			+ "left join instrulog i on i.slno=t.instrulogid  \r\n"
			+ "where t.userid=:userid and i.date=:fromdate",nativeQuery = true)
	int gettotalcount(@Param("userid")Long userid,@Param("fromdate") LocalDate fromdate);
	
	@Query(value="select t.id as id,i.date as attendancedate,t.intime as intime,t.outtime as outtime,\r\n"
			+ "			TIME_FORMAT(TIMEDIFF(t.outtime, t.intime), '%H:%i') as totalworkingdays,t.reason as reason,ed.employeename as employeename\r\n"
			+ "			from instrulog i left join attendance_timeline t on t.instrulogid =i.slno\r\n"
			+ "			left join employee_details ed on ed.id=t.userid \r\n"
			+ "			where i.date=:fromdate and ed.employeename =:employeename  and t.outtime is not null order by t.id asc",nativeQuery = true)
	List<EmployeeOverViewList>  findLogDetails(@Param("fromdate") LocalDate fromdate,@Param("employeename") String employeename);

	 
}
