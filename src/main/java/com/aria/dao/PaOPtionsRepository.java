package com.aria.dao;

import com.aria.model.PaOPtions;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;


@Repository
public interface PaOPtionsRepository extends JpaRepository<PaOPtions, Long> {



    @Query(value = "select id, name from paoptions where id=:id ", nativeQuery = true)
    List<Tuple> findAll(@Param("id") Long id);

    @Query(value = "select * from paoptions where id=:id ", nativeQuery = true)
    List<PaOPtions> findAlls(@Param("id") Long id);
}
