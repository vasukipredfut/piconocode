package com.aria.dao;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.aria.model.BinStatus;

@Repository
public interface BinStatusRepository extends JpaRepository<BinStatus,Long> {


    @Query(value = "SELECT * FROM binstatus bin WHERE bin_name = :binNo and locked =:status ", nativeQuery = true)
    Optional<BinStatus> findBy(@Param("binNo") Long binNo, @Param("status") Boolean status);
}
