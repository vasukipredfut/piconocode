package com.aria.dao;

import com.aria.model.BankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankInfoRepository extends JpaRepository<BankInfo, Long> {


    @Query(value = "SELECT * FROM bankinfo b WHERE b.vid =:vendorId ", nativeQuery = true)
    List<BankInfo> findBy(@Param("vendorId") Long vendorId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE bankinfo SET status=null WHERE vid =:vendorId ", nativeQuery = true)
    void updateBankInfo(@Param("vendorId") Long vendorId);


    @Query(value = "SELECT * FROM bankinfo b WHERE b.vid =:vendorId and b.vtable='ledger' and b.status='A' Limit 1", nativeQuery = true)
    Optional<BankInfo> findByVid(@Param("vendorId") Long vendorId);

    @Query(value = "SELECT * FROM bankinfo b WHERE b.vid =:vendorId and b.vtable='ledger' and b.ifsc=:ifsc and b.accno=:accNo Limit 1", nativeQuery = true)
    Optional<BankInfo> findByVid(@Param("vendorId") Long vendorId, @Param("ifsc") String ifsc , @Param("accNo") String accNo);
}
