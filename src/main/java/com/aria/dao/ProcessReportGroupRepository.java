package com.aria.dao;

import com.aria.model.ProcessReportGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessReportGroupRepository extends JpaRepository<ProcessReportGroup, Long> {




    @Query(value = "SELECT * FROM process_report_group pm WHERE pm.prid =:reportNameId " , nativeQuery = true)
    List<ProcessReportGroup> findBy(@Param("reportNameId") Long reportNameId );
}
