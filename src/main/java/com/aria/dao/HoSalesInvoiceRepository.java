package com.aria.dao;


import com.aria.model.HoSalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoSalesInvoiceRepository extends JpaRepository<HoSalesInvoice,Long> {


    @Query(value = "SELECT  *  FROM Ho_Sales_Invoice b WHERE  b.hoId =:hoId Limit 1 ", nativeQuery = true)
    Optional<HoSalesInvoice> findBy(@Param("hoId") Long hoId);


}
