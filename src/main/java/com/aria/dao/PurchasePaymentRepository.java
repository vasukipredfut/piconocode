package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.PurchaseOrderItems;
import com.aria.model.PurchasePayment;

@Repository
public interface PurchasePaymentRepository extends JpaRepository<PurchasePayment, Long> {


    @Query(value = "SELECT * FROM purchase_order_payment po WHERE po.purchase_order_id =:poId AND po.payment_category_id =:paymentCategoryId LIMIT 1", nativeQuery = true)
    Optional<PurchasePayment> getPurchasePayment(@Param("poId") Long poId, @Param("paymentCategoryId") Long paymentCategoryId );

    @Query(value = "SELECT * FROM purchase_order_payment po WHERE po.purchase_order_id =:poId", nativeQuery = true)
    List<PurchasePayment> getPurchasePayment(@Param("poId") Long poId);


    @Query(value = "SELECT * FROM purchase_order_payment po WHERE po.purchase_order_id =:poId and payment_status=0", nativeQuery = true)
    List<PurchasePayment> getPurchasePaymentListByPoIDANDStatus(@Param("poId") Long poId);


    @Query(value = "SELECT COALESCE(SUM(pop.percentage),0.0) from purchase_order_payment pop where pop.purchase_order_id =:poId  AND pop.status = 1", nativeQuery = true)
    public Double  sumOfPercentages(@Param("poId") Long poId);




}
