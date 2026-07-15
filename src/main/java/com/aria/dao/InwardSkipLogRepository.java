package com.aria.dao;

import com.aria.model.InwardSkipLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InwardSkipLogRepository extends JpaRepository<InwardSkipLog, Long> {

    @Query(value = "SELECT * FROM inward_skip_log sk where sk.kpi =:kpi and sk.tgid=:tgId and sk.batchno =:batchNo and sk.tno =:tno  limit 1",nativeQuery = true)
    Optional<InwardSkipLog> findBy(@Param("kpi") Long kpi, @Param("tgId") Long tgId , @Param("batchNo") String batchNo, @Param("tno") String tno  );
}
