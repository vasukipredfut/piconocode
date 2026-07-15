package com.aria.dao;

import com.aria.model.TsLap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TsLapRepository extends JpaRepository<TsLap, Long> {

    List<TsLap> findByPshift(int pshift);

    @Query(value = "select * from tslap  where (:id is null or id >:id)", nativeQuery = true)
    List<TsLap> findByIds(@Param("id") Long id);

}
