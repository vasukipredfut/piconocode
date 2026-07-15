package com.aria.dao;

import com.aria.dto.BlendingBinList;
import com.aria.model.BlendingPreprocessRequestAndReceiveBinMap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BlendingPreprocessRequestAndReceiveBinMapRepository extends JpaRepository<BlendingPreprocessRequestAndReceiveBinMap, Long> {


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " LEFT JOIN blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE bad.branch_id =:branch AND bad.status ='A' AND b.bin_no =:binNo AND b.batch_no=:batchNo " +
            " AND b.batch_id=:batchId AND b.item_id=:itemId AND b.lot=:lot AND (b.status='Pending' or b.status IS NULL ) limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBin(@Param("branch") Long branch,
                                                                            @Param("binNo") Long binNo,
                                                                            @Param("batchNo") String batchNo,
                                                                            @Param("batchId") Long batchId,
                                                                            @Param("itemId") Long itemId,
                                                                            @Param("lot") Double lot);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE bad.branch_id =:branch AND bad.status ='A' AND b.bin_no =:binNo AND b.batch_no=:batchNo " +
            " AND b.batch_id=:batchId AND b.item_id=:itemId AND b.lot=:lot AND b.status='Updated'   limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBinWithStatus(@Param("branch") Long branch,
                                                                                      @Param("binNo") Long binNo,
                                                                                      @Param("batchNo") String batchNo,
                                                                                      @Param("batchId") Long batchId,
                                                                                      @Param("itemId") Long itemId,
                                                                                      @Param("lot") Double lot);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE  bad.branch_id =:branch AND bad.status ='A' AND b.bin_no =:binNo AND b.batch_no=:batchNo " +
            " AND b.batch_id=:batchId AND b.item_id=:itemId AND b.lot=:lot limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBinWithOutStatus(@Param("branch") Long branch,
                                                                                         @Param("binNo") Long binNo,
                                                                                         @Param("batchNo") String batchNo,
                                                                                         @Param("batchId") Long batchId,
                                                                                         @Param("itemId") Long itemId,
                                                                                         @Param("lot") Double lot);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE  bad.branch_id =:branch AND bad.status ='A' AND  b.batch_no=:batchNo " +
            " AND b.item_id=:itemId AND b.lot=:lot limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessWithOutBin(@Param("branch") Long branch,
                                                                                         @Param("batchNo") String batchNo,
                                                                                         @Param("itemId") Long itemId,
                                                                                         @Param("lot") Double lot);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE  bad.branch_id =:branch AND bad.status ='A' AND  b.batch_no=:batchNo " +
            " AND b.item_id=:itemId  limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessWithOutLot(@Param("branch") Long branch,
                                                                                   @Param("batchNo") String batchNo,
                                                                                   @Param("itemId") Long itemId);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE bad.status ='A' and b.bin_no =:binNo AND b.batch_no=:batchNo " +
            " AND b.batch_id=:batchId  and (b.status='Pending' or b.status IS NULL )  limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBinWithOutProduct(@Param("binNo") Long binNo,
                                                                                          @Param("batchNo") String batchNo,
                                                                                          @Param("batchId") Long batchId);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE bad.status ='A' AND b.bin_no =:binNo AND b.batch_no=:batchNo " +
            " AND (b.status='Pending' or b.status IS NULL )  limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBinWithOutProduct(@Param("binNo") Long binNo,
                                                                                          @Param("batchNo") String batchNo);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " left join blending_advice_details bad on bad.id = b.blending_detail_id " +
            " WHERE bad.status ='A' AND b.batch_no=:batchNo AND (b.status='Pending' or b.status IS NULL )  limit 1 ", nativeQuery = true)
    Optional<BlendingPreprocessRequestAndReceiveBinMap> findByPreProcessBinWithOutProduct(@Param("batchNo") String batchNo);


    @Query(value = " SELECT b.bin_no as bin_no, b.branch as branch_id , b.qty as quantity , b.item_id as product_id , b.batch_no as batch_no ,  b.batch_id as batch_id  " +
            " FROM blending_preprocess_request_receive_bin_map b WHERE (:blendingDetailsId is null OR b.blending_detail_id =:blendingDetailsId )   ", nativeQuery = true)
    List<BlendingBinList> findBy(@Param("blendingDetailsId") Long blendingDetailsId);


    @Query(value = " SELECT * FROM blending_preprocess_request_receive_bin_map b " +
            " WHERE b.trn_no =:trnNo AND b.batch_no=:batchNo and b.status='updated' ", nativeQuery = true)
    List<BlendingPreprocessRequestAndReceiveBinMap> findBy(@Param("trnNo") String trnNo, @Param("batchNo") String batchNo);

}
