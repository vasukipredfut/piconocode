package com.aria.dao;

import com.aria.model.BsLap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;

@Repository
public interface BsLapRepository extends JpaRepository<BsLap,Long> {

    @Query(value = "SELECT id, name FROM bslap where (:id is null or id >:id) ", nativeQuery = true)
    List<Tuple> findAlls(@Param("id") Long id);
}
