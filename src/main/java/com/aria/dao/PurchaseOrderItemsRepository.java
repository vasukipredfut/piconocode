package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.PurchaseOrderItems;

@Repository
public interface PurchaseOrderItemsRepository extends JpaRepository<PurchaseOrderItems, Long> {


    @Query(value = "SELECT SUM(final_order_qty)  FROM purchase_order_items bi WHERE bi.purchase_order_id=:poId", nativeQuery = true)
    Double sumOfPoQty(@Param("poId") Long poId);

    @Query(value = "SELECT  COALESCE(SUM(final_order_qty),0.0)  FROM purchase_order_items p WHERE p.purchase_order_id=:poId AND p.item_id = :itemId ", nativeQuery = true)
    Double sumOfPoQty(@Param("poId") Long poId, @Param("itemId") Long itemId);

    @Query(value = "SELECT *  FROM purchase_order_items bi WHERE bi.purchase_order_id=:poId AND bi.ITEM_ID =:itemId LIMIT 1 ", nativeQuery = true)
    Optional<PurchaseOrderItems> findBy(@Param("poId") Long poId, @Param("itemId") Long itemId);

    @Query(value = "SELECT *  FROM purchase_order_items bi WHERE bi.purchase_order_id=:poId ", nativeQuery = true)
    List<PurchaseOrderItems> findBy(@Param("poId") Long poId);

    @Query(value = "SELECT *  FROM purchase_order_items bi WHERE bi.purchase_order_id=:poId AND bi.id =:id  ", nativeQuery = true)
    Optional<PurchaseOrderItems> findById(@Param("poId") Long poId, @Param("id") Long id);

    @Query(value = "SELECT *  FROM purchase_order_items poi WHERE poi.purchase_order_id=:poId AND  (poi.po_inward_status IS NULL OR poi.po_inward_status='Open') ", nativeQuery = true)
    List<PurchaseOrderItems> findByInwardStatus(@Param("poId") Long poId);

    @Query(value = "SELECT *  FROM purchase_order_items bi WHERE bi.purchase_order_id=:poId AND bi.ITEM_ID =:itemId AND bi.final_order_price =:price  LIMIT 1 ", nativeQuery = true)
    Optional<PurchaseOrderItems> findBy(@Param("poId") Long poId, @Param("itemId") Long itemId, @Param("price") Double price);

}
