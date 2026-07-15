package com.aria.dao;

import com.aria.model.LotValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotValuesDao extends JpaRepository<LotValues, Long> {


    @Query(value = "select   distinct lot  from lot_value l  where l.lot IS NOT NULL ", nativeQuery = true)
    List<String> findByLotlist();

}
