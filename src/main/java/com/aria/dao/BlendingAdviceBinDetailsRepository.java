package com.aria.dao;

import com.aria.dto.BlendingBinList;
import com.aria.model.BlendingAdviceBinDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BlendingAdviceBinDetailsRepository extends JpaRepository<BlendingAdviceBinDetails, Long> {

    @Query(value = "SELECT  *  FROM blending_advice_bin_details b WHERE  b.blending_advice_stock_id =:blendingAdviceStockId AND b.product_id =:productId AND branch_Id=:branchId", nativeQuery = true)
    List<BlendingAdviceBinDetails> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId, @Param("productId") Long productId, @Param("branchId") Long branchId);

    @Query(value = "SELECT  *  FROM blending_advice_bin_details b WHERE  b.blending_advice_stock_id =:blendingAdviceStockId ", nativeQuery = true)
    List<BlendingAdviceBinDetails> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId);


    @Query(value = "SELECT max(id) as id , bin_no , blending_advice_stock_id, branch_id , SUM(quantity) as quantity , type , product_id, batch_no FROM blending_advice_bin_details b " +
            " WHERE  b.blending_advice_stock_id =:blendingAdviceStockId " +
            " AND b.product_id =:productId " +
            " AND branch_Id=:branchId " +
            " GROUP BY bin_no, blending_advice_stock_id, branch_id , type , product_id ,batch_no ", nativeQuery = true)
    List<BlendingBinList> findByGroupList(@Param("blendingAdviceStockId") Long blendingAdviceStockId, @Param("productId") Long productId, @Param("branchId") Long branchId);


    @Query(value = "SELECT  distinct  babd.bin_no  from blending_advice_details bad   " +
            "left join  blending_advice_stock_details basd on basd.blending_advice_details_id = bad.id    " +
            "left join  blending_advice_bin_details babd on babd.blending_advice_stock_id = basd.id    " +
            "WHERE  bad.id =:blendingAdviceId ", nativeQuery = true)
    List<Long> findByBinNo(@Param("blendingAdviceId") Long blendingAdviceId);


}
