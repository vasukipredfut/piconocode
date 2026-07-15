package com.aria.dao;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.AttendanceTimelineList;
import com.aria.model.Attendance;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query(value = "SELECT *  FROM attendance a WHERE a.user_id =:userId", nativeQuery = true)
    List<Attendance> findAllByUser(@Param("userId") Long userId);

    @Query(value = "SELECT *  FROM attendance a WHERE a.user_id =:userId and a.date =:date", nativeQuery = true)
    Optional<Attendance> findByUserAndDate(@Param("userId") Long  userId, @Param("date") Date date);

    @Query(value = "SELECT date  FROM attendance a WHERE a.user_id =:userId", nativeQuery = true)
    List<Date> findAllDatesByUser(@Param("userId") Long userId);

    @Query(value = "SELECT *  FROM attendance a WHERE a.user_id =:userId AND a.date BETWEEN :date1 AND :date2", nativeQuery = true)
    List<Attendance> findAllDatesByUser(@Param("userId") Long userId,  @Param("date1") Date date1, @Param("date2") Date date2);

    @Query(value = "SELECT SUM(over_time)  FROM attendance a WHERE a.user_id =:userId AND a.date BETWEEN :date1 AND :date2", nativeQuery = true)
    Object sumOfOverTimeUser(@Param("userId") Long userId,  @Param("date1") Date date1, @Param("date2") Date date2);

    @Query(value = "SELECT *  FROM attendance a WHERE a.date =:date and a.punchin is Null and a.punchout is Null", nativeQuery = true)
    List<Attendance> findAllByDate(@Param("date") Date date);

    @Query(value = "SELECT COUNT(*) > 0 FROM attendance a WHERE a.user_id = :userId AND DATE(a.date) = DATE(:shiftDate) ",nativeQuery = true)
    boolean existsAttendanceForShiftDate(@Param("userId") Long userId,@Param("shiftDate") Date shiftDate);

    @Query(value = "SELECT COUNT(*) > 0 FROM attendance a WHERE a.user_id = :userId AND DATE(a.date) = :attendanceDate " ,nativeQuery = true)
    boolean existsAttendanceForDate(@Param("userId") Long userId,@Param("attendanceDate") LocalDate attendanceDate);


    @Query(value = "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM attendance a " +
            "WHERE a.user_id = :userId AND DATE(a.date) = :attendanceDate AND a.punchout IS NULL " , nativeQuery = true)
    boolean existsByUserIdAndAttendanceDateAndOutTimeIsNull(@Param("userId") Long userId,
                                                            @Param("attendanceDate") LocalDate attendanceDate);

	@Query(value = " SELECT at.punchIn as checkIn,at.punchOut as checkOut,TIME_FORMAT(TIMEDIFF(at.punchOut, at.punchIn), '%H:%i') as totalworkingdays  FROM attendance at where at.user_id =:userId and at.date =:date",nativeQuery = true)
    Optional<AttendanceTimelineList>  findAttendanceDetail(@Param("userId") Long  userId, @Param("date") Date date);

}
