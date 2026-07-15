package com.aria.dao;

import com.aria.model.CanteenAttendance;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CanteenAttendanceRepository extends JpaRepository<CanteenAttendance, Long> {

	@Query(value = "SELECT * FROM canteen_attendances  WHERE id IN(SELECT attendance_menu_id FROM canteen_attendance_menu WHERE type=:typeKey)", nativeQuery = true)
	List<CanteenAttendance> findByType(@Param("typeKey") String typeKey);
	
	@Query(value="select count(type) as count, cam.type, DATE(ca.date) as date from canteen_attendances ca "
			+ ",canteen_attendance_menu cam where cam.attendance_menu_id=ca.id "
			+ "group by DATE(ca.date), cam.type", nativeQuery=true)
	Page<Tuple> getAllData(Pageable pageable);

}