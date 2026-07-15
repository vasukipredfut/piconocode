package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.dto.OrderResponse;
import com.aria.dto.productPieceList;
import com.aria.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT * FROM dpatch_billorder WHERE ordernumber =:orderNumber AND ( cast(:billDate as date) is null or order_billdate=:billDate ) limit 1 ", nativeQuery = true)
    Optional<Order> findByOrderNumberANDBillDate(@Param("orderNumber") Long orderNumber, @Param("billDate") Date billDate);

    Optional<Order> findByOrderNumber(Long orderNumber);

    Optional<Order> findByBillNo(String billNo);


    @Query(value = "SELECT * FROM dpatch_billorder WHERE order_billno =:billNo AND ( cast(:billDate as date) is null or order_billdate=:billDate ) limit 1 ", nativeQuery = true)
    Optional<Order> findByBillNorANDBillDate(@Param("billNo") String billNo, @Param("billDate") Date billDate);

    @Query(value = "SELECT new com.aria.dto.OrderResponse(d.id,d.orderNumber,d.billNo,d.billDate,l.city,l.name,"
            + " e.employeename,d.orderStatus) FROM com.aria.model.Order d Left join com.aria.model.Vendor l "
            + " on d.vendorId.id=l.id Left join com.aria.model.User e on e.id=d.createdBy.id"
            + " WHERE d.orderStatus= :status and d.dpatchAgainstTypeId= :type and d.active=0 "
            + " AND (:firmId is null or d.firmId=:firmId) "
            + " AND (:branchId is null or d.branchId=:branchId) "
            + " AND (:orderNumber is null or d.orderNumber=:orderNumber) "
            + " AND (cast(:billDate as date) is null or d.billDate=:billDate) "
            + " AND (:billNo is null or lower(d.billNo) like ('%' || lower(:billNo) || '%')) "
            + " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%')) "
            + " AND (:status is null or lower(d.orderStatus) like ('%' || lower(:status) || '%')) "
            + " ORDER BY d.id DESC ")
    public Page<OrderResponse> findAllByStatus(@Param("firmId") Long firmId,
                                               @Param("branchId") Long branchId,
                                               @Param("orderNumber") Long orderNumber,
                                               @Param("billNo") String billNo,
                                               @Param("billDate") Date billDate,
                                               @Param("vendorName") String vendorName,
                                               @Param("status") String status,
                                               @Param("type") Long type,
                                               Pageable pageable);

    @Query(value = "SELECT new com.aria.dto.OrderResponse(d.id, d.orderNumber, d.billNo, d.billDate, l.city, l.name, "
            + " e.employeename, d.orderStatus) FROM com.aria.model.Order d Left join com.aria.model.Vendor l "
            + " on d.vendorId.id=l.id Left join com.aria.model.User e on  e.id=d.createdBy.id"
            + " WHERE d.dpatchAgainstTypeId=1 "
            + " AND (:firmId is null or d.firmId=:firmId) "
            + " AND (:branchId is null or d.branchId=:branchId) "
            + " AND (:orderNumber is null or d.orderNumber=:orderNumber) "
            + " AND (cast(:billDate as date) is null or d.billDate=:billDate) "
            + " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%')) "
            + " AND (:status is null or lower(d.orderStatus) like ('%' || lower(:status) || '%')) "
            + " AND d.active=0 ORDER BY d.id DESC")
    public Page<OrderResponse> getAllAgainstOrder(@Param("firmId") Long firmId,
                                                  @Param("branchId") Long branchId,
                                                  @Param("orderNumber") Long orderNumber,
                                                  @Param("billDate") Date billDate,
                                                  @Param("vendorName") String vendorName,
                                                  @Param("status") String status, Pageable pageable);

    @Query(value = "SELECT new com.aria.dto.OrderResponse(d.id, d.orderNumber, d.billNo, d.billDate, l.city, l.name,"
            + " e.employeename, d.orderStatus) FROM com.aria.model.Order d Left join com.aria.model.Vendor l "
            + " on d.vendorId.id=l.id Left join com.aria.model.User e on e.id=d.createdBy.id"
            + " WHERE d.dpatchAgainstTypeId=2 and d.active=0 "
            + " AND (:firmId is null or d.firmId=:firmId) "
            + " AND (:branchId is null or d.branchId=:branchId) "
            + " AND (cast(:billDate as date) is null or d.billDate=:billDate) "
            + " AND (:billNumber is null or lower(d.billNo) like ('%' || lower(:billNumber) || '%')) "
            + " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%')) "
            + " AND (:status is null or lower(d.orderStatus) like ('%' || lower(:status) || '%')) "
            + " ORDER BY d.id DESC")
    public Page<OrderResponse> getAllorderAgainstBill(@Param("firmId") Long firmId,
                                                      @Param("branchId") Long branchId,
                                                      @Param("billNumber") String billNumber,
                                                      @Param("billDate") Date billDate,
                                                      @Param("vendorName") String vendorName,
                                                      @Param("status") String status, Pageable pageable);

    @Query(value = "SELECT p.product_ref_id as productId, p.no_of_pieces as boxQty, p.lot_pieces as pieceQty FROM product_pieces_count p WHERE p.product_ref_id = :productId", nativeQuery = true)
    List<productPieceList> getPieceQtyList(@Param("productId") Long productId);

}
