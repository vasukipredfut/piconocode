package com.aria.dao;

import com.aria.dto.BlendingBinList;
import com.aria.model.BlendingRequestReceiveBinMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlendingRequestReceiveBinMapRepository extends JpaRepository<BlendingRequestReceiveBinMap, Long> {


    @Query(value = "SELECT * FROM blending_receive_bin_map b " +
            " WHERE b.type_Id =:typeId AND b.trn_no=:trnNo " +
            " AND b.item_id=:itemId AND b.bin_no=:binNo " +
            " AND b.batch_no=:batchNo AND b.type=:type AND b.lot =:lot LIMIT 1 ", nativeQuery = true)
    Optional<BlendingRequestReceiveBinMap> findByReceive(@Param("typeId") Long typeId,
                                                               @Param("trnNo") String trnNo,
                                                               @Param("itemId") Long itemId,
                                                               @Param("binNo") Long binNo,
                                                               @Param("batchNo") String batchNo,
                                                               @Param("lot") Double lot,
                                                               @Param("type") String type);



    @Query(value = " SELECT b.bin_no as bin_no, b.branch as branch_id , b.qty as quantity , b.item_id as product_id , b.batch_no as batch_no , b.type as type  " +
            " FROM blending_receive_bin_map b WHERE (:blendingDetailsId is null OR b.blending_detail_id =:blendingDetailsId ) " , nativeQuery = true)
    List<BlendingBinList> findBy(@Param("blendingDetailsId") Long blendingDetailsId);


}
