package com.aria.dao;

import com.aria.model.SjtQty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SjtQtyRepo extends JpaRepository<SjtQty, Long> {

    @Query(value = "select * from sjtqty where sjid =:sjId limit 1",nativeQuery = true)
    Optional<SjtQty> findBySjId(@Param("sjId") Long sjId);
}
