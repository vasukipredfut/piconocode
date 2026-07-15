package com.aria.dao;

import com.aria.model.BillExchangeProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillExchangeProductRepository extends JpaRepository<BillExchangeProduct, Long> {


    @Query(value = "SELECT  *  FROM bill_exchange_product b WHERE b.exchange_id =:exchangeId ", nativeQuery = true)
    List<BillExchangeProduct> findBy(@Param("exchangeId") Long exchangeId);
}
