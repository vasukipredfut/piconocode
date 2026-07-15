package com.aria.dao;

import com.aria.model.EmployeeQualificationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EmployeeQualificationMappingRepository extends JpaRepository<EmployeeQualificationMapping, Long> {


    @Query(value = "SELECT *  FROM employee_qualification_mapping u WHERE u.emp_id =:empId", nativeQuery = true)
    List<EmployeeQualificationMapping> findByEmpId(@Param("empId") Long empId);
}
