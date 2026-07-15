package com.aria.dao;

import com.aria.model.DeliteStockMoveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliteStockMoveRequestRepository extends JpaRepository<DeliteStockMoveRequest, Long> {

    @Query(value="SELECT dsr FROM DeliteStockMoveRequest dsr WHERE "
            + "(:fromBranchId is null or dsr.fromBranchId = :fromBranchId) "
            + "AND (:toBranchId is null or dsr.toBranchId = :toBranchId) "
            + "AND (:status is null OR lower(dsr.activationStatus)  LIKE (lower(:status))) "
            + "order by dsr.id desc")
    Page<DeliteStockMoveRequest> findAllByBranchAndStatusPagination(Long fromBranchId, Long toBranchId, String status, Pageable pageable);

    @Query(value="SELECT * FROM delitestock_moverequest d WHERE d.kalasal_to_date < :toDate ", nativeQuery = true)
    List<DeliteStockMoveRequest> findAllByToDate(@Param("toDate")Date toDate);





}
