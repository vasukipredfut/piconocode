package com.aria.dao;

import com.aria.model.Units;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitsRepository extends JpaRepository<Units, Long> {


    Optional<Units> findByName(String name);

    @Query(value = "SELECT *  FROM units u WHERE u.name=:name Limit 1", nativeQuery = true)
    Optional<Units> findBy(@Param("name") String name);

}
