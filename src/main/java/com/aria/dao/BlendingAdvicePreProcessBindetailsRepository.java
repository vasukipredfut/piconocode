package com.aria.dao;

import com.aria.dto.BlendingPreprocessRequestList;
import com.aria.model.BlendingPreProcessBinDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlendingAdvicePreProcessBindetailsRepository extends JpaRepository<BlendingPreProcessBinDetails, Long> {


    @Query(value = "SELECT  *  FROM blending_advice_preprocess_bin_details b WHERE  b.blending_advice_preprocess_id =:blendingAdvicePreProcessId ", nativeQuery = true)
    List<BlendingPreProcessBinDetails> findBy(@Param("blendingAdvicePreProcessId") Long blendingAdvicePreProcessId);


    @Query(value = " select bp.blending_advice_details_id as blendingdetailsId , bp.cas_group as casgroup ,   " +
            " bpb.bin_no as binNo,  bpb.batchno as batchNo , bpb.batchid as batchId , bpb.lot as lot ,   " +
            " bp.blending_advice_stock_id as blendingAdviceStockId , bad.product_id as productId  " +
            " from blending_advice_preprocess bp   " +
            " left join blending_advice_details bad on bad.id = bp.blending_advice_details_id   " +
            " left join blending_advice_preprocess_bin_details bpb on bpb.blending_advice_preprocess_id =  bp.id   " +
            " WHERE bp.blending_advice_details_id =:blendingdetailsId AND bpb.batchno IS NOT NULL   " +
            " group by bp.blending_advice_details_id, bp.blending_advice_stock_id , bp.cas_group ,   " +
            " bpb.bin_no , bpb.batchno , bpb.batchid  , bpb.lot ,bad.product_id ", nativeQuery = true)
    List<BlendingPreprocessRequestList> findByDetailsId(@Param("blendingdetailsId") Long blendingdetailsId);

}
