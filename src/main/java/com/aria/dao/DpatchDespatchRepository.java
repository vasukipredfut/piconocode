package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.DpatchDespatch;

import java.util.Date;

public interface DpatchDespatchRepository extends JpaRepository<DpatchDespatch, Long> {

    @Query(value = "Select * from dpatch_despatch where active_flag =:active and status =:status ORDER BY id DESC", nativeQuery = true)
    public Page<DpatchDespatch> findAllApprove(String status, Integer active, Pageable page);

    @Query(value = "SELECT d.* FROM dpatch_despatch d LEFT JOIN dpatch_billorder db ON d.order_ref_id = db.id "
			+ " LEFT JOIN ledger l ON l.id = db.vendor_id "
            + " WHERE db.dpatch_against_type_id = :type "
			+ " AND (:firmId is null or d.firm_id=:firmId) "
			+ " AND (:branchId is null or d.branch_id=:branchId) "
			+ " AND (:orderNumber is null or db.ordernumber=:orderNumber) "
			+ " AND (cast(:dispatchDate as date) is null or d.trndate=:dispatchDate) "
			+ " AND (:billNo is null or lower(db.order_billno) like ('%' || lower(:billNo) || '%')) "
			+ " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%')) "
			+ " AND (:status is null or lower(d.status) like ('%' || lower(:status) || '%')) "
            + " ORDER BY d.id DESC ", nativeQuery = true)
    public Page<DpatchDespatch> findAllByStatus(@Param("firmId") Long firmId,
                                                @Param("branchId") Long branchId,
                                                @Param("orderNumber") Long orderNumber,
                                                @Param("billNo") String billNo,
                                                @Param("dispatchDate") Date dispatchDate,
                                                @Param("vendorName") String vendorName,
                                                @Param("status") String status,
                                                @Param("type") Long type,
                                                Pageable pageable);

}
