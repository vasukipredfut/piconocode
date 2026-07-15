package com.aria.dao;

import com.aria.dto.UnitReportMapList;
import com.aria.model.UnitReportTableMap;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitReportTableMapRepository extends JpaRepository<UnitReportTableMap, Long> {
    @Query(value = " SELECT u.proid as productId1, pm1.name as productName1 ,  " +
            " u.proid2 as productId2, pm2.name as productName2 , u.sprocess as rotaryJournalId  , " +
            " u.sbyeid as sbyieldProductId, pm3.name as sbyieldProductName , u.sjid as journalId , u.packsjid as packJournalId " +
            " FROM ureptplate u left join products_master pm1 on pm1.id = u.proid " +
            " left join products_master pm2 on pm2.id = u.proid2 " +
            " left join products_master pm3 on pm3.id = u.sbyeid  where u.id =:id LIMIT 1", nativeQuery = true)
    UnitReportMapList findBy(@Param("id") Long id);
   
    @Query(value = " SELECT u.ygroup as productName1,  u.sjid as journalId  FROM ureptplate u", nativeQuery = true)
    List<UnitReportMapList> findAllNameAndSjid();
    
    @Query(value = " SELECT u.ygroup  FROM ureptplate u where u.sjid=:sjid ", nativeQuery = true)
   String findByName(Long sjid);
}
