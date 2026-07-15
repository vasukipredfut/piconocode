package com.aria.dao;

import com.aria.model.ProcessReportName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessReportNameRepository extends JpaRepository<ProcessReportName,Long> {


    @Query(value = "SELECT * FROM process_report_name pm WHERE " +
            " (:name is null OR lower(pm.name) LIKE ('%' || lower(:name) || '%')) ", nativeQuery = true)
    List<ProcessReportName> findAll (@Param("name") String name );

}
