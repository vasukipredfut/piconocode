package com.aria.dao;



import com.aria.model.EpayMasterNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpayMasterNewRepository extends JpaRepository<EpayMasterNew, Long> {


    @Query(value = "SELECT *  FROM epaymasternew ad WHERE ad.empcode =:userId AND ad.connect_table ='emaster' AND ad.status ='A' ", nativeQuery = true)
    EpayMasterNew findByStaff(@Param("userId") Long userId);

    @Query(value = "SELECT *  FROM epaymasternew ad WHERE ad.empcode =:userId AND ad.connect_table ='emastdaily' AND ad.status ='A' ", nativeQuery = true)
    EpayMasterNew findByWages(@Param("userId") Long userId);

}
