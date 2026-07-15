package com.aria.dao;

import com.aria.model.CanteenAttendanceToken;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  CanteenAttendanceTokenRepository extends JpaRepository<CanteenAttendanceToken, Long> {
	@Query(value = "SELECT * FROM canteen_attendance_token WHERE date =:date", nativeQuery = true)
	Optional<CanteenAttendanceToken> findAllByDate(@Param("date") Date date);
	@Query(value = "SELECT * FROM canteen_attendance_token WHERE date =:date", nativeQuery = true)
	List<CanteenAttendanceToken> findByDate(@Param("date") Date date);
}