package com.aria.dao;

import com.aria.dto.BlendingBatchBin;
import com.aria.model.BlendingStock;
import com.aria.model.BlendingStockDetails;
import com.aria.model.BranchStockSum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlendingStockDetailsRepostiory extends JpaRepository<BlendingStockDetails, Long> {

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId and b.batchno =:batchNo and b.binid =:binId and b.binname =:binname", nativeQuery = true)
    Optional<BlendingStockDetails> findByProductBatchAndBin(@Param("productId") Long productId,
                                                            @Param("batchNo") String batchNo, @Param("binId") String binId, @Param("binname") Long binname);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId and b.batchno =:batchNo and b.binid =:binId ", nativeQuery = true)
    List<BlendingStockDetails> findByProductBatchAndBin(@Param("productId") Long productId,
                                                        @Param("batchNo") String batchNo, @Param("binId") String binId);

    @Query(value = "SELECT  *  FROM binstock b WHERE  b.batchno =:batchNo ", nativeQuery = true)
    List<BlendingStockDetails> findByProductBatchNo(@Param("batchNo") String batchNo);

    @Query(value = "SELECT  b.branch as branchId,  SUM(stkqty) as qtySum  FROM binstock b WHERE b.refid =:productId GROUP BY b.branch ", nativeQuery = true)
    List<BranchStockSum> findByOverAllQty(@Param("productId") Long productId);

    @Query(value = "SELECT  SUM(b.stkqty) as qty, b.branch, b.locktype as lockType FROM binstock b WHERE b.refid =:productId and b.lockstatus = 'Y' and b.locktype is NOT NULL  AND (b.mrnstatus IS NULL or b.mrnstatus = 'N')   GROUP BY b.branch, b.locktype ", nativeQuery = true)
    List<BlendingStock> findByLOCK(@Param("productId") Long productId);

    @Query(value = "SELECT  SUM(b.stkqty) as qty, b.branch, b.category  FROM binstock b WHERE b.refid =:productId and (b.lockstatus IS NULL OR b.lockstatus NOT IN ('Y')) and b.category is NOT NULL GROUP BY b.branch, b.category", nativeQuery = true)
    List<BlendingStock> findByStateWiseLOCK(@Param("productId") Long productId);


    @Query(value = "SELECT  b.branch as branchId, SUM(stkqty) as qtySum FROM binstock b WHERE  b.refid =:productId and b.category =:category GROUP BY b.branch, b.category ", nativeQuery = true)
    List<BranchStockSum> findByOilFFQty(@Param("productId") Long productId, @Param("category") String category);

    @Query(value = "SELECT b.branch as branchId, count(distinct b.batchno) as sumOfCount from binstock b WHERE b.stkQty > 0 AND b.refid =:productId and b.locktype =:lockType GROUP BY b.branch , b.locktype ", nativeQuery = true)
    List<BranchStockSum> findByLockTypeCount(@Param("productId") Long productId, @Param("lockType") String lockType);

    @Query(value = "SELECT  b.branch as branchId, SUM(stkqty) as qtySum  FROM binstock b WHERE b.refid =:productId and b.oil_ffa >= 1.5  GROUP BY b.branch ", nativeQuery = true)
    List<BranchStockSum> findByOilFFQty2(@Param("productId") Long productId);
/*

    
    @Query(value = "SELECT SUM(b.stkQty) as qty, b.refid as productId, b.batchNo as batchNo, b.batchId as batchId, b.branch as branch, " +
            " max(br.name) as branchName , b.binId as binNo, b.binName as binName , Max(gd.id) as gid , max(gd.external) as godownType," +
            " max(gd.name) as godownName ,b.category as category, b.lockType as lockType, b.inwarddate as inwardDate,  b.lot as lot, " +
            " b.oil_ffa as FFA, b.location as location, b.camp_name as campName,  b.seedcolor as seedcolor , " +
            " ( case WHEN (ba.bin_no IS NULL OR ba.bin_no = 0) then false else true end ) as preprocess , " +
            " b.seedmoisture as seedMoisture, b.blending_lock as blendingLock  FROM binstock b " +
            " LEFT join blending_advice_preprocess_bin_details ba on ba.bin_no = b.binname  " +
            " LEFT JOIN branch br ON br.id = b.branch LEFT JOIN gogroup g ON g.goname = b.binname " +
            " LEFT JOIN godown gd ON gd.id = g.gid " +
            " WHERE  (b.mrnstatus IS NULL or b.mrnstatus = 'N') AND b.refId =:productId " +
            " AND (:category is null or  b.category= :category) " +
            " AND (:branch is null or br.id = :branch) " +
            " AND (:ffa is null or b.oil_ffa = :ffa) " +
            " AND (:status is null or b.lockStatus = :status) " +
            " AND (:lockType is null or b.lockType = :lockType) " +
            " AND (b.stkQty > 0)  " +
            " GROUP BY  b.refid , b.branch ,  b.batchNo , b.batchId , b.binId , b.binName , " +
            " b.category , b.lockType , b.inwarddate ,  b.lot , b.oil_ffa , b.location , b.camp_name ,  b.seedcolor , " +
            " b.blending_lock, ba.bin_no, b.seedmoisture   order by b.inwarddate asc " , nativeQuery = true)
    List<BlendingStock> findBinStockDetails( @Param("productId") Long productId,
                                             @Param("category") String category,
                                             @Param("ffa") Double ffa,
                                             @Param("branch") Long branch,
                                             @Param("lockType") String lockType,
                                             @Param("status") String status);*/

    
    @Query(value = " SELECT SUM(t1.qty) as qty, t1.productId, t1.batchNo, t1.batchId, t1.branch,  " +
            " t1.branchName , t1.binNo, t1.binName , t1.silo ,  t1.gid , t1.godownType, " +
            " t1.godownName ,t1.category, t1.lockType, t1.inwardDate, t1.lot,  " +
            " t1.FFA, t1.location, t1.campName,  t1.seedcolor , t1.preprocess , t1.seedMoisture, t1.blendingLock  " +
            " from (  " +
            "   SELECT b.stkQty as qty, b.refid as productId, b.batchNo as batchNo, b.batchId as batchId, b.branch as branch,  " +
            "   max(br.name) as branchName , b.binId as binNo, b.binName as binName , Max(gd.id) as gid , max(gd.external) as godownType, " +
            "   max(gd.name) as godownName , b.category as category, b.lockType as lockType, b.inwarddate as inwardDate,  b.lot as lot,  " +
            "   b.oil_ffa as FFA, b.location as location, b.camp_name as campName,  b.seedcolor as seedcolor , max(g.silo) as silo , " +
            "   ( case WHEN (ba.bin_no IS NULL OR ba.bin_no = 0) then false else true end ) as preprocess ,  " +
            "   b.seedmoisture as seedMoisture, b.blending_lock as blendingLock  FROM binstock b  " +
            "   LEFT join blending_advice_preprocess_bin_details ba on ba.bin_no = b.binname   " +
            "   LEFT JOIN branch br ON br.id = b.branch LEFT JOIN gogroup g ON g.goname = b.binname  " +
            "   LEFT JOIN godown gd ON gd.id = g.gid  " +
            "   WHERE  (b.mrnstatus IS NULL or b.mrnstatus = 'N') AND b.refId =:productId  " +
            "     AND (:category is null or  b.category= :category)  " +
            "     AND (:branch is null or br.id = :branch)  " +
            "     AND (:ffa is null or b.oil_ffa = :ffa)  " +
            "     AND (:status is null or b.lockStatus = :status)  " +
            "     AND (:lockType is null or b.lockType = :lockType)  " +
            "     AND (b.stkQty > 0)   " +
            "   GROUP BY  b.stkQty, b.refid , b.branch ,  b.batchNo , b.batchId , b.binId , b.binName ,  " +
            "   b.category , b.lockType , b.inwarddate ,  b.lot , b.oil_ffa , b.location , b.camp_name ,  b.seedcolor ,  " +
            "   b.blending_lock, ba.bin_no, b.seedmoisture   order by b.inwarddate asc  " +
            "  ) as t1 " +
            " GROUP BY   t1.productId, t1.batchNo, t1.batchId, t1.branch,  " +
            " t1.branchName , t1.binNo, t1.binName ,  t1.silo , t1.gid , t1.godownType, " +
            " t1.godownName ,t1.category, t1.lockType, t1.inwardDate, t1.lot,  " +
            " t1.FFA, t1.location, t1.campName,  t1.seedcolor , t1.preprocess , t1.seedMoisture, t1.blendingLock " , nativeQuery = true)
    List<BlendingStock> findBinStockDetails( @Param("productId") Long productId,
                                             @Param("category") String category,
                                             @Param("ffa") Double ffa,
                                             @Param("branch") Long branch,
                                             @Param("lockType") String lockType,
                                             @Param("status") String status);




    @Query(value = "SELECT b.stkqty as qty, br.id as branch, br.name as branchName , b.refid as productId , "
            + " b.batchno as batchNo, b.batchid as batchId, b.binid as binNo, b.binname as binName , b.category as category, "
            + " b.lockType as locktype, b.inwarddate as inwardDate, b.oil_ffa as FFA, b.location as location, b.seedcolor as seedcolor , "
            + " b.seedmoisture as seedMoisture , b.camp_name as campName FROM binstock b  LEFT JOIN Branch br ON br.id = b.branch "
            + " WHERE (:productId is null or b.refid = :productId ) "
            + " AND (b.batchno IN (:batchNo) ) "
            + " AND (b.binname IN (:binNo) ) "
            + " AND (b.stkQty > 0)" , nativeQuery = true)
    List<BlendingStock> findBinStockDetails( @Param("productId") Long productId, @Param("binNo") List<Long> binNo, @Param("batchNo") List<String> batchNo);





    @Query(value = " select babd.batch_no as batchNo, babd.bin_no as binNo, babd.lot as lot , babd.quantity as qty , " +
            " babd.product_id as product_id " +
            " from blending_advice ba left join blending_advice_details bad on bad.blending_advice_id = ba.id " +
            " left join blending_advice_stock_details basd on basd.blending_advice_details_id = bad.id " +
            " left join blending_advice_bin_details babd on babd.blending_advice_stock_id  = basd.id " +
            " where ba.status = 'A' AND bad.status = 'A' AND  ba.journal_id =:journalId AND bad.branch_id =:branchId   " +
            " AND babd.product_id =:productId AND babd.bin_no =:binNo AND babd.lot =:lot " +
            " order by babd.id desc limit 1 " , nativeQuery = true)
    BlendingBatchBin findBlendingBatchBinDetails(@Param("journalId") Long journalId,
                                               @Param("productId") Long productId,
                                               @Param("branchId") Long branchId,
                                               @Param("binNo") Long binNo,
                                               @Param("lot") Double lot);










}
