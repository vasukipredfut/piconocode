package com.aria.dao;

import com.aria.model.BillInwardLorryDetails;
import com.aria.model.HOConfigRESTLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HOConfigRESTLogRepository extends JpaRepository<HOConfigRESTLog, Long> {

    @Query(value = "SELECT * FROM ho_config_rest_log b WHERE b.type_id=:typeId AND b.type='GRN' LIMIT 1", nativeQuery = true)
    Optional<HOConfigRESTLog> findBy(@Param("typeId") Long typeId);
}
