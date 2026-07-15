package com.aria.dao;

import com.aria.dto.SplitProductBatchList;
import com.aria.model.SplitProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitProductDetailsRepository extends JpaRepository<SplitProductDetails, Long> {



    @Query(value = "SELECT * FROM split_product_details s WHERE (:splitTrnId is null OR s.split_trn_id =:splitTrnId) ", nativeQuery = true)
    List<SplitProductDetails> viewSplitProductDetails(@Param("splitTrnId") Long splitTrnId);

    @Query(value = "SELECT s.bsk_batch_no as batchNo , s.selected_qty as qty , s.palletnumber as palletNo  " +
            "FROM split_product_details s WHERE (:splitTrnId is null OR s.split_trn_id =:splitTrnId) AND s.palletNumber is not null AND s.palletNumber <> '' " +
            "group by s.bsk_batch_no , s.selected_qty , s.palletnumber ", nativeQuery = true)
    List<SplitProductBatchList> findBy(@Param("splitTrnId") Long splitTrnId);

    @Query(value = "SELECT s.bsk_batch_no as batchNo , s.selected_qty as qty , s.palletnumber as palletNo FROM split_product_details s WHERE (:splitTrnId is null OR s.split_trn_id =:splitTrnId) " +
            "group by s.bsk_batch_no , s.selected_qty, s.palletnumber  ", nativeQuery = true)
    List<SplitProductBatchList> findByBatchWithQty(@Param("splitTrnId") Long splitTrnId);



}
