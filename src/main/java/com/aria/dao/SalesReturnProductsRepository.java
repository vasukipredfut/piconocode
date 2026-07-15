package com.aria.dao;

import com.aria.model.SalesReturnProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SalesReturnProductsRepository extends JpaRepository<SalesReturnProducts, Long> {


    @Query(value = "SELECT * FROM sales_return_products s WHERE s.return_id =:returnId ", nativeQuery = true)
    List<SalesReturnProducts> findBy ( @Param("returnId") Long returnId);


    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sales_return_products s WHERE s.return_id =:returnId ", nativeQuery = true)
    void deleteList ( @Param("returnId") Long returnId);
}
