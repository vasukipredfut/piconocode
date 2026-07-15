package com.aria.dao;

import com.aria.model.No2ProductsPartyMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2billitems;

import java.util.List;

@Repository
public interface No2billitemsRepository extends JpaRepository<No2billitems, Long> {


    @Query(value = "SELECT *  FROM no2_bill_items pm WHERE pm.bill_id =:billId ", nativeQuery = true)
    List<No2billitems> findAllByBillId (@Param("billId") Long billId );

}
