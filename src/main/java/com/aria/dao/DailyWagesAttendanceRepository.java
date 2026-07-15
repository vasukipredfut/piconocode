package com.aria.dao;

import com.aria.dto.AttendanceTimelineList;
import com.aria.model.Attendance;
import com.aria.model.DailyWagesAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface DailyWagesAttendanceRepository extends JpaRepository<DailyWagesAttendance,Long> {



    @Query(value = "SELECT *  FROM daily_wages_attendance a WHERE a.user_id =:userId and a.date =:date", nativeQuery = true)
    Optional<DailyWagesAttendance> findByUserAndDate(@Param("userId") Long  userId, @Param("date") Date date);

    @Query(value = "SELECT COUNT(*) > 0 FROM daily_wages_attendance a WHERE a.user_id = :userId AND DATE(a.date) = DATE(:date) ",nativeQuery = true)
	boolean existsAttendanceForShiftDate(@Param("userId") Long  userId, @Param("date") Date date);
    
    @Query(value = " SELECT at.punchIn as checkIn,at.punchOut as checkOut,TIME_FORMAT(TIMEDIFF(at.punchOut, at.punchIn), '%H:%i') as totalworkingdays  FROM daily_wages_attendance at where at.user_id =:userId and at.date =:date",nativeQuery = true)
    Optional<AttendanceTimelineList>  findAttendanceDetail(@Param("userId") Long  userId, @Param("date") Date date);

	

}
