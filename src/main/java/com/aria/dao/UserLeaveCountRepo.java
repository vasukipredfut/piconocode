package com.aria.dao;

import com.aria.model.UserLeaveCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLeaveCountRepo extends JpaRepository<UserLeaveCount, Long> {

    @Query(value = "SELECT earn_leave_count FROM employee_leave el WHERE el.emp_id=:empId", nativeQuery = true)
    Integer getYearnedLeave(@Param("empId") Long empId);

    @Query(value = "SELECT remaining_leave_count FROM employee_leave el WHERE el.emp_id=:empId",nativeQuery = true)
    Integer getRemainingLeave(@Param("empId") Long empId);
}
