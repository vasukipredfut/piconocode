package com.aria.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesReturn;

import java.util.Date;
import java.util.Optional;


@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {


    @Query(value = "SELECT  s.*  FROM sales_return s left join ledger l on l.id = s.vendor_id "
            + " WHERE (:referenceNumber is null or (lower(s.refno) LIKE ('%' || lower(:referenceNumber) || '%')) ) "
            + " AND (:vendorName is null or (lower(l.name) LIKE ('%' || lower(:vendorName) || '%')) ) "
            + " AND (:type is null or (lower(s.return_type) LIKE ('%' || lower(:type) || '%')) ) "
            + " AND (cast(:dateFrom as date) is null or (s.trndate >= :dateFrom)) "
            + " AND (cast(:dateTo as date) is null or (s.trndate <= :dateTo))  order by s.id desc   " , nativeQuery = true)
    Page<SalesReturn> findBy(@Param("type") String type,
                             @Param("referenceNumber") String referenceNumber,
                             @Param("vendorName") String vendorName ,
                             @Param("dateFrom") Date dateFrom,
                             @Param("dateTo") Date dateTo,
                             Pageable pageable);


    @Query(value = "SELECT * FROM sales_return s  WHERE s.trnno=:trnNo ", nativeQuery = true)
    Optional<SalesReturn> findBy(@Param("trnNo") String trnno);

}
