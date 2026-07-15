package com.aria.dao;

import com.aria.model.DpEntryStoredBinDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DpEntryStoredBinDetailsRepository extends JpaRepository<DpEntryStoredBinDetails, Long> {



    @Query(value = "SELECT  *  FROM direct_purchase_stored_bin b WHERE b.entry_product_id =:entryProductId and b.bin_id =:binId", nativeQuery = true)
    Optional<DpEntryStoredBinDetails> findByProductBatch(@Param("entryProductId") Long entryProductId, @Param("binId") Long binId);

    @Query(value = "SELECT  b.bin_id FROM direct_purchase_stored_bin b WHERE b.entry_product_id =:entryProductId ", nativeQuery = true)
    List<String> findByProduct(@Param("entryProductId") Long entryProductId);

    @Query(value = "SELECT  * FROM direct_purchase_stored_bin b WHERE b.entry_product_id =:entryProductId ", nativeQuery = true)
    List<DpEntryStoredBinDetails> findByProductId(@Param("entryProductId") Long entryProductId);
}
