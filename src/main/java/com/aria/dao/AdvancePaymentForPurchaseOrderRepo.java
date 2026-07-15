package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.AdvancePaymentForPurchaseOrder;

public interface AdvancePaymentForPurchaseOrderRepo extends JpaRepository<AdvancePaymentForPurchaseOrder, Long> {
    public Page<AdvancePaymentForPurchaseOrder> findAll(Pageable page);

    @Query(value = "select * from purchase_advance_payment pap WHERE  pap.purchase_order_ref_id =:poId AND  pap.vendor_ref_id =:vendorId AND pap.status ='Completed' ", nativeQuery = true)
    List<AdvancePaymentForPurchaseOrder> findByPurchaseOrderAdvancePayment(@Param("poId") Long poId,
                                                                           @Param("vendorId") Long vendorId);

    @Query(value = "select * from purchase_advance_payment pap WHERE pap.purchase_order_ref_id =:poId AND pap.purchase_payment_id =:paymentId AND pap.payment_category_id =:categoryId ", nativeQuery = true)
    Optional<AdvancePaymentForPurchaseOrder> findByPurchaseOrderAdvancePaymentByPaymentId(@Param("poId") Long poId,
                                                                                          @Param("paymentId") Long paymentId, @Param("categoryId") Long categoryId);


    @Query(value = "select * from purchase_advance_payment pap WHERE pap.purchase_order_ref_id =:poId AND pap.purchase_payment_id =:paymentId  ", nativeQuery = true)
    Optional<AdvancePaymentForPurchaseOrder> findByPurchaseOrderAdvancePaymentByPaymentId(@Param("poId") Long poId, @Param("paymentId") Long paymentId);

    @Query(value = "select * from purchase_advance_payment pap WHERE  pap.purchase_order_ref_id =:poId ", nativeQuery = true)
    List<AdvancePaymentForPurchaseOrder> findByPurchaseOrderAdvancePayment(@Param("poId") Long poId);

    @Query(value = "SELECT COALESCE(SUM(advance_amount),0.0)  from purchase_advance_payment pap where pap.purchase_order_ref_id =:poId AND ( pap.status <> 'Pending' ) ", nativeQuery = true)
    public Double sumOfAdvanceAmount(@Param("poId") Long poId);

    @Query(value = "SELECT COALESCE(SUM(pap.percentage),0.0) from purchase_advance_payment pap where pap.purchase_order_ref_id =:poId  AND pap.status = 'Completed'", nativeQuery = true)
    public Double sumOfPercentages(@Param("poId") Long poId);

    @Query(value = "select * from purchase_advance_payment pap WHERE  pap.purchase_order_ref_id =:poId AND pap.status = 'Pending' ", nativeQuery = true)
    List<AdvancePaymentForPurchaseOrder> findByPurchaseOrderAdvancePaymentPendingList(@Param("poId") Long poId);

    @Query(value = " SELECT cp.* " + "FROM ( "
            + "    SELECT * FROM purchase_advance_payment WHERE lower(status) = 'authorized' " + "    UNION ALL "
            + "    SELECT * FROM ( " + "        SELECT DISTINCT ON (purchase_order_ref_id) * "
            + "        FROM purchase_advance_payment "
            + "        WHERE lower(status) IN ('pending', 'completed', 'activated', 'verified', 'approved') "
            + "        ORDER BY purchase_order_ref_id, purchase_advance_payment_id " + "    ) AS other_status_payments "
            + " ) AS cp " + "LEFT JOIN purchase_order po ON po.id = cp.purchase_order_ref_id "
            + " LEFT JOIN ledger ven ON ven.id = po.vendor_id " + " WHERE "
            + "    (:poNumber IS NULL OR lower(po.po_no) LIKE ('%' || lower(:poNumber) || '%')) "
            + "    AND (:status IS NULL OR lower(cp.status) LIKE ('%' || lower(:status) || '%')) "
            + "    AND (:trnNo IS NULL OR lower(cp.trnno) LIKE ('%' || lower(:trnNo) || '%')) "
            + "    AND (:vendorId IS NULL OR ven.id = :vendorId) "
            + "    AND (CAST(:fromDt AS DATE) IS NULL OR po.po_date >= :fromDt) "
            + "    AND (:frimId IS NULL OR cp.firm_id = :frimId) "
            + "    AND (:branch IS NULL OR cp.branch_id = :branch) " + "    AND (:dept IS NULL OR cp.dept_id = :dept) "
            + "    AND (CAST(:toDt AS DATE) IS NULL OR po.po_date <= :toDt) "
            + " ORDER BY cp.purchase_advance_payment_id DESC", countQuery = "SELECT COUNT(*) " + "FROM ( "
            + "    SELECT * FROM purchase_advance_payment WHERE lower(status) = 'authorized' "
            + "    UNION ALL " + "    SELECT * FROM ( "
            + "        SELECT DISTINCT ON (purchase_order_ref_id) * " + "        FROM purchase_advance_payment "
            + "        WHERE lower(status) IN ('pending', 'completed', 'activated', 'verified', 'approved') "
            + "        ORDER BY purchase_order_ref_id, purchase_advance_payment_id "
            + "    ) AS other_status_payments " + ") AS cp "
            + " LEFT JOIN purchase_order po ON po.id = cp.purchase_order_ref_id "
            + " LEFT JOIN ledger ven ON ven.id = po.vendor_id " + "WHERE "
            + "    (:poNumber IS NULL OR lower(po.po_no) LIKE ('%' || lower(:poNumber) || '%')) "
            + "    AND (:status IS NULL OR lower(cp.status) LIKE ('%' || lower(:status) || '%')) "
            + "    AND (:trnNo IS NULL OR lower(cp.trnno) LIKE ('%' || lower(:trnNo) || '%')) "
            + "    AND (:vendorId IS NULL OR ven.id = :vendorId) "
            + "    AND (CAST(:fromDt AS DATE) IS NULL OR po.po_date >= :fromDt) "
            + "    AND (:frimId IS NULL OR cp.firm_id = :frimId) "
            + "    AND (:branch IS NULL OR cp.branch_id = :branch) "
            + "    AND (:dept IS NULL OR cp.dept_id = :dept) "
            + "    AND (CAST(:toDt AS DATE) IS NULL OR po.po_date <= :toDt)", nativeQuery = true)
    Page<AdvancePaymentForPurchaseOrder> filterByPOAdvancePayment(@Param("poNumber") String poNumber,
                                                                  @Param("vendorId") Long vendorId, @Param("status") String status,
                                                                  @Param("trnNo") String trnNo, @Param("fromDt") Date fromDt,
                                                                  @Param("toDt") Date toDt, @Param("frimId") Long frimId,
																  @Param("dept") Long dept,
                                                                  @Param("branch") Long branch, Pageable page);

    @Transactional
    @Modifying
    @Query(value = "UPDATE purchase_advance_payment SET pending_amount = :pendingAmount WHERE purchase_order_ref_id = :purchaseOrderRefId AND status = 'Pending'", nativeQuery = true)
    void updatePendingAmount(@Param("pendingAmount") Double pendingAmount,
                             @Param("purchaseOrderRefId") Long purchaseOrderRefId);

}
