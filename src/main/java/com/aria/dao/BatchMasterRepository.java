package com.aria.dao;

import com.aria.model.BatchMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchMasterRepository extends JpaRepository<BatchMaster, Long> {

    @Query(value = "SELECT * FROM batchmaster b WHERE b.batchno=:batchno Limit 1 ", nativeQuery = true)
    Optional<BatchMaster> findByBatchno(@Param("batchno") String batchno);

    @Query(value = "SELECT * FROM batchmaster b WHERE (b.id =:batchId) AND (b.batchno=:batchno) Limit 1 ", nativeQuery = true)
    Optional<BatchMaster> findByBatch(@Param("batchId") Long batchId, @Param("batchno") String batchno);
    
    List<BatchMaster> findAllByBatchno(String batchNo);

}
