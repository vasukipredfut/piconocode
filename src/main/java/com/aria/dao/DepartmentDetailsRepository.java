package com.aria.dao;

import com.aria.model.DepartmentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentDetailsRepository extends JpaRepository<DepartmentDetails,Long> {


    @Query(value = "SELECT *  FROM department_details dd  WHERE ( :orgId is null OR dd.org_id =:orgId )", nativeQuery = true)
    List<DepartmentDetails> findAll(@Param("orgId") Long orgId);
}
