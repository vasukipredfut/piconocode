package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.ProductPieceQty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductPieceCountRepository extends JpaRepository<ProductPieceQty, Long> {

    @Query(value = "select * from product_pieces_count where product_ref_id =:id Limit 1", nativeQuery = true)
    ProductPieceQty findByProductId(@Param("id") Long id);


}
