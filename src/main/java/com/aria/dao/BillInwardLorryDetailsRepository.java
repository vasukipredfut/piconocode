package com.aria.dao;


import com.aria.model.BillInwardLorryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillInwardLorryDetailsRepository extends JpaRepository<BillInwardLorryDetails, Long> {


    @Query(value = "SELECT * FROM bill_lorry_details b WHERE b.inward_id=:inwardId LIMIT 1", nativeQuery = true)
    Optional<BillInwardLorryDetails> findBy(@Param("inwardId") Long inwardId);

    @Query(value = "SELECT * FROM bill_lorry_details b WHERE b.inward_id=:inwardId LIMIT 1", nativeQuery = true)
    List<BillInwardLorryDetails> findByList(@Param("inwardId") Long inwardId);
}
