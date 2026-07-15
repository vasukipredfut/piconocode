package com.aria.dao;

import com.aria.model.BinLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BinLockRepository extends JpaRepository<BinLock, Long> {

    @Query(value = "SELECT id, batchid, batchno, bin_no, created_by, created_date, inward_date, lock_date, "
            + " lock_status, lock_type, lorry_serial_no, quantity, state, updated_by, updated_date, employee_id, "
            + " product_id FROM binlock bin WHERE (:productId is null or bin.product_id = :productId) "
            + " AND (:binNo is null or (lower(bin.bin_no) LIKE ('%' || lower(:binNo) || '%')))  "
            + " AND (:batchNo is null or bin.batchno = :batchNo) "
            + " AND (:lockType is null or bin.lock_type = :lockType) "
            + " AND (:lockStatus is null or bin.lock_status = :lockStatus) ", nativeQuery = true)
    Page<BinLock> filterBillBinLockList(@Param("productId") Long productId,
                                        @Param("binNo") String binNo,
                                        @Param("batchNo") String batchNo,
                                        @Param("lockType") String lockType,
                                        @Param("lockStatus") String status,
                                        Pageable pageable);


    @Query(value = "SELECT * FROM binlock bin WHERE (:productId is null or bin.product_id = :productId) "
            + " AND (:binNo is null or (lower(bin.bin_no) LIKE ('%' || lower(:binNo) || '%')))  "
            + " AND (:batchNo is null or bin.batchno = :batchNo) "
            + " AND (bin.lock_status = 'Lock') ", nativeQuery = true)
    Optional<BinLock> findBy(@Param("productId") Long productId, @Param("batchNo") String batchNo, @Param("binNo") String binNo);



    @Query(value = "SELECT * FROM binlock bin " +
            " WHERE  (:binNo is null or (lower(bin.bin_no) LIKE ('%' || lower(:binNo) || '%')))  "
            + " AND (bin.lock_status = 'Lock') ", nativeQuery = true)
    Optional<BinLock> findBy( @Param("binNo") String binNo);


    @Query(value = "SELECT * FROM binlock bin " +
            " WHERE  bin.bin_no =:binNo AND (bin.lock_type = 'DT') AND (bin.lock_status = 'Lock') ", nativeQuery = true)
    Optional<BinLock> findByDTBinLock( @Param("binNo") String binNo);


}
