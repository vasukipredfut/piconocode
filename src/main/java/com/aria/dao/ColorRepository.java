package com.aria.dao;

import com.aria.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {


    @Query(value = "SELECT id, name FROM color ", nativeQuery = true)
    List<Tuple> findAlls();

    @Query(value = "SELECT * FROM color where name=:name LIMIT 1", nativeQuery = true)
    Optional<Color> findBy(@Param("name") String name);
}
