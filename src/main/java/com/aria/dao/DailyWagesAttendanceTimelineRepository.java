package com.aria.dao;

import com.aria.dto.AttendanceTimelineList;
import com.aria.dto.EmployeeOverViewList;
import com.aria.model.AttendanceTimeline;
import com.aria.model.DailyWagesAttendanceTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyWagesAttendanceTimelineRepository extends JpaRepository<DailyWagesAttendanceTimeline,Long> {


    @Query(value = "SELECT *  FROM daily_wages_attendance_timeline at WHERE at.attendanceid = :attendanceId ORDER BY attendancetime DESC LIMIT 1", nativeQuery = true)
    Optional<DailyWagesAttendanceTimeline> findLatestByAttendanceId(@Param("attendanceId") Long attendanceId);

	
	 
	@Query(value = "select i.date as createddate,b.location as location, b.machineip as ipaddress,at.intime as intime,at.outtime as outtime,TIME_FORMAT(TIMEDIFF(at.outtime, at.intime), '%H:%i') as totalworkingdays, "
		+ " ed.name as employeename,at.reason as reason,s.name as shiftname "
		+ " from daily_wages_attendance_timeline at left join dwlog i on i.id =at.dwlogId left join shift_assign sa on sa.shift_date=i.date and sa.emp_id =at.userid "
		 + " left join shifts s on s.id =sa.shift_id left join biomast b on b.sno = at.biomast_id "
		+ " left join emast_daily  ed on ed.bioid = i.bioid   "
		+ " where (at.intime is not null and at.outtime is not null)  and ed.name =:employeename and i.date between :fromdate and :todate "
		+ " order by at.id asc " ,nativeQuery = true)
	List<AttendanceTimelineList>  findListByWagesnameandShiftdate(@Param("employeename")String employeename,
			@Param("fromdate") LocalDate fromdate,@Param("todate") LocalDate todate);

	 
		@Query(value = "select t.id as id,i.date as attendancedate,t.intime as intime,t.outtime as outtime , TIME_FORMAT(TIMEDIFF(t.outtime, t.intime), '%H:%i') as totalworkingdays,t.reason as reason,ed.name as employeename "
		+ " from dwlog  i left join daily_wages_attendance_timeline  t on t.dwlogid  =i.id 	left join emast_daily  ed on ed.id=t.userid "
		+ " where i.date=:fromdate and ed.name =:employeename  and t.outtime is not null order by t.id asc" ,nativeQuery = true)
	List<EmployeeOverViewList> findLogDetails(@Param("fromdate") LocalDate fromdate, @Param("employeename")String employeename);




}
