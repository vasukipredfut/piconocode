package com.aria.dao;

import com.aria.dto.BlendingRequestList;
import com.aria.dto.CasBinWiseStockList;
import com.aria.model.BlendingAdviceStockDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlendingAdviceStockDetailsRepository extends JpaRepository<BlendingAdviceStockDetails, Long> {


    @Query(value = "SELECT  *  FROM blending_advice_stock_details b WHERE  b.blending_advice_details_id =:blendingAdviceStockId ", nativeQuery = true)
    List<BlendingAdviceStockDetails> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId);

    @Query(value = "SELECT  b.*  FROM blending_advice ba left join blending_advice_details bad on bad.blending_advice_id = ba.id " +
            " left join blending_advice_stock_details b on b.blending_advice_details_id = bad.id " +
            " WHERE  ba.status = 'A' AND bad.status = 'A' AND  ba.journal_id =:journalId AND bad.branch_id =:branchId " +
            " AND b.product_id =:productId AND b.cas_group =:casGroup LIMIT 1", nativeQuery = true)
    Optional<BlendingAdviceStockDetails> findBy(@Param("casGroup") String casGroup,
                                                @Param("branchId") Long branchId,
                                                @Param("productId") Long productId,
                                                @Param("journalId") Long journalId);

    /*
    @Query(value = " select b.binname as binNo,  SUM(b.stkqty) as stkqty  , b.lot as lot  from blending_advice_bin_details ba " +
            " left join binstock b on b.binname = ba.bin_no and b.batchno = ba.batch_no and b.refid = ba.product_id and b.blending_lock = true " +
            " where " +
            " ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  ) " +
            " and ( :branch is null or b.branch =:branch ) " +
            " and b.lot <> 0 " +
            " and ( :productId is null or b.refid = :productId ) group by b.binname , b.lot ", nativeQuery = true)
    List<CasBinWiseStockList> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                     @Param("productId") Long productId,
                                     @Param("branch") Long branch);
   */


    @Query(value = " SELECT t1.binname as binNo , SUM(t1.stkqty) as stkqty, t1.lot as lot   " +
            " FROM (   SELECT distinct b.*     " +
            "       FROM blending_advice_bin_details ba , binstock b     " +
            "       where    " +
            "           b.binname = ba.bin_no and b.batchno = ba.batch_no and b.refid = ba.product_id and ba.mainlot = b.lot     " +
            "           and ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  )    " +
            "           and ( :branch is null or b.branch =:branch )    " +
            "           and b.lot <> 0 and stkqty > 0  and ( b.lockstatus = 'N' OR b.lockstatus IS NULL OR b.lockstatus=''  )    " +
            "           and ( :productId is null or b.refid = :productId )  " +
            " ) as t1 group by t1.binname , t1.lot ", nativeQuery = true)
    List<CasBinWiseStockList> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                     @Param("productId") Long productId,
                                     @Param("branch") Long branch);

    @Query(value = " SELECT t1.binname as binNo , SUM(t1.stkqty) as stkqty, t1.lot as lot   " +
            " FROM (   SELECT distinct b.*     " +
            "       FROM blending_advice_bin_details ba , binstock b     " +
            "       where    " +
            "           b.binname = ba.bin_no and b.batchno = ba.batch_no and b.refid = ba.product_id and ba.mainlot = b.lot     " +
            "           and ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  )    " +
            "           and ( :branch is null or b.branch =:branch )    " +
            "           and b.lot <> 0 and stkqty > 0  and   b.lockstatus = 'Y' and b.locktype =:lockType   " +
            "           and ( :productId is null or b.refid = :productId )  " +
            " ) as t1 group by t1.binname , t1.lot ", nativeQuery = true)
    List<CasBinWiseStockList> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                     @Param("productId") Long productId,
                                     @Param("branch") Long branch,
                                     @Param("lockType") String lockType);


    @Query(value = " SELECT b.id as blendingStockId,  b.product_id as productId , b.cas_group as casgroup, bi.branch_id as issueBranch ,  " +
            "g.dept as issueDeptId , SUM(bi.quantity) as qty , bi.lot as lot , bi.mainlot as mainLot  " +
            "FROM blending_advice_stock_details b " +
            "left join blending_advice_bin_details bi on bi.blending_advice_stock_id = b.id  " +
            "left join gogroup g on g.goname = bi.bin_no   " +
            "WHERE  bi.type = 'external' AND b.blending_advice_details_id =:blendingAdviceDetailId  " +
            "group by b.id , b.product_id ,  bi.branch_id ,  g.dept , bi.lot , b.cas_group , bi.mainlot  ", nativeQuery = true)
    List<BlendingRequestList> findByRequestItem(@Param("blendingAdviceDetailId") Long blendingAdviceDetailId);


    @Query(value = "SELECT  b.product_id as productId ,  bi.branch_id as issueBranch ,  g.dept as issueDeptId , SUM(bi.quantity) as qty  " +
            " FROM blending_advice_stock_details b left join blending_advice_bin_details bi on bi.blending_advice_stock_id = b.id " +
            " left join gogroup g on g.goname = bi.bin_no " +
            " WHERE  bi.type = 'external' AND b.blending_advice_details_id =:blendingAdviceDetailId " +
            " group by b.product_id ,  bi.branch_id ,  g.dept  ", nativeQuery = true)
    List<BlendingRequestList> findByIssueBranchAndDept(@Param("blendingAdviceDetailId") Long blendingAdviceDetailId);


    @Query(value = " SELECT  b.product_id as productId ,  bi.branch_id as issueBranch , g.dept as issueDeptId, bi.batch_no as batchNo,  " +
            "bi.bin_no as binNo ,  SUM(bi.quantity) as qty, bi.lot as lot  , bi.mainlot as mainLot " +
            "FROM blending_advice_stock_details b left join blending_advice_bin_details bi on bi.blending_advice_stock_id = b.id " +
            "left join gogroup g on g.goname = bi.bin_no   " +
            "WHERE  bi.type = 'external' AND b.blending_advice_details_id =:blendingAdviceDetailId " +
            "AND b.product_id =:productId AND bi.branch_id= :issueBranch AND g.dept =:issueDeptId " +
            "group by b.product_id ,  bi.branch_id ,  g.dept , bi.batch_no , bi.bin_no , bi.lot , bi.mainlot  ", nativeQuery = true)
    List<BlendingRequestList> findByRequestItem(@Param("blendingAdviceDetailId") Long blendingAdviceDetailId,
                                                @Param("productId") Long productId,
                                                @Param("issueBranch") Long issueBranch,
                                                @Param("issueDeptId") Long issueDeptId);


    @Query(value = "  SELECT  gd.id as godownId , max(gd.name) as godownName ,  bi.branch_id as issueBranch ,  g.dept as issueDeptId  " +
            " FROM blending_advice_stock_details b left join blending_advice_bin_details bi on bi.blending_advice_stock_id = b.id  " +
            " left join gogroup g on g.goname = bi.bin_no left join godown gd on gd.id = g.gid   " +
            " WHERE lower(bi.type)  = lower('ExternalGodown') AND b.blending_advice_details_id =:blendingAdviceDetailId  " +
            " group by gd.id ,   bi.branch_id ,  g.dept ", nativeQuery = true)
    List<BlendingRequestList> findByRequestGodown(@Param("blendingAdviceDetailId") Long blendingAdviceDetailId);


    @Query(value = "SELECT gd.id as godownId , max(gd.name) as godownName , bi.branch_id as issueBranch , g.dept as issueDeptId ,  " +
            " bi.product_id as productId , bi.batch_no as batchNo , bi.bin_no as binNo, bi.lot as lot , SUM(bi.quantity) as qty  " +
            " FROM blending_advice_stock_details b left join blending_advice_bin_details bi on bi.blending_advice_stock_id = b.id  " +
            " left join gogroup g on g.goname = bi.bin_no left join godown gd on gd.id = g.gid " +
            " WHERE lower(bi.type)= lower('ExternalGodown') AND b.blending_advice_details_id =:blendingAdviceDetailId  " +
            " AND gd.id =:gid AND bi.branch_id= :issueBranch AND g.dept =:issueDeptId " +
            " GROUP BY gd.id , bi.branch_id , g.dept , bi.product_id , bi.batch_no , bi.bin_no , bi.lot", nativeQuery = true)
    List<BlendingRequestList> findByRequestGoDownItems(@Param("blendingAdviceDetailId") Long blendingAdviceDetailId,
                                                       @Param("gid") Long gid, @Param("issueBranch") Long issueBranch,
                                                       @Param("issueDeptId") Long issueDeptId);


    @Query(value = " select ba.bin_no as binNo,  SUM(b.stkqty) as stkqty  , ba.lot as lot  " +
            " from blending_preprocess_request_receive_bin_map  ba    " +
            " left join blending_advice_stock_details basd on " +
            " basd.blending_advice_details_id = ba.blending_detail_id and basd.id = ba.blending_advice_stock_id " +
            " left join binstock b on b.binname = ba.bin_no and b.batchno = ba.batch_no and b.refid = ba.item_id    " +
            " where ( :blendingAdviceStockId is null or basd.id =:blendingAdviceStockId  )    " +
            " and ( :branch is null or ba.branch =:branch )    " +
            " and ( :casgroup is null or ba.casgroup =:casgroup )    " +
            " and ba.lot <> 0 and b.stkqty > 0   " +
            " and ( :productId is null or ba.item_id = :productId ) group by ba.bin_no , ba.lot ", nativeQuery = true)
    List<CasBinWiseStockList> findByPreprocessReceiveBin(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                                         @Param("productId") Long productId,
                                                         @Param("branch") Long branch,
                                                         @Param("casgroup") String casgroup);


    @Query(value = " select t1.binNo as binNo , SUM(t1.stkqty)  as stkqty , t1.lot as lot from   " +
            " ( " +
            "   select  ba.bin_no as binNo,  b.stkqty as stkqty  , ba.lot as lot  from blending_receive_bin_map  ba       " +
            "   left join blending_advice_stock_details basd on basd.blending_advice_details_id = ba.blending_detail_id      " +
            "   left join blending_advice_bin_details babd on babd.blending_advice_stock_id = basd.id and babd.batch_no = ba.batch_no    " +
            "   left join binstock b on b.binname = ba.bin_no and b.batchno = ba.batch_no and b.refid = ba.item_id      " +
            "   where   " +
            "   basd.blending_advice_details_id = ba.blending_detail_id   " +
            "   and babd.blending_advice_stock_id = basd.id and babd.batch_no = ba.batch_no  " +
            "   and ( :blendingAdviceStockId is null or basd.id =:blendingAdviceStockId  )       " +
            "   and ( :branch is null or ba.branch =:branch )       " +
            "   and ( :casgroup is null or basd.cas_group  =:casgroup )       " +
            "   and ba.lot <> 0  and b.stkqty > 0     " +
            "   and ( :productId is null or ba.item_id = :productId ) group by  ba.bin_no , ba.lot , b.stkqty " +
            " ) as t1  " +
            " group by t1.binNo , t1.lot ", nativeQuery = true)
    List<CasBinWiseStockList> findByRequestReceiveBin(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                                      @Param("productId") Long productId,
                                                      @Param("branch") Long branch,
                                                      @Param("casgroup") String casgroup);


    @Query(value = " SELECT distinct bs.* from blending_advice_stock_details bs , blending_advice_bin_details bb   " +
            " WHERE bb.blending_advice_stock_id = bs.id AND bs.product_id = bb.product_id   " +
            " AND bs.blending_advice_details_id =:blendingAdviceDetailId " +
            " AND bs.product_id =:productId AND bb.batch_no =:batchNo limit 1", nativeQuery = true)
    Optional<BlendingAdviceStockDetails> findBy( @Param("blendingAdviceDetailId") Long blendingAdviceDetailId,
                                             @Param("productId") Long productId, @Param("batchNo") String batchNo );




    @Query(value = "SELECT  *  FROM blending_advice_stock_details b WHERE " +
            " b.blending_advice_details_id =:blendingAdviceStockId " +
            " and ( :casgroup is null or b.cas_group  =:casgroup ) limit 1 ", nativeQuery = true)
    Optional<BlendingAdviceStockDetails> findBy(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                                @Param("casgroup") String casgroup);
}
