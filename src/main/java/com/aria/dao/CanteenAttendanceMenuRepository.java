package com.aria.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CanteenAttendanceMenu;

@Repository
public interface  CanteenAttendanceMenuRepository extends JpaRepository<CanteenAttendanceMenu, Long> {

	@Query(value = "SELECT * FROM canteen_attendance_menu WHERE attendance_menu_id =:attendanceMenuId ", nativeQuery = true)
	List<CanteenAttendanceMenu> findAllById(@Param("attendanceMenuId") Long attendanceMenuId);
	
	
}