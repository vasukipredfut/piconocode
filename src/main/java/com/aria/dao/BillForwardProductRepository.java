package com.aria.dao;

import com.aria.model.BillForwardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillForwardProductRepository extends JpaRepository<BillForwardProduct, Long> {

    @Query(value = "SELECT  *  FROM bill_forward_product b WHERE b.forward_id =:forwardId ", nativeQuery = true)
    List<BillForwardProduct> findBy(@Param("forwardId") Long forwardId);
}
