package com.aria.dao;


import com.aria.model.Weekoff;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekOffRepository extends JpaRepository<Weekoff, Long> {

    /*@Query(value = "SELECT *  FROM shift_rotation s WHERE s.shift_id =:shiftId ORDER BY id DESC limit 1", nativeQuery = true)
    ShiftRotation findByShiftId (@Param("shiftId") Long shiftId);
    
	@Query(value = "SELECT sr FROM ShiftRotation sr WHERE (:branchId is null or sr.branch.id = :branchId) "
			+ "AND (:departmentId is null or sr.departmentDetails.id = :departmentId) "
			+ "AND (:employeeType is null or lower(sr.category) = lower(:employeeType)) "
			+ "AND (cast(:dateFrom as timestamp) is null or (sr.fromDate >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (sr.toDate <= :dateTo))")
	List<ShiftRotation> filterShiftRotationList(@Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("employeeType") String employeeType,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);*/

    @Query(value = "SELECT *  FROM weekoff w WHERE w.emp_id =:empId", nativeQuery = true)
    Optional<Weekoff> findById (@Param("empId") Long empId);

    @Query(value = "SELECT *  FROM weekoff w WHERE w.emp_id =:empId AND w.emp_type=:empType", nativeQuery = true)
    Optional<Weekoff> findById (@Param("empId") Long empId, @Param("empType") int empType);
    
    
    @Query(value = "SELECT *  FROM weekoff w WHERE w.emp_id  in (:empId) AND w.emp_type=:empType", nativeQuery = true)
    List<Weekoff> findByEmployees (@Param("empId") List<Long> empId, @Param("empType") int empType);
}
