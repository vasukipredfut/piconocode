package com.aria.dao;

import com.aria.model.ProcessReportProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessReportProductGroupRepository extends JpaRepository<ProcessReportProductGroup,Long> {


    @Query(value = "SELECT * FROM process_report_product_group pr " +
            " where pr.prid =:reportNameId and pr.prigrpid =:reportGroupId and pr.activeflag = 1 " , nativeQuery = true)
    List<ProcessReportProductGroup> findBy(@Param("reportNameId") Long reportNameId ,
                                           @Param("reportGroupId") Long reportGroupId);
}
