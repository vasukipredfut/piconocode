package com.aria.dao;

import com.aria.model.ISO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISORepository extends JpaRepository <ISO,Long> {

    @Query(value = "SELECT i.iso_no FROM iso i WHERE i.module_type =:moduleType", nativeQuery = true)
     String getISONo(@Param("moduleType") String moduleType);

}
