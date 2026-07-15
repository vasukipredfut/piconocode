package com.aria.dao;

import com.aria.model.HoSalesInvoice;
import com.aria.model.HoSalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoSalesOrderRepository extends JpaRepository<HoSalesOrder, Long> {

    @Query(value = "SELECT  *  FROM Ho_Sales_Order b WHERE  b.hoId =:hoId Limit 1 ", nativeQuery = true)
    Optional<HoSalesOrder> findBy(@Param("hoId") Long hoId);


}
