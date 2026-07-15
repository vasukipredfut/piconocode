package com.aria.dao;

import com.aria.model.SalesReturnBinDetails;
import com.aria.model.SalesReturnProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SalesReturnBinDetailsRepository extends JpaRepository<SalesReturnBinDetails, Long> {


    @Query(value = "SELECT *  FROM sales_return_bin_details s WHERE s.return_product_Id =:returnId ", nativeQuery = true)
    List<SalesReturnBinDetails> findBy(@Param("returnId") Long returnId);

    @Transactional
    @Modifying
    @Query(value = "DELETE  FROM sales_return_bin_details s WHERE s.return_product_Id =:returnProductId ", nativeQuery = true)
    void deleteAll(@Param("returnProductId") Long returnId);
}
