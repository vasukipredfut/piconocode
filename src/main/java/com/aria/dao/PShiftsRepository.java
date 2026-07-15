package com.aria.dao;

import com.aria.model.PShifts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;

@Repository
public interface PShiftsRepository extends JpaRepository<PShifts, Long> {



    @Query(value = "SELECT id, name FROM pshifts", nativeQuery = true)
    List<Tuple> findAlls();
}
