package com.aria.dao;

import com.aria.model.ProcessReportTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessReportTitleRepository extends JpaRepository<ProcessReportTitle,Long> {



    @Query(value = "SELECT * FROM process_report_group_title pr " +
            " where pr.prid =:reportNameId and pr.prgrpid =:reportGroupId  order by pr.order asc " , nativeQuery = true)
    List<ProcessReportTitle> findBy(@Param("reportNameId") Long reportNameId ,
                                           @Param("reportGroupId") Long reportGroupId);
}
