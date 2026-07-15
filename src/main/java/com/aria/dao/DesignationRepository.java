
package com.aria.dao;

import com.aria.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository extends JpaRepository<Designation,Long> {


    @Query(value = " SELECT * FROM designation d WHERE (:designation is null or (lower(d.name) LIKE ('%' || lower(:designation) || '%'))) AND d.status='A' ", nativeQuery = true)
    List<Designation> findAllByDesignation(@Param("designation") String designation);
}