package com.aria.dao;

import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BinStockRepository extends JpaRepository<BinStock, Long> {

    @Query(value = "SELECT * FROM binstock b WHERE ( b.lockStatus='N' OR b.lockType ='WR'  OR b.lockType is null OR b.lockStatus is null  ) AND (:branchId is null or b.branch = :branchId) "
            + " AND (:deptId is null or b.dept = :deptId) " + " AND (:lot is null or b.lot =:lot) "
            + " AND (:productId is null or b.refid = :productId) " + " AND (:batchId is null or b.batchid = :batchId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) " + " AND (b.stkqty > 0) ", nativeQuery = true)
    List<BinStock> filterBillBinStockList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                          @Param("productId") Long productId, @Param("batchId") Long batchId, @Param("batchNo") String batchNo,
                                          @Param("lot") Double lot);

    @Query(value = "SELECT bin.branch as branch, bin.dept as dept, sum(bin.stkQty) as quantity FROM BinStock bin WHERE (bin.lockStatus='N' OR bin.lockType ='WR' or bin.lockType is null or bin.lockStatus is null ) "
            + " AND (:firmId is null or bin.cid = :firmId) " + " AND (:branchId is null or bin.branch = :branchId) "
            + "AND (:deptId is null or bin.dept = :deptId) " + "AND (:lot is null or bin.lot =:lot) "
            + "AND (:productId is null or bin.refId = :productId) "
            + "AND (:batchId is null or bin.batchId = :batchId) " + "AND (:batchNo is null or bin.batchNo = :batchNo) "
            + "AND (bin.stkQty > 0) group by bin.branch,bin.dept ")
    List<BinStockQuantity> filterBillBinStockListGroupByBranchDept(@Param("firmId") Long firmId,
                                                                   @Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("productId") Long productId,
                                                                   @Param("batchId") Long batchId, @Param("batchNo") String batchNo, @Param("lot") Double lot);

    @Query(value = "SELECT distinct binname FROM binstock  WHERE (:branchId is null OR branch =:branchId ) AND dept = 4 AND (:binNo is null OR CAST(binname  AS TEXT)  LIKE ('%' || lower(:binNo) || '%')) ORDER BY binname asc ", nativeQuery = true)
    List<Long> filterBillBinList(@Param("branchId") Long branchId, @Param("binNo") String binNo);

    @Query(value = "SELECT gg.gCode as binNo, gg.goName as binId, sum(bs.stkQty) as currentStock   FROM GoGroup gg LEFT JOIN BinStock bs ON bs.binId=gg.gCode "
            + "where (gg.binType = 'RM' AND gg.branch =:branchId ) " + " GROUP BY gg.gCode, gg.goName ")
    List<FindBinDetails> dpBinANDStock(@Param("branchId") Long branchId);

    @Query(value = "SELECT  b.binType as binType, b.gCode as binCode, b.dept as deptId, b.branch as branchId, b.cluster as cluster, b.gid as godownId, g.name as godownName, dpt.name as deptName, br.branchName as branchName, dpt.organisation.name as orgName, bin.locked as locked, bin.locktype as locktype  FROM GoGroup b, DepartmentDetails dpt, Branch br, BinStatus bin, GoDown g WHERE dpt.id=b.dept AND br.id=b.branch AND bin.binName=b.goName AND b.goName = :binId AND b.gid = g.id")
    BinGoGroupList getBinStkDetailsForBinNo(@Param("binId") Long binId);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binid =:binId AND b.binname =:binname limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBin(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                                @Param("binId") String binId, @Param("binname") long binname);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binid =:binId AND b.binname =:binname AND b.lot =:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBinAndLot(@Param("productId") Long productId,
                                                      @Param("batchNo") String batchNo, @Param("binId") String binId, @Param("binname") long binname,
                                                      @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo  AND b.binname =:binname limit 1", nativeQuery = true)
    BinStock findByProductBatchAndBinId(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                        @Param("binname") Long binname);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo  AND b.binname =:binname AND b.lot =:lot  limit 1", nativeQuery = true)
    BinStock findByProductBatchAndBinId(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                        @Param("binname") long binname, @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.binname =:binName ", nativeQuery = true)
    List<BinStock> findBybinNumber(@Param("binName") Long binName);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binname =:binname AND b.lot =:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByProductAndBatchAndBinId(@Param("productId") Long productId,
                                                     @Param("batchNo") String batchNo, @Param("binname") Long binname, @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.batchid =:batchId AND b.batchno =:batchNo AND b.binname =:binname  ", nativeQuery = true)
    List<BinStock> findByBatchAndBinId(@Param("batchId") Long batchId, @Param("batchNo") String batchNo,
                                       @Param("binname") Long binname);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binname =:binname ", nativeQuery = true)
    List<BinStock> findByProductAndBatchAndBinIdANDLockTrue(@Param("productId") Long productId,
                                                            @Param("batchNo") String batchNo, @Param("binname") Long binname);

    @Query(value = "SELECT bin FROM BinStock bin WHERE (:branchId is null or bin.branch = :branchId) "
            + "AND (:deptId is null or bin.dept = :deptId) " + "AND (:productId is null or bin.refId = :productId) "
            + "AND (:batchId is null or bin.batchId = :batchId) " + "AND (:batchNo is null or bin.batchNo = :batchNo) "
            + "AND (bin.stkQty > 0) order by bin.id desc ")
    Page<BinStock> filterBillBinStockList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                          @Param("productId") Long productId, @Param("batchId") Long batchId, @Param("batchNo") String batchNo,
                                          Pageable pageable);

    @Query(value = "SELECT bin.branch as branch, bin.lot as lot, pr.productName as productName,  "
            + " br.branchName as branchName,  bin.dept as departmentId, dept.name as departmentName, "
            + " bin.refId as productId, sum(bin.stkQty) as stock FROM  ProductMaster pr, BinStock bin, "
            + " Branch br, DepartmentDetails dept WHERE (bin.refId = :productId) AND (bin.cid = :firmId) "
            + " AND (dept.id=bin.dept) " + "AND (pr.id=bin.refId)" + "AND (br.id=bin.branch)"
            + " AND (:lockType is null or bin.lockType = :lockType) " + "AND (:lot is null or bin.lot = :lot) "
            + " AND (:lockType is null AND bin.lockType is NULL) " + "AND ( bin.stkQty > 0) "
            + " AND (:lockStatus is null or bin.lockStatus = :lockStatus) GROUP BY pr.productName, bin.lot, bin.branch, bin.dept, bin.refId, br.branchName, dept.name ")
    List<StockByBranch> getStockByBranch(@Param("firmId") Long firmId, @Param("productId") Long productId,
                                         @Param("lockType") String lockType, @Param("lockStatus") String status, @Param("lot") Double lot);

    @Query(value = "SELECT bin.binName AS binNo, bin.batchno AS batchNo, SUM(bin.stkQty) AS stockQty "
            + "FROM BinStock bin " + "WHERE (:branchId IS NULL OR bin.branch = :branchId) "
            + "AND (:deptId IS NULL OR bin.dept = :deptId) "
            + "AND (:productId IS NULL OR bin.refId = :productId OR bin.stkqty = 0) "
            + "GROUP BY bin.binName, bin.batchno", nativeQuery = true)
    List<BinStockListDto> filterBinStockList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                             @Param("productId") Long productId);

    @Query(value = "SELECT distinct g.goname FROM  gogroup g  WHERE g.branch =:branchId AND g.dept =:deptId "
            + " AND (:type is null OR g.binType =:type)  ORDER BY g.goname asc ", nativeQuery = true)
    List<String> filterBillBinListDeptWise(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                           @Param("type") String type);

    @Query(value = "SELECT distinct g.goname FROM  gogroup g  WHERE g.branch =:branchId AND g.dept =:deptId "
            + " AND (:type is null OR g.binType =:type)  ORDER BY g.goname asc ", nativeQuery = true)
    List<String> listAllDeptWiseBinWithStockList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                                 @Param("type") String type);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.binname =:binName AND b.binid =:binId AND b.lot =:lot AND b.stkqty > 0 ORDER BY id ASC", nativeQuery = true)
    List<BinStock> findByBinStockByBinNoANdBinId(@Param("binName") Long binName, @Param("binId") String binId,
                                                 @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE  b.batchno =:batchNo ", nativeQuery = true)
    List<BinStock> findByProductBatchNo(@Param("batchNo") String batchNo);

    @Query(value = "SELECT bin FROM BinStock bin WHERE (:branchId is null or bin.branch = :branchId) "
            + "AND (:deptId is null or bin.dept = :deptId) " + "AND (:lot is null or bin.lot =:lot) "
            + "AND (:productId is null or bin.refId = :productId) " + "AND (bin.stkQty > 0) ")
    List<BinStock> filterBillBinStockList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                          @Param("productId") Long productId, @Param("lot") Double lot);

    @Query(value = "SELECT COALESCE(SUM(bin.stkqty),0.0) as qty from binstock bin WHERE "
            + " (:productId is null or bin.refid = :productId) " + "AND (bin.stkqty > 0) ", nativeQuery = true)
    Double getCurrentSock(@Param("productId") Long productId);

    @Query(value = "SELECT COALESCE(SUM(bin.stkqty),0.0) as qty from binstock bin WHERE "
            + " (:branchId is null or bin.branch = :branchId) " + "AND (:productId is null or bin.refid = :productId) "
            + "AND (bin.stkqty > 0) ", nativeQuery = true)
    Double getCurrentOpeningSock(@Param("branchId") Long branchId, @Param("productId") Long productId);

    @Query(value = "SELECT  *  FROM binstock b WHERE  b.batchno =:batchNo AND b.refid =:productId", nativeQuery = true)
    List<BinStock> findByProductBatchNo(@Param("batchNo") String batchNo, @Param("productId") Long productId);

    @Query(value = "SELECT bin.branch as id, max(b.name) as name from binstock bin LEFT JOIN branch b ON b.id = bin.branch WHERE b.name <> '' "
            + " AND (:productId is null or bin.refid = :productId) "
            + " AND (bin.stkqty > 0) GROUP BY bin.branch ", nativeQuery = true)
    List<Tuple> getBranch(@Param("productId") Long productId);

    @Query(value = "SELECT  bin.branch  as branch , b.name as branchName, bin.dept as dept, dd.name as deptName, COALESCE(SUM(bin.stkqty),0.0) as qty  from binstock bin , branch b , department_details dd  where b.id = bin.branch AND  dd.id = bin.dept "
            + " AND (:productId is null or bin.refid = :productId) "
            + " AND (bin.stkqty > 0) GROUP BY  bin.branch , bin.dept , b.name, dd.name", nativeQuery = true)
    List<PlantWiseStock> getBranchWithDeptCurrentStock(@Param("productId") Long productId);

    @Query(value = " SELECT binname as binNo, COALESCE(SUM(stkqty),0.0) as qty, lot , refid as productId  from binstock  "
            + " WHERE (:productId is null or refid = :productId) " + " AND (:binNo is null or binname = :binNo) "
            + " AND (:lot is null or lot = :lot) AND lot IS NOT NULL "
            + " AND (stkqty > 0 ) GROUP BY  binname , lot , refid ", nativeQuery = true)
    List<BinLotWiseStock> getBinLotWiseStock(@Param("productId") Long productId, @Param("binNo") Long binNo,
                                             @Param("lot") Double lot);

    @Query(value = " SELECT binname as binNo, COALESCE(SUM(stkqty),0.0) as qty, lot , refid as productId  from binstock  "
            + " WHERE (:productId is null or refid = :productId) " + " AND (:binNo is null or binname = :binNo) "
            + " AND (:dept is null or dept = :dept) " + " AND (:branch is null or branch = :branch) "
            + " AND (:lot is null or lot = :lot) AND lot IS NOT NULL  GROUP BY  binname , lot , refid ", nativeQuery = true)
    List<BinLotWiseStock> getBinWiseLot(@Param("productId") Long productId, @Param("binNo") Long binNo,
                                        @Param("lot") Double lot, @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT distinct binname FROM binstock  b  " + " WHERE (:branchId is null or b.branch = :branchId) "
            + " AND (:deptId is null or b.dept = :deptId) " + " AND (:lot is null or b.lot =:lot)  AND lot IS NOT NULL "
            + "  AND binname IS NOT NULL " + " AND (:productId is null or b.refid = :productId) " + " AND (b.stkqty > 0) "
            + " AND (:binNo is null OR CAST(binname  AS TEXT)  LIKE ('%' || lower(:binNo) || '%')) ORDER BY binname asc ", nativeQuery = true)
    List<Long> filterBinNoList(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                               @Param("productId") Long productId, @Param("binNo") String binNo, @Param("lot") Double lot);

    @Query(value = "select b.id AS id, b.refid AS refId, b.batchno AS batchNo, b.batchid AS batchId,    "
            + " b.binid AS binId, b.binname AS binName, b.lot AS lot, b.stkqty AS stkQty,      "
            + " b.avgcost AS avgCost, b.cost AS cost, b.dept AS dept, b.branch AS branch,     "
            + " b.lockstatus AS lockStatus,b.bintype AS binType,  b.locktype AS lockType,     "
            + " b.oil_ffa AS oilFFA,  b.inwarddate AS inwarDate, b.location AS location,     "
            + " t1.camp AS camp, t1.place AS place, t1.state AS state, t1.lorrySerialNo AS lorrySerialNo      "
            + " from (    "
            + "      SELECT  bs.id AS id, bs.refid AS refId, bs.batchno AS batchNo, bs.batchid AS batchId,    "
            + "      bi.lorry_serial_no AS lorrySerialNo , c.camp as camp, c.place as place, c.state as state , d.id as dip, bi.id as biid    "
            + "       FROM binstock bs     "
            + "       LEFT JOIN bill_inward_product_item_received bipir ON  bipir.batch_no = bs.batchno     "
            + "       LEFT JOIN bill_inward bi ON bi.id = bipir.inward_id     "
            + "       LEFT JOIN direct_purchase d on d.bill_ref_id  = bi.id     "
            + "       LEFT JOIN camp c on c.id = d.camp_id     "
            + "       WHERE   bi.product_category ='RawMaterial'    "
            + "       AND (:productId is null or bs.refid = :productId)     "
            + "       AND (:branchId is null or bs.branch = :branchId)     "
            + "       AND (:binNo is null or bs.binname = :binNo)     "
            + "       AND (:lockType is null or bs.locktype = :lockType)     "
            + "       AND (bs.lot is not null AND bs.lot > 0)     " + "       AND (lower(bs.lockstatus) = 'y')     "
            + "       GROUP BY bs.id , bs.refid , bs.batchno , bs.batchid , bi.lorry_serial_no , c.camp , c.place , c.state , d.id , bi.id    "
            + "       order by bs.id desc    "
            + " ) as t1 , binstock b WHERE t1.id = b.id and b.stkqty > 0 ", nativeQuery = true)
    Page<BinLockStock> filterBinLockByLockTypeAndStatus(@Param("branchId") Long branchId,
                                                        @Param("productId") Long productId, @Param("binNo") Long binNo, @Param("lockType") String lockType,
                                                        Pageable pageable);

    @Query(value = " select b.id AS id, b.refid AS refId, b.batchno AS batchNo, b.batchid AS batchId,    "
            + " b.binid AS binId, b.binname AS binName, b.lot AS lot, b.stkqty AS stkQty,      "
            + " b.avgcost AS avgCost, b.cost AS cost, b.dept AS dept, b.branch AS branch,     "
            + " b.lockstatus AS lockStatus,b.bintype AS binType,  b.locktype AS lockType,     "
            + " b.oil_ffa AS oilFFA,  b.inwarddate AS inwarDate, b.location AS location,     "
            + " t1.camp AS camp, t1.place AS place, t1.state AS state, t1.lorrySerialNo AS lorrySerialNo      "
            + " from (    "
            + "  SELECT  bs.id AS id, bs.refid AS refId, bs.batchno AS batchNo, bs.batchid AS batchId,    "
            + "  bi.lorry_serial_no AS lorrySerialNo , c.camp as camp, c.place as place, c.state as state , "
            + "  d.id as dip, bi.id as biid    " + "   FROM binstock bs     "
            + "   LEFT JOIN bill_inward_product_item_received bipir ON  bipir.batch_no = bs.batchno     "
            + "   LEFT JOIN bill_inward bi ON bi.id = bipir.inward_id     "
            + "   LEFT JOIN direct_purchase d on d.bill_ref_id  = bi.id     "
            + "   LEFT JOIN camp c on c.id = d.camp_id     " + "   WHERE   bi.product_category ='RawMaterial'    "
            + "   AND (:productId is null or bs.refid = :productId)     "
            + "   AND (:branchId is null or bs.branch = :branchId)     "
            + "   AND (:binNo is null or bs.binname = :binNo)     "
            + "   AND (:lockType is null or bs.locktype = :lockType)     "
            + "   AND (bs.lot is not null AND bs.lot > 0)     " + "   AND (lower(bs.lockstatus) = 'y')     "
            + "   GROUP BY bs.id , bs.refid , bs.batchno , bs.batchid , bi.lorry_serial_no , c.camp , c.place , c.state , d.id , bi.id    "
            + "   order by bs.id desc    "
            + " ) as t1 , binstock b WHERE t1.id = b.id and b.stkqty > 0 ", nativeQuery = true)
    List<BinLockStock> filterBinLockByLockTypeAndStatus(@Param("branchId") Long branchId,
                                                        @Param("productId") Long productId, @Param("binNo") Long binNo, @Param("lockType") String lockType);

    @Query(value = "select bin.* from binlock bin left join binstock b on cast (b.binname as text ) = bin.bin_no WHERE "
            + "( :branchId is null or (bs.branch = :branchId )) "
            + "AND (:lockType is null or bin.lock_type = :lockType) " + "AND (b.lot is not null) "
            + "AND (bin.lock_status = 'Lock') ", nativeQuery = true)
    List<BinLock> findAllBinNumberLocked(@Param("branchId") Long branchId, @Param("lockType") String lockType);

    @Query(value = "select  b.binid as binId, b.lot as lot , b.binname as binNo, COALESCE(SUM(b.stkqty),0.0) as qty, "
            + " b.refid as productId,  max(pm.name) as productName, max(pm.category) as productCategory,  "
            + " COALESCE(SUM(b.cost),0.0) as cost, (  (SUM(b.cost) ) / (SUM(b.stkqty)) ) as avgCost from binstock b "
            + " left join products_master pm on pm.id = b.refid WHERE stkqty  > 0 "
            + " AND (:branchId is null or b.branch = :branchId) " + " AND (:deptId is null or b.dept = :deptId) "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (:productName is null or lower(pm.name) like ('%' || lower(:productName) || '%')) "
            + " AND (:productCategory is null or pm.category = :productCategory )"
            + " AND (:binNo is null OR CAST(b.binname  AS TEXT)  LIKE ('%' || lower(:binNo) || '%')) "
            + " AND (:lot is null OR b.lot = :lot) "
            + " GROUP BY  b.binid , b.binname , b.refid, b.lot ORDER BY b.binname ASC", nativeQuery = true)
    Page<BinWiseStockList> findBy(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                  @Param("productId") Long productId, @Param("productName") String productName,
                                  @Param("productCategory") String productCategory, @Param("binNo") String binNo, @Param("lot") Double lot,
                                  Pageable pageable);

    @Query(value = "select   (   ( sum ((b.stkqty * b.oil_ffa) ))  / (sum(b.stkqty))  ) as avgffa from binstock b WHERE "
            + "  b.oil_ffa IS NOT NULL " + " AND (:binName is null or b.binname = :binName) "
            + " AND (:productId is null or b.refid = :productId) " + " AND (b.stkqty > 0) ", nativeQuery = true)
    Double getAvgFFA(@Param("binName") Long binName, @Param("productId") Long productId);

    @Query(value = " SELECT  " + "  ( sum(cas.ffaQty) / sum(cas.qty) ) as casFFA " + "       FROM "
            + "         (  SELECT  " + "               SUM(b.stkqty) as qty ,    "
            + "               SUM(b.stkqty * b.oil_ffa) as ffaQty " + "            FROM binstock b WHERE b.stkqty > 0  "
            + "                 AND b.oil_ffa IS NOT NULL  " + "                 AND b.binname IN (:binName) "
            + "                 AND (:productId is null or b.refid = :productId) "
            + "                 GROUP BY b.binname , b.binid , b.refid  " + "        ) cas ", nativeQuery = true)
    Double getAvgCasFFA(@Param("binName") List<Long> binName, @Param("productId") Long productId);

    @Query(value = " select b.inwarddate as inwardDate, b.refid as productId, b.batchno as batchNo , b.batchid as batchId from binstock b  "
            + " WHERE b.stkqty > 0  " + " AND (:productId is null or b.refid = :productId) "
            + " AND (:branchId is null or b.branch = :branchId) "
            + " AND (cast(:date as date) is null or (b.inwarddate = :date)) "
            + " GROUP BY b.batchno , b.batchid, b.inwarddate, b.refid ", nativeQuery = true)
    List<ProductionBatchList> findBy(@Param("productId") Long productId, @Param("branchId") Long branchId,
                                     @Param("date") Date date);

    @Query(value = " select b.binname as binNo,  (   ( sum ((b.stkqty * b.oil_ffa) ))  / (sum(b.stkqty))  ) as avgFFA from binstock b WHERE "
            + "  b.oil_ffa IS NOT NULL " + " AND (:productId is null or b.refid = :productId) "
            + " AND (b.stkqty > 0) group by b. binname, b.refid ", nativeQuery = true)
    List<CasBinWiseStockList> getBinWithAvgFFA(@Param("productId") Long productId);

    @Query(value = "SELECT b.binname as binName , max(b.binid) as binId , b.lot as lot , b.refid  as productId , "
            + " max(pm.name) as productName,  b.branch as branch , SUM(b.stkqty)  as qty from binstock b "
            + " left join products_master pm on pm.id = b.refid  " + " WHERE b.stkqty > 0  AND b.binname <> 0 "
            + " AND b.refid IN (:productIds) " + " AND b.lot IN (:lots) " + " AND (:dept is null or b.dept = :dept) "
            + " AND (:branch is null or b.branch = :branch) " + " GROUP BY  b.binname , b.lot ,  b.refid , b.branch "
            + " ORDER BY  b.binname ASC ", nativeQuery = true)
    List<StockPaper> getStockPaper(@Param("productIds") List<Long> productId, @Param("lots") List<Double> lots,
                                   @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT  b.refid  as productId , b.lot as lot , max(pm.name) as productName from binstock b "
            + " left join products_master pm on pm.id = b.refid  WHERE b.stkqty > 0 " + " AND  b.refid IN (:productId) "
            + " AND (:dept is null or b.dept = :dept) " + " AND (:branch is null or b.branch = :branch) "
            + " GROUP BY  b.lot , b.refid  " + " ORDER BY  b.refid ASC ", nativeQuery = true)
    List<StockPaperItems> getStockPaperItems(@Param("productId") List<Long> productId, @Param("branch") Long branch,
                                             @Param("dept") Long dept);

    @Query(value = "SELECT  b.refid  as productId , b.lot as lot , max(pm.name) as productName "
            + " from binstock b left join products_master pm on pm.id = b.refid  "
            + " WHERE  pm.pcategory IS NOT NULL and pm.pcategory !='' and pm.pcategory !='Consumable' and b.stkqty > 0 "
            + " AND (:dept is null or b.dept = :dept) " + " AND (:branch is null or b.branch = :branch) "
            + " GROUP BY  b.lot , b.refid   ORDER BY  b.refid ASC  Limit 200", nativeQuery = true)
    List<StockPaperItems> getStockPaperAllItems(@Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT  b.refid, b.binid, b.binname, b.branch,  b.dept, sum(b.stkqty) as qty , b.lot , max(pm.name) as productName "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE b.stkqty > 0 AND b.binname <> 0 "
            + " AND ( b.lot IS NOT NULL or b.lot <> 0 )   "
            + " AND ( b.refid in( select distinct br.id from brmproids br where br.bid =:branch ) )"
            + " AND (:branch is null or branch = :branch) "
            + " GROUP BY b.binid, b.binname, b.branch, b.dept, b.lot, b.refid "
            + " ORDER BY  b.binname ASC ", nativeQuery = true)
    List<PhysicalStockItems> getPhysicalStockItems(@Param("branch") Long branch);

    @Query(value = "SELECT * FROM binstock b " + " WHERE b.branch =:branch AND b.refid =:itemId "
            + " AND b.binname =:binName AND b.batchno =:issuedFromBatch " + " AND b.lot =:lot "
            + " AND ( b.locktype <> 'BU' or b.locktype <> 'SW' or b.locktype <> 'SB' or b.locktype <> 'FU' or b.locktype <> 'QI' or b.locktype <> 'SR' or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N' ) "
            + " AND b.stkqty > 0  limit 1 ", nativeQuery = true)
    BinStock findBy(@Param("branch") Long branch, @Param("itemId") Long itemId,
                    @Param("issuedFromBatch") String issuedFromBatch, @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock b " + " WHERE b.branch =:branch AND b.refid =:itemId "
            + " AND b.binname =:binName AND b.batchno =:issuedFromBatch "
            + " AND b.stkqty > 0  limit 1 ", nativeQuery = true)
    BinStock findBy(@Param("branch") Long branch, @Param("itemId") Long itemId,
                    @Param("issuedFromBatch") String issuedFromBatch, @Param("binName") Long binName);

    @Query(value = "SELECT * FROM binstock bs WHERE bs.batchno =:batchNo AND bs.binname = :binId AND bs.lockstatus = :lockStatus AND bs.locktype = :lockType AND bs.dept =:deptId AND bs.branch =:branchId AND bs.stkqty =:issuedQty AND bs.lot =:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByBatchNoAndBinIdAndLockStatusAndLockTypeAndDeptAndBranchAndIssuedQty(
            @Param("batchNo") String issuedFromBatch, @Param("binId") Long issuedFromBin,
            @Param("lockStatus") String lockStatus, @Param("lockType") String lockType, @Param("deptId") Long deptId,
            @Param("branchId") Long branchId, @Param("issuedQty") Double allocatedQty, @Param("lot") Double lot);

    @Query(value = "select  distinct category  from binstock b where  b.category  IS NOT NULL AND (b.stkqty > 0) AND (:category is null or lower(b.category) like ('%' || lower(:category) || '%')) ", nativeQuery = true)
    List<String> findByCategory(@Param("category") String category);

    @Query(value = "SELECT * FROM binstock b WHERE b.binname = :binNo AND b.batchno = :batchNo limit 1", nativeQuery = true)
    BinStock findByBatchNoAndBinName(@Param("binNo") Long binNo, @Param("batchNo") String batchNo);

    @Query(value = " select b.binname as binNo, b.lockstatus as lock from binstock b WHERE (b.mrnstatus IS NULL or b.mrnstatus = 'N') AND b.lockstatus ='Y'  "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (b.stkqty > 0) group by  b.binname, b.lockstatus ", nativeQuery = true)
    List<BinLockList> getBinLockList(@Param("productId") Long productId);

    @Query(value = "select b.binname as binNo, b.blending_lock as blendingLock from binstock b WHERE b.blending_lock = true "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (b.stkqty > 0) group by  b.binname, b.blending_lock ", nativeQuery = true)
    List<BinLockList> getBlendingLockList(@Param("productId") Long productId);

    @Query(value = "SELECT  b.refid as itemId, COALESCE( SUM( (stkqty * lot) ), 0.0 ) as qty  FROM binstock b "
            + " WHERE b.stkqty > 0 AND b.binname <> 0 AND (:branch is null or b.branch = :branch) "
            + " AND b.refid  in (SELECT distinct id from brmproids br where br.bid =:branch AND br.day_end_closing_flag = true) "
            + " GROUP BY b.refid order by b.refid ", nativeQuery = true)
    List<ProductStockList> getActualStock(@Param("branch") Long branch);

    @Query(value = "SELECT  COALESCE( SUM( (b.stkqty * b.lot) ), 0.0 ) as qty   FROM binstock b WHERE b.stkqty > 0 "
            + " AND ( b.lot =:lot )   " + " AND ( b.refid =:refId) " + " AND (b.branch = :branch) "
            + " AND (b.binname = :binName) " + " AND (b.binid = :binid) " + " AND (b.gid= :gid) "
            + " AND (b.dept =:dept) ", nativeQuery = true)
    Double getActualStockQty(@Param("branch") Long branch, @Param("binName") Long binName, @Param("binid") String binid,
                             @Param("refId") Long refId, @Param("gid") Long gid, @Param("lot") Double lot, @Param("dept") Long dept);

    @Query(value = "select b.lot as lot , COALESCE(SUM(b.stkqty),0.0) as qty, b.refid as productId  from binstock b "
            + " WHERE stkqty  > 0 AND (:productId is null or b.refid = :productId) "
            + " GROUP BY  b.refid, b.lot ", nativeQuery = true)
    List<ProductLotWiseStockList> findBy(@Param("productId") Long productId);

    @Query(value = " SELECT max(pm.name) as itemName, pm.id as itemId, b.branch as branchId, COALESCE( SUM( (b.stkqty * lot) ), 0.0 ) as qty "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE "
            + " b.refid NOT IN (SELECT br.id as itemId FROM brmproids br WHERE br.day_end_closing_flag = true AND (br.bid =:branch ) ) "
            + " AND b.stkqty > 0 AND b.binname <> 0 AND (b.branch =:branch)  "
            + " GROUP BY b.refid , b.branch, pm.id order by pm.id ", nativeQuery = true)
    List<DayEndStockList> findByClosingStockDetails(@Param("branch") Long branch);

    @Query(value = "SELECT  b.refid, b.branch, sum(b.stkqty) as qty , b.lot , max(pm.name) as productName, b.binname as binname , b.gid as gid "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE b.stkqty > 0 AND b.binname <> 0 AND b.gid <> 0 "
            + " AND ( b.lot IS NOT NULL or b.lot <> 0 )   " + " AND (:branch is null or branch = :branch) "
            + " GROUP BY  b.branch,  b.lot, b.refid, b.gid, b.binname "
            + " ORDER BY  b.refid  ASC ", nativeQuery = true)
    List<DayEndClosingStockList> getDayEndClosingStockItems(@Param("branch") Long branch);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.binname =:binName AND b.binid =:binId AND b.lot =:lot AND b.stkqty > 0 AND b.batchno =:batchNo ORDER BY id ASC", nativeQuery = true)
    List<BinStock> findByBinStockByBinNoANdBinId(@Param("binName") Long binName, @Param("binId") String binId,
                                                 @Param("lot") Double lot, @Param("batchNo") String batchNo);

    @Query(value = "SELECT DISTINCT b.binname as binNo, b.batchno as batchNo, b.stkqty as stockQty FROM binstock b WHERE b.refid = :productId", nativeQuery = true)
    List<BinStockListDto> filterBinStockByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT * FROM binstock b WHERE b.ltrnno = :trnNo AND b.refid = :itemId", nativeQuery = true)
    List<BinStock> findByLTrnNo(String trnNo, Long itemId);

    @Query(value = "select pm.id as itemId, pm.name as itemName , b.binname as binName, b.binid as binId , "
            + " b.lot as lot, b.stkqty as stkQty , b.branch as branchId , br.name as branchName , b.batchno as productionBatchNo , "
            + " b.batchid as productionBatchId from binstock b left join products_master pm on pm.id = b.refid  "
            + " left join branch br on br.id = b.branch  " + " where  b.binname <> 0 AND b.stkqty > 0  "
            + " AND (:productName is null or lower(pm.name) like ('%' || lower(:productName) || '%')) "
            + " AND (:productCategory is null or lower(pm.productcategory) like ('%' || lower(:productCategory) || '%')) "
            + " AND (:branchId is null or b.branch = :branchId) " + " AND (:productId is null or b.refid = :productId) "
            + " AND (:binNo is null OR CAST(b.binname  AS TEXT)  LIKE ('%' || lower(:binNo) || '%')) "
            + " AND (:lot is null OR b.lot = :lot) ", nativeQuery = true)
    Page<HOBinStockList> findBy(@Param("branchId") Long branchId, @Param("productId") Long productId,
                                @Param("productName") String productName, @Param("productCategory") String productCategory,
                                @Param("binNo") String binNo, @Param("lot") Double lot, Pageable pageable);

    @Query(value = "SELECT * FROM binstock b WHERE ( b.locktype not in ('DT','BU', 'SB', 'QI') )  AND (:branchId is null or b.branch = :branchId) "
            + " AND (:deptId is null or b.dept = :deptId) " + " AND (:lot is null or b.lot =:lot) "
            + " AND (:productId is null or b.refid = :productId) " + " AND (:batchId is null or b.batchid = :batchId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) " + " AND (:binNo is null or b.binname = :binNo) "
            + " AND (:gid is null or b.gid = :gid) " + " AND (b.stkqty > 0) ", nativeQuery = true)
    List<BinStock> filterBinStockData(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                      @Param("productId") Long productId, @Param("batchId") Long batchId, @Param("batchNo") String batchNo,
                                      @Param("lot") Double lot, @Param("gid") Long gid, @Param("binNo") Long binNo);

    @Query(value = "SELECT * FROM binstock b WHERE  b.refid =:itemId AND b.binname =:binName AND b.batchno =:issuedFromBatch AND b.lot =:lot AND (b.lockstatus = 'N' OR b.lockstatus is null or b.lockstatus = '') AND (b.locktype <> 'BU' or b.locktype <> 'SW' or b.locktype <> 'SB' or b.locktype <> 'FU' or b.locktype <> 'QI' or b.locktype <> 'SR' or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N' ) limit 1", nativeQuery = true)
    BinStock findByBatchNoAndBinNameByLockStatus(@Param("itemId") Long itemId,
                                                 @Param("issuedFromBatch") String issuedFromBatch, @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = "SELECT  p.refid as productId, p.name as productName, sum(stkqty) as stkQty, p.lot as lot FROM no2products p JOIN binstock b ON p.refid=b.refid AND p.lot=b.lot WHERE p.producttype =:ProductType AND b.branch =:branch AND b.dept =:dept AND p.active=1 AND b.stkqty>0 AND (b.lockStatus = 'N' OR b.lockstatus IS NULL OR b.lockstatus = '') AND (b.locktype <> 'BU' or b.locktype <> 'SW' or b.locktype <> 'SB' or b.locktype <> 'FU' or b.locktype <> 'QI' or b.locktype <> 'SR' or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N') GROUP BY p.refid, p.lot, p.name", nativeQuery = true)
    List<StockbyProduct> findStockbyProductAndLockStatus(@Param("ProductType") String ProductType,
                                                         @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT DISTINCT * FROM binstock b WHERE b.refid = :productId AND b.branch = :branch AND b.dept = :dept AND (b.lockstatus = 'N' OR b.lockstatus IS NULL OR b.lockstatus = '') AND b.batchno = :batchNo", nativeQuery = true)
    BinStock findStockbyProductIdBatchNoAndLockStatus(@Param("productId") Long productId, @Param("branch") Long branch,
                                                      @Param("dept") Long dept, @Param("batchNo") String batchNo);

    @Query(value = "SELECT * FROM binstock b WHERE (b.lockstatus IS NULL OR b.lockstatus = '' OR b.lockstatus = 'N') AND b.refid = :refId AND b.branch = :branch AND b.dept = :dept ORDER BY b.adate ASC, b.batchid ASC", nativeQuery = true)
    List<BinStock> FindByProIdLockStatus(@Param("refId") Long refId, @Param("branch") Long branch,
                                         @Param("dept") Long dept);

    @Query(value = "SELECT * FROM binstock b "
            + " WHERE ( b.lockStatus='N' OR b.lockType ='WR'  OR b.lockType is null OR b.lockStatus is null  )  AND b.inwarddate IS NOT NULL "
            + " AND (:branchId is null or b.branch = :branchId) " + " AND (:deptId is null or b.dept = :deptId) "
            + " AND (:lot is null or b.lot =:lot) " + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchId is null or b.batchid = :batchId) " + " AND (:batchNo is null or b.batchno = :batchNo) "
            + " AND (b.stkqty > 0) ", nativeQuery = true)
    List<BinStock> filterBinStockOnlyBill(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                          @Param("productId") Long productId, @Param("batchId") Long batchId, @Param("batchNo") String batchNo,
                                          @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock b WHERE   "
            + " b.refid =:itemId AND b.binname =:binName AND b.batchno =:issuedFromBatch "
            + " AND b.lot =:lot AND (b.locktype <> 'BU' or b.locktype <> 'SW' or b.locktype <> 'SB' or b.locktype <> 'FU' or b.locktype <> 'QI' or b.locktype <> 'SR' or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N' ) AND b.stkqty =:qty  limit 1", nativeQuery = true)
    BinStock findByBatchNoAndBinName(@Param("itemId") Long itemId, @Param("issuedFromBatch") String issuedFromBatch,
                                     @Param("binName") Long binName, @Param("lot") Double lot, @Param("qty") Double qty);

    @Query(value = " SELECT b.binname as binNo, COALESCE(SUM(b.stkqty),0.0) as qty, b.lot , b.refid as productId , b.batchno as batchNo , max(pm.name) as productName from binstock b LEFT JOIN products_master pm on pm.id = b.refid "
            + " WHERE (:productId is null or b.refid = :productId) " + " AND (:binNo is null or b.binname = :binNo) "
            + " AND (:lot is null or b.lot = :lot) AND lot IS NOT NULL "
            + " AND (stkqty > 0 ) GROUP BY  b.binname , b.lot , b.refid , b.batchno ", nativeQuery = true)
    List<SplitBinLotWiseStock> getBinLotWiseStockSplit(@Param("productId") Long productId, @Param("binNo") Long binNo,
                                                       @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock b " + " WHERE b.inwarddate IS NOT NULL AND b.oil_FFA IS NOT NULL "
            + " AND (:productId is null or b.refid = :productId) " + " AND (:batchId is null or b.batchid = :batchId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) LIMIT 1", nativeQuery = true)
    Optional<BinStock> findBy(@Param("productId") Long productId, @Param("batchId") Long batchId,
                              @Param("batchNo") String batchNo);

    @Query(value = "SELECT * FROM binstock b " + " WHERE (:binName is null or b.binname = :binName) "
            + " AND (:batchNo is null or b.batchno = :batchNo)  LIMIT 1 ", nativeQuery = true)
    Optional<BinStock> findBy(@Param("binName") Long binName, @Param("batchNo") String batchNo);

    @Query(value = "SELECT b.branch AS branch, b.dept AS dept, SUM(b.stkqty) AS quantity FROM binstock b "
            + "WHERE b.refid = :productId AND b.dept = :dept AND b.branch = :branch AND (b.lockstatus = 'N' OR b.lockstatus IS NULL OR b.lockstatus = '') AND b.lot = :lot "
            + "GROUP BY b.branch, b.dept", nativeQuery = true)
    List<BinStockQuantity> getStockInPouch(@Param("branch") Long branch, @Param("dept") Long dept,
                                           @Param("productId") Long productId, @Param("lot") Long lot);

    @Query(value = "SELECT * FROM binstock b "
            + " WHERE ( b.lockStatus='N' OR b.lockType ='WR'  OR b.lockType is null OR b.lockStatus is null  ) "
            + " AND (:branchId is null or b.branch = :branchId) AND (:firmId is null or b.cid = :firmId) "
            + " AND (:binStockId is null or b.id = :binStockId) " + " AND (:deptId is null or b.dept = :deptId) "
            + " AND (:lot is null or b.lot =:lot) " + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchId is null or b.batchid = :batchId) " + " AND (:binName is null or b.binname = :binName) "
            + " AND (:batchNo is null or b.batchno = :batchNo) "
            + " AND (b.stkqty > 0)  ORDER BY b.id ASC ", nativeQuery = true)
    List<BinStock> filterReqBinStockList(@Param("binStockId") Long binStockId, @Param("firmId") Long firmId,
                                         @Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("productId") Long productId,
                                         @Param("batchId") Long batchId, @Param("batchNo") String batchNo, @Param("lot") Double lot,
                                         @Param("binName") Long binName);

    @Query(value = "SELECT * FROM binstock b WHERE b.id in (:binStockIds) AND b.lockStatus='Y' AND b.locktype ='DT' AND (:branchId is null or b.branch = :branchId) "
            + " AND (:deptId is null or b.dept = :deptId) " + " AND (:lot is null or b.lot =:lot) "
            + " AND (:productId is null or b.refid = :productId) " + " AND (:batchId is null or b.batchid = :batchId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) " + " AND (b.stkqty > 0) ", nativeQuery = true)
    List<BinStock> filterBinStockOnlyDelite(@Param("binStockIds") List<Long> binStockIds,
                                            @Param("branchId") Long branchId, @Param("deptId") Long deptId, @Param("productId") Long productId,
                                            @Param("batchId") Long batchId, @Param("batchNo") String batchNo, @Param("lot") Double lot);

    @Query(value = "select b.id as id, b.inwarddate as inwardDate , b.lot as lot  , b.location as state , b.batchno as batchNo, b.batchid as batchId, "
            + "  b.camp_name as campName, b.binid as binId, b.binname as binName , bi.lorry_serial_no as lorryserialNo, max(b.stkqty) as Qty from binstock b "
            + " left join bill_inward_product_item_received bipir on bipir.batch_no = b.batchno AND bipir.batch_id = b.batchid "
            + " left join bill_inward bi on bi.id = bipir.inward_id   " + " where b.stkqty > 0 AND b.binname =:binName "
            + " group by b.id , b.location , b.inwarddate , b.lot , b.batchno , b.batchid , b.camp_name , b.binid, b.binname , bi.lorry_serial_no", nativeQuery = true)
    List<BinLockBiNoDetails> filterFindBy(@Param("binName") Long binName);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binid =:binId AND b.binname =:binname AND b.lot=:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBin(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                                @Param("binId") String binId, @Param("binname") long binname, @Param("lot") double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.refid =:productId AND b.batchno =:batchNo AND b.binid =:binId AND b.binname =:binname AND b.lot=:lot AND b.seedcolor=:color limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBin(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                                @Param("binId") String binId, @Param("binname") long binname, @Param("lot") double lot,
                                                @Param("color") String color);

    @Query(value = "SELECT  *  FROM binstock b WHERE  b.refid =:productId AND b.batchid =:batchId  AND b.batchno =:batchNo AND  b.ltrnno =:trnNo ", nativeQuery = true)
    List<BinStock> revertProductionReceiveBatch(@Param("productId") Long productId, @Param("batchId") Long batchId,
                                                @Param("batchNo") String batchNo, @Param("trnNo") String trnNo);

    @Query(value = "SELECT  *  FROM binstock b WHERE  b.refid =:productId "
            + " AND b.batchid =:batchId  AND b.batchno =:batchNo AND  b.lot =:lot "
            + " AND  b.binid =:binId AND  b.binname =:binName order by id ASC ", nativeQuery = true)
    List<BinStock> revertProductionIssueBatch(@Param("productId") Long productId, @Param("batchId") Long batchId,
                                              @Param("batchNo") String batchNo, @Param("lot") Double lot, @Param("binId") String binId,
                                              @Param("binName") Long binName);

    @Query(value = "SELECT  *  FROM binstock b " + " WHERE b.binname =:binName AND b.binid =:binId "
            + " AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 ORDER BY b.inwarddate ASC ", nativeQuery = true)
    List<BinStock> findByBinStocks(@Param("productId") Long productId, @Param("binName") Long binName,
                                   @Param("binId") String binId, @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b " + " WHERE b.binname =:binName AND b.binid =:binId "
            + " AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 "
            + " AND b.lockstatus = 'Y' AND b.locktype ='DT' ORDER BY b.inwarddate ASC ", nativeQuery = true)
    List<BinStock> findByBinStocksWithDeliteOnly(@Param("productId") Long productId, @Param("binName") Long binName,
                                                 @Param("binId") String binId, @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.binname =:binName AND b.binid =:binId AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 AND b.batchno =:batchNo ORDER BY b.inwarddate ASC", nativeQuery = true)
    List<BinStock> findByBinStockByBinNoANdBinId(@Param("productId") Long productId, @Param("binName") Long binName,
                                                 @Param("binId") String binId, @Param("lot") Double lot, @Param("batchNo") String batchNo);

    @Query(value = "SELECT  *  FROM binstock b " + " WHERE b.binname =:binName AND b.binid =:binId "
            + " AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 " + " AND b.batchno =:batchNo "
            + " AND b.lockstatus = 'Y' AND b.locktype ='DT'  ORDER BY b.inwarddate ASC", nativeQuery = true)
    List<BinStock> findByBinStockByBinNoANdBinIdDeliteOnly(@Param("productId") Long productId,
                                                           @Param("binName") Long binName, @Param("binId") String binId, @Param("lot") Double lot,
                                                           @Param("batchNo") String batchNo);

    @Query(value = "SELECT * FROM binstock b WHERE b.ltrnno = :trnNo AND b.batchno = :batchNo  AND b.refid =:productId AND b.lot =:lot AND b.stkqty =:qty AND b.locktype =:lockType limit 1", nativeQuery = true)
    BinStock findByBatchNoAndTRNNo(@Param("trnNo") String trnNo, @Param("batchNo") String batchNo,
                                   @Param("productId") Long productId, @Param("lot") Double lot, @Param("qty") Double qty,
                                   @Param("lockType") String lockType);

    @Query(value = "SELECT  *  FROM binstock b WHERE "
            + " b.refid =:productId AND b.batchno =:batchNo AND b.binname =:binname AND b.gid=:gid AND b.lot =:lot AND "
            + " b.branch = :branch limit 1", nativeQuery = true)
    List<BinStock> findByGodownStock(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                     @Param("binname") Long binname, @Param("gid") Long gid, @Param("lot") Double lot,
                                     @Param("branch") Long branch);

    @Query(value = " SELECT b.branch as branchId, max(br.name) as branchName , b.binid as binId, b.binname as binNo,   "
            + " b.inwarddate as lorryDate,  b.camp_name as camp, b.location as place,       "
            + " b.batchid as batchId, b.batchno as batchNo , b.lot as lot , COALESCE(SUM(DISTINCT b.stkqty),0.0) as qty,   "
            + " b.lockstatus as lockStatus , b.locktype as lockType , COALESCE(SUM(DISTINCT b.cost),0.0) as cost, max( b.avgcost) as avgCost ,  "
            + " b.refid as productId, max(pm.name) as productName, max(pm.category) as productCategory , b.mrnstatus as  mrnStatus "
            + " FROM binstock b LEFT JOIN branch br on b.branch = br.id   "
            + " left join products_master pm on pm.id = b.refid WHERE stkqty  > 0 "
            + " AND (:branchId is null or b.branch = :branchId)   " + " AND (:deptId is null or b.dept = :deptId)  "
            + " AND (:productId is null or b.refid = :productId)  "
            + " AND (:productName is null or lower(pm.name) like ('%' || lower(:productName) || '%'))  "
            + " AND (:productCategory is null or pm.category = :productCategory ) "
            + " AND (:binNo is null OR b.binname=:binNo)  " + " AND (:lot is null OR b.lot = :lot) "
            + " GROUP BY b.branch , b.binid , b.binname , b.inwarddate, b.camp_name , b.location ,"
            + " b.batchid , b.batchno , b.lot , b.lockstatus , b.locktype , b.refid ,b.mrnstatus", nativeQuery = true)
    List<BinQtyWiseStockList> findByBinQtyWiseStock(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                                    @Param("productId") Long productId, @Param("productName") String productName,
                                                    @Param("productCategory") String productCategory, @Param("binNo") Long binNo, @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock bs WHERE "
            + " bs.ltrnno = :trnNo AND bs.batchno =:batchNo AND bs.binname = :binId "
            + " AND bs.lockstatus = :lockStatus AND bs.locktype = :lockType "
            + " AND bs.dept =:deptId AND bs.branch =:branchId "
            + " AND bs.stkqty =:issuedQty AND bs.lot =:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByBatchNoAndBinIdAndLockStatusAndLockTypeAndDeptAndBranchAndIssuedQtyANDTrnNo(
            @Param("trnNo") String trnNo, @Param("batchNo") String issuedFromBatch, @Param("binId") Long issuedFromBin,
            @Param("lockStatus") String lockStatus, @Param("lockType") String lockType, @Param("deptId") Long deptId,
            @Param("branchId") Long branchId, @Param("issuedQty") Double allocatedQty, @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock b WHERE b.ltrnno = :trnNo AND ( b.lockstatus ='N' OR b.lockstatus IS NULL)   AND b.stkqty > 0 ", nativeQuery = true)
    List<BinStock> findByLTrnNo(String trnNo);

    @Query(value = "SELECT * FROM binstock b WHERE b.id =:id AND b.refid =:itemId AND b.binname =:binName AND b.batchno =:issuedFromBatch AND b.lot =:lot AND (b.locktype <> 'BU' or b.locktype <> 'SW' or b.locktype <> 'SB' or b.locktype <> 'FU' or b.locktype <> 'QI' or b.locktype <> 'SR' or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N' ) limit 1", nativeQuery = true)
    BinStock findByBatchNoAndBinName(@Param("id") Long id, @Param("itemId") Long itemId,
                                     @Param("issuedFromBatch") String issuedFromBatch, @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = " SELECT max(pm.name) as itemName, pm.id as itemId, b.branch as branchId, COALESCE( SUM( (b.stkqty * lot) ), 0.0 ) as qty "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE "
            + " b.refid IN (SELECT br.id as itemId FROM brmproids br WHERE br.day_end_closing_flag = true AND (br.bid =:branch ) ) "
            + " AND b.stkqty > 0 AND b.binname <> 0 AND (b.branch =:branch)  "
            + " GROUP BY b.refid , b.branch, pm.id order by pm.id ", nativeQuery = true)
    List<DayEndStockList> findByMainDayEndClosingStockDetails(@Param("branch") Long branch);

    @Query(value = " SELECT max(pm.name) as itemName, pm.id as itemId, b.branch as branchId, COALESCE( SUM( (b.stkqty * lot) ), 0.0 ) as qty "
            + " FROM binstock b left join products_master pm on pm.id = b.refid  "
            + " WHERE (:itemId is null or b.refid =:itemId )"
            + " AND b.stkqty > 0 AND b.binname <> 0 AND (b.branch =:branch)  "
            + " GROUP BY b.refid , b.branch, pm.id order by pm.id ", nativeQuery = true)
    List<DayEndStockList> findByProductWiseClosingStockDetails(@Param("itemId") Long itemId,
                                                               @Param("branch") Long branch);

    @Query(value = "SELECT  *  FROM binstock b WHERE b.binname =:binName AND b.binid =:binId AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 AND b.batchno =:batchNo ORDER BY b.id DESC ", nativeQuery = true)
    List<BinStock> findByBinStockByBinNoANdBinIdDESC(@Param("productId") Long productId, @Param("binName") Long binName,
                                                     @Param("binId") String binId, @Param("lot") Double lot, @Param("batchNo") String batchNo);

    @Query(value = " SELECT distinct b.*    "
            + " from blending_advice_bin_details ba , binstock b, blending_advice_stock_details basd     "
            + " where b.binname = ba.bin_no AND b.batchno = ba.batch_no AND b.refid = ba.product_id  AND ba.mainlot = b.lot"
            + " AND  ba.branch_id  = b.branch AND basd.id = ba.blending_advice_stock_id "
            + " AND ( :blendingAdviceStockId is null or basd.id =:blendingAdviceStockId  ) "
            + " AND ( :branch is null or b.branch =:branch ) " + " AND b.lot <> 0 AND stkqty > 0  "
            + " AND ( b.lockstatus <> 'Y' OR b.lockstatus IS NULL ) "
            + " AND ( :productId is null or b.refid = :productId ) "
            + " AND ( :binName is null or b.binname = :binName ) " + " AND ( :lot is null or b.lot = :lot )  "
            + " AND ( :casgroup is null or basd.cas_group =:casgroup )"
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingStockId(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                         @Param("branch") Long branch, @Param("productId") Long productId, @Param("binName") Long binName,
                                         @Param("lot") Double lot, @Param("casgroup") String casgroup);

    /*
     *
     *
     * @Query(value = " SELECT distinct b.*     " +
     * " from blending_advice_bin_details ba , binstock b    " +
     * " where b.batchno = ba.batch_no AND b.refid = ba.product_id   " +
     * " AND  ba.branch_id  = b.branch" +
     * " AND ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  ) "
     * + " AND ( :branch is null or b.branch =:branch )" +
     * " AND b.lot <> 0 AND stkqty > 0  AND (b.lockstatus <> 'Y' OR b.lockstatus IS NULL ) "
     * + " AND ( :productId is null or b.refid = :productId ) " +
     * " AND ( :binName is null or b.binname = :binName ) " +
     * " AND ( :lot is null or b.lot = :lot )  " + " ORDER BY b.inwarddate DESC ",
     * nativeQuery = true) List<BinStock>
     * findByBlendingStockId1(@Param("blendingAdviceStockId") Long
     * blendingAdviceStockId,
     *
     * @Param("branch") Long branch, @Param("productId") Long productId,
     *
     * @Param("binName") Long binName, @Param("lot") Double lot);
     */

    @Query(value = " SELECT distinct b.*    " + "  from blending_preprocess_request_receive_bin_map  ba "
            + "  left join blending_advice_stock_details basd  on basd.id = ba.blending_advice_stock_id AND basd.blending_advice_details_id = ba.blending_detail_id"
            + "  left join blending_advice_details bad on bad.id = basd.blending_advice_details_id"
            + "  left join binstock b on b.binname = ba.bin_no AND b.batchno = ba.batch_no AND b.refid = ba.item_id  AND ba.lot  = b.lot"
            + "  where  bad.branch_id  = b.branch "
            + "  AND ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  )"
            + "  AND ( :branch is null or b.branch =:branch )"
            + "  AND b.lot <> 0 AND stkqty > 0  AND (b.lockstatus <> 'Y' OR b.lockstatus IS NULL )"
            + "  AND ( :productId is null or b.refid = :productId )"
            + "  AND ( :binName is null or b.binname = :binName )" + "  AND ( :lot is null or b.lot = :lot ) "
            + "  AND ( :casgroup is null or basd.cas_group =:casgroup ) "
            + "  ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingStockId1(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                          @Param("branch") Long branch, @Param("productId") Long productId, @Param("binName") Long binName,
                                          @Param("lot") Double lot, @Param("casgroup") String casgroup);

    @Query(value = " SELECT distinct b.*    " + " from blending_receive_bin_map  ba   "
            + " left join blending_advice_details bad on bad.id = ba.blending_detail_id "
            + " left join blending_advice_stock_details basd on basd.blending_advice_details_id = ba.blending_detail_id   "
            + " left join blending_advice_bin_details babd on babd.blending_advice_stock_id = basd.id AND babd.batch_no = ba.batch_no  "
            + " left join binstock b on b.binname = ba.bin_no AND b.batchno = ba.batch_no AND b.refid = ba.item_id "
            + " WHERE  bad.branch_id  = b.branch "
            + " AND ( :blendingAdviceStockId is null or basd.id =:blendingAdviceStockId  )"
            + " AND ( :branch is null or b.branch =:branch )"
            + " AND b.lot <> 0 AND stkqty > 0  AND (b.lockstatus <> 'Y' OR b.lockstatus IS NULL )"
            + " AND ( :productId is null or b.refid = :productId )"
            + " AND ( :binName is null or b.binname = :binName )" + " AND ( :lot is null or b.lot = :lot ) "
            + " AND ( :casgroup is null or basd.cas_group =:casgroup ) "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingStockId2(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                          @Param("branch") Long branch, @Param("productId") Long productId, @Param("binName") Long binName,
                                          @Param("lot") Double lot, @Param("casgroup") String casgroup);

    @Query(value = "SELECT distinct b.*        " + " FROM blending_advice_bin_details bb , binstock b        "
            + " where b.binname = bb.bin_no AND b.batchno = bb.batch_no AND b.refid = bb.product_id AND bb.mainlot = b.lot    "
            + " AND  bb.branch_id  = b.branch    " + " AND  bb.blending_advice_stock_id IN    " + "   (   "
            + "       SELECT  distinct bs.id  FROM blending_advice ba    "
            + "       LEFT JOIN blending_advice_details bad on bad.blending_advice_id = ba.id    "
            + "       LEFT JOIN blending_advice_stock_details bs on bs.blending_advice_details_id = bad.id    "
            + "       WHERE  ba.status = 'A' AND bad.status = 'A' AND  ba.journal_id =:journalId    "
            + "       AND bad.branch_id =:branch    "
            + "       AND bs.product_id =:productId AND bs.cas_group =:blendingCasGroup   " + "   )     "
            + " AND ( :branch is null or b.branch =:branch )    "
            + " AND b.lot <> 0 AND stkqty > 0  AND ( b.lockstatus <> 'Y' OR b.lockstatus IS NULL )     "
            + " AND ( :productId is null or b.refid = :productId )     "
            + " AND ( :binName is null or b.binname = :binName )     " + " AND ( :lot is null or b.lot = :lot )      "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingCasgrp(@Param("blendingCasGroup") String blendingCasGroup,
                                        @Param("journalId") Long journalId, @Param("branch") Long branch, @Param("productId") Long productId,
                                        @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = " SELECT distinct b.*        " + " FROM blending_advice_bin_details bb , binstock b        "
            + " where  b.batchno = bb.batch_no AND b.refid = bb.product_id    " + " AND  bb.branch_id  = b.branch    "
            + " AND  bb.blending_advice_stock_id IN    " + "   (   "
            + "       SELECT  distinct bs.id  FROM blending_advice ba    "
            + "       LEFT JOIN blending_advice_details bad on bad.blending_advice_id = ba.id    "
            + "       LEFT JOIN blending_advice_stock_details bs on bs.blending_advice_details_id = bad.id    "
            + "       WHERE  ba.status = 'A' AND bad.status = 'A' AND  ba.journal_id =:journalId    "
            + "       AND bad.branch_id =:branch    "
            + "       AND bs.product_id =:productId AND bs.cas_group =:blendingCasGroup   " + "   )     "
            + " AND ( :branch is null or b.branch =:branch )    "
            + " AND b.lot <> 0 AND stkqty > 0  AND ( b.lockstatus <> 'Y' OR b.lockstatus IS NULL )     "
            + " AND ( :productId is null or b.refid = :productId )     "
            + " AND ( :binName is null or b.binname = :binName )     " + " AND ( :lot is null or b.lot = :lot )      "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingCasgrp1(@Param("blendingCasGroup") String blendingCasGroup,
                                         @Param("journalId") Long journalId, @Param("branch") Long branch, @Param("productId") Long productId,
                                         @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = "SELECT * FROM binstock b WHERE b.inwarddate IS NOT NULL AND b.oil_FFA IS NOT NULL AND b.location IS NOT NULL "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) order by b.id ASC limit 1 ", nativeQuery = true)
    Optional<BinStock> findByBatch(@Param("productId") Long productId, @Param("batchNo") String batchNo);

    @Query(value = " SELECT * FROM binstock b WHERE b.inwarddate IS NOT NULL "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) order by b.id ASC limit 1 ", nativeQuery = true)
    Optional<BinStock> findByBatchInwardDateNotNull(@Param("productId") Long productId,
                                                    @Param("batchNo") String batchNo);

    @Query(value = " SELECT * FROM binstock b WHERE b.oil_ffa IS NOT NULL "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) order by b.id ASC limit 1 ", nativeQuery = true)
    Optional<BinStock> findByBatchOilFFANotNull(@Param("productId") Long productId, @Param("batchNo") String batchNo);

    @Query(value = "SELECT * FROM binstock b WHERE b.seedmoisture IS NOT NULL AND b.seedmoisture  != '' "
            + " AND (:productId is null or b.refid = :productId) "
            + " AND (:batchNo is null or b.batchno = :batchNo) order by b.id ASC limit 1 ", nativeQuery = true)
    Optional<BinStock> findByBatchMoistureNotNull(@Param("productId") Long productId, @Param("batchNo") String batchNo);

    @Query(value = " SELECT * FROM binstock b WHERE   "
            + " b.refid =:itemId AND b.binname =:binName AND b.batchno =:issuedFromBatch "
            + " AND b.lot =:lot AND ( b.locktype NOT IN ('BU' ,'SW' , 'SB' , 'FU' ,'QI' , 'SR') or b.locktype IS NULL  or b.mrnstatus IS NULL or b.mrnstatus ='N' ) AND b.stkqty =:qty  limit 1", nativeQuery = true)
    BinStock findByBinStockObject(@Param("itemId") Long itemId, @Param("issuedFromBatch") String issuedFromBatch,
                                  @Param("binName") Long binName, @Param("lot") Double lot, @Param("qty") Double qty);

    @Query(value = " SELECT * FROM binstock b WHERE   "
            + " b.refid =:itemId AND b.binname =:binName AND b.batchno =:issuedFromBatch  "
            + " AND b.lot =:lot AND ( b.locktype NOT IN ('BU' ,'SW' , 'SB' , 'FU' ,'QI' , 'SR') or b.locktype IS NULL or b.mrnstatus IS NULL or b.mrnstatus ='N' )   ", nativeQuery = true)
    List<BinStock> findByBinStockObject(@Param("itemId") Long itemId, @Param("issuedFromBatch") String issuedFromBatch,
                                        @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = "SELECT b.binname as binName , max(b.binid) as binId , b.lot as lot , b.refid  as productId , "
            + " max(pm.name) as productName,  b.branch as branch , SUM(b.stkqty)  as qty from binstock b "
            + " left join products_master pm on pm.id = b.refid  " + " WHERE b.stkqty > 0  AND b.binname <> 0 "
            + " AND b.refid =:productId  AND b.lot =:lot AND b.binname =:binName "
            + " AND (:dept is null or b.dept = :dept) " + " AND (:branch is null or b.branch = :branch) "
            + " GROUP BY  b.binname , b.lot ,  b.refid , b.branch , b.dept limit 1 ", nativeQuery = true)
    StockPaper getStockPaperItems(@Param("binName") Long binName, @Param("productId") Long productId,
                                  @Param("lot") Double lot, @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT b.binname as binName from binstock b WHERE b.stkqty > 0  AND b.binname <> 0 "
            + " AND b.refid =:productId " + " AND b.lot =:lot " + " AND (:dept is null or b.dept = :dept) "
            + " AND (:branch is null or b.branch = :branch)  ORDER BY  b.binname ASC ", nativeQuery = true)
    List<Long> getStockPaperBinList(@Param("productId") Long productId, @Param("lot") Double lot,
                                    @Param("branch") Long branch, @Param("dept") Long dept);

    @Query(value = "SELECT distinct b.*     " + " from blending_advice_bin_details ba , binstock b     "
            + " where b.binname = ba.bin_no AND b.batchno = ba.batch_no AND b.refid = ba.product_id  AND ba.mainlot = b.lot "
            + " AND  ba.branch_id  = b.branch "
            + " AND ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  )  "
            + " AND ( :branch is null or b.branch =:branch ) "
            + " AND b.lot <> 0 AND b.stkqty > 0  AND  b.lockstatus = 'Y' AND b.locktype ='DT'   "
            + " AND ( :productId is null or b.refid = :productId )  "
            + " AND ( :binName is null or b.binname = :binName )  " + " AND ( :lot is null or b.lot = :lot )   "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingStockIdWithDelite(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                                   @Param("branch") Long branch, @Param("productId") Long productId, @Param("binName") Long binName,
                                                   @Param("lot") Double lot);

    @Query(value = " SELECT distinct b.*     " + " from blending_advice_bin_details ba , binstock b    "
            + " where b.batchno = ba.batch_no AND b.refid = ba.product_id   " + " AND  ba.branch_id  = b.branch"
            + " AND ( :blendingAdviceStockId is null or ba.blending_advice_stock_id =:blendingAdviceStockId  ) "
            + " AND ( :branch is null or b.branch =:branch )"
            + " AND b.lot <> 0 AND stkqty > 0  AND  b.lockstatus = 'Y' AND b.locktype ='DT' "
            + " AND ( :productId is null or b.refid = :productId ) "
            + " AND ( :binName is null or b.binname = :binName ) " + " AND ( :lot is null or b.lot = :lot )  "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingStockIdWithDelite1(@Param("blendingAdviceStockId") Long blendingAdviceStockId,
                                                    @Param("branch") Long branch, @Param("productId") Long productId, @Param("binName") Long binName,
                                                    @Param("lot") Double lot);

    @Query(value = "SELECT distinct b.*        " + " FROM blending_advice_bin_details bb , binstock b        "
            + " where b.binname = bb.bin_no AND b.batchno = bb.batch_no AND b.refid = bb.product_id AND bb.mainlot = b.lot    "
            + " AND  bb.branch_id  = b.branch    " + " AND  bb.blending_advice_stock_id IN    " + "   (   "
            + "       SELECT  distinct bs.id  FROM blending_advice ba    "
            + "       LEFT JOIN blending_advice_details bad on bad.blending_advice_id = ba.id    "
            + "       LEFT JOIN blending_advice_stock_details bs on bs.blending_advice_details_id = bad.id    "
            + "       WHERE  ba.status = 'A' AND bad.status = 'A' AND  ba.journal_id =:journalId    "
            + "       AND bad.branch_id =:branch    "
            + "       AND bs.product_id =:productId AND bs.cas_group =:blendingCasGroup   " + "   )     "
            + " AND ( :branch is null or b.branch =:branch )    "
            + " AND b.lot <> 0 AND b.stkqty > 0 AND  b.lockstatus = 'Y' AND b.locktype ='DT'    "
            + " AND ( :productId is null or b.refid = :productId )     "
            + " AND ( :binName is null or b.binname = :binName )     " + " AND ( :lot is null or b.lot = :lot )      "
            + " ORDER BY b.inwarddate DESC ", nativeQuery = true)
    List<BinStock> findByBlendingCasgrpWithDelite(@Param("blendingCasGroup") String blendingCasGroup,
                                                  @Param("journalId") Long journalId, @Param("branch") Long branch, @Param("productId") Long productId,
                                                  @Param("binName") Long binName, @Param("lot") Double lot);

    @Query(value = "SELECT  *  FROM binstock b " + " WHERE b.binname =:binName AND b.binid =:binId "
            + " AND b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 AND  b.lockstatus = 'Y' AND b.locktype ='DT' ORDER BY b.inwarddate ASC ", nativeQuery = true)
    List<BinStock> findByBinStocksDelite(@Param("productId") Long productId, @Param("binName") Long binName,
                                         @Param("binId") String binId, @Param("lot") Double lot);

    @Transactional
    @Modifying
    @Query(value = " update binstock b set  lockstatus=:lockstatus, locktype=:locktype  where b.binname =:binName and b.category <>'WaitingForResult' and ( b.mrnstatus = 'N' OR  b.mrnstatus IS NULL ) ", nativeQuery = true)
    void updateLockStatus(@Param("binName") Long binName, @Param("lockstatus") String lockstatus,
                          @Param("locktype") String locktype);

    @Transactional
    @Modifying
    @Query(value = " update binstock b set  lockstatus=:lockstatus, locktype=:locktype  where b.binname =:binName and b.category = 'WaitingForResult' and ( b.mrnstatus = 'N' OR  b.mrnstatus IS NULL ) ", nativeQuery = true)
    void updateWaitingForResultStatus(@Param("binName") Long binName, @Param("lockstatus") String lockstatus,
                                      @Param("locktype") String locktype);

    @Transactional
    @Modifying
    @Query(value = " update binstock b set  lockstatus=:lockstatus, locktype=:locktype  where b.binname =:binName and b.category = 'WaitingForResult' and ( b.mrnstatus = 'N' OR  b.mrnstatus IS NULL ) ", nativeQuery = true)
    void updateWaitingForResultStatusWithMRN(@Param("binName") Long binName, @Param("lockstatus") String lockstatus,
                                             @Param("locktype") String locktype);

    @Query(value = "SELECT  *  FROM binstock b  " + " WHERE b.refid =:productId AND b.batchno =:batchNo "
            + " AND b.batchid =:batchId AND b.binname =:binname " + " AND b.lot=:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBin(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                                @Param("batchId") Long batchId, @Param("binname") long binname, @Param("lot") double lot);

    @Query(value = "SELECT  *  FROM binstock b  " + " WHERE b.refid =:productId AND b.batchno =:batchNo "
            + " AND b.lot=:lot limit 1", nativeQuery = true)
    Optional<BinStock> findByProductBatchAndBin(@Param("productId") Long productId, @Param("batchNo") String batchNo,
                                                @Param("lot") double lot);

    @Query(value = "SELECT *  FROM binstock b  WHERE b.refid =:productId AND b.batchno =:batchNo "
            + "  AND b.batchid =:batchId AND b.branch =:branchId AND b.dept =:deptId ", nativeQuery = true)
    List<BinStock> findByBinStockDelete(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                        @Param("productId") Long productId, @Param("batchId") Long batchId, @Param("batchNo") String batchNo);

    @Query(value = " SELECT distinct g.goname FROM  gogroup g  , gogroup_palletnumber_map gp "
            + " WHERE gp.bin_id = g.goname  and  g.branch =:branchId AND g.dept =:deptId "
            + "             AND (:type is null OR g.binType =:type)  ORDER BY g.goname asc   ", nativeQuery = true)
    List<String> filterAsrsBillBinListDeptWise(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                               @Param("type") String type);

    @Query(value = "SELECT distinct binname FROM binstock  b  WHERE (:branchId is null or b.branch = :branchId) AND (:dept is null or b.dept = :dept) "
            + " AND (:productId is null or b.refid = :productId)  AND (b.stkqty > 0) ORDER BY binname asc ", nativeQuery = true)
    List<Long> findBinNoList(Long branchId, Long dept, Long productId);

    @Query(value = "SELECT  * FROM binStock b "
            + "WHERE b.refid = :productId AND b.binname = :binNo AND b.stkqty > 0 AND LOWER(b.batchno) LIKE 'prdn%' ", nativeQuery = true)
    List<BinStock> findBatchNoByBinNo(@Param("productId") Long productId, @Param("binNo") Long binNo);

    @Query(value = " SELECT " + "            b.binname as binNo, gpm.pallet_number as palletNo, "
            + "            COALESCE(SUM(CAST(REPLACE(NULLIF(ap.allocatedQty, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) as qty, b.lot , "
            + "            b.refid as productId , b.batchno as batchNo , max(pm.name) as productName "
            + "            FROM binstock b " + "            LEFT JOIN products_master pm on pm.id = b.refid "
            + "            LEFT JOIN gogroup_palletnumber_map gpm on gpm.bin_id = b.binname "
            + "            LEFT JOIN wcshift w on w.batchno = b.batchno "
            + "            LEFT JOIN asrs_production_detail apd on apd.trnno  = w.trnno "
            + "            LEFT JOIN asrs_pallet ap on ap.asrsproductionid  = apd.id "
            + "            WHERE (:productId is null or b.refid = :productId) "
            + "            AND (:binNo is null or b.binname = :binNo)  "
            + "            AND (:palletNo is null or gpm.pallet_number = :palletNo) "
            + "            AND (:lot is null or b.lot = :lot) AND lot IS NOT NULL " + "            AND (stkqty > 0 ) "
            + "            AND ap.palletnumber = gpm.pallet_number and ap.finalasrsqty ='0'"
            + "            GROUP BY  b.binname , gpm.pallet_number, ap.palletnumber , b.lot , b.refid , b.batchno", nativeQuery = true)
    List<SplitBinLotWiseStock> getBinLotWiseStockSplitWithPalletNo(@Param("productId") Long productId,
                                                                   @Param("binNo") Long binNo, @Param("palletNo") String palletNo, @Param("lot") Double lot);

    @Query(value = " SELECT * FROM binstock b WHERE b.batchno = :batchNo AND b.binname = :binNo "
            + "  AND b.refid = :productId AND (b.lockstatus IS NULL OR UPPER(b.lockstatus) NOT LIKE 'Y') and b.stkqty>0 order by id asc limit 1", nativeQuery = true)
    Optional<BinStock> findAvailableQuantity(@Param("batchNo") String batchNo, @Param("binNo") Long binNo,
                                             @Param("productId") Long productId);

    @Query(value = " SELECT * FROM binstock b WHERE b.dept = :dept AND b.branch = :branch "
            + "  AND b.refid = :conProduct AND (b.lockstatus IS NULL OR UPPER(b.lockstatus) NOT LIKE 'Y') AND b.lot= :lot AND b.stkqty>0  order by id asc", nativeQuery = true)
    List<BinStock> findStockByConProduct(@Param("conProduct") Long conProduct, @Param("dept") Long dept,
                                         @Param("branch") Long branch, @Param("lot") Double lot);

    @Query(value = "  SELECT * FROM binstock b  "
            + "  left join gogroup_palletnumber_map gpm on gpm.bin_id = b.binname  "
            + "  WHERE b.ltrnno = :trnNo AND b.refid =:refid  "
            + "  AND gpm.pallet_number =:palletNo AND gpm.pallet_type ='Packing' limit 1", nativeQuery = true)
    List<BinStock> findBy(@Param("trnNo") String trnNo, @Param("refid") Long refid, @Param("palletNo") String palletNo);

    @Query(value = "  SELECT  b.qty FROM stbin b  where b.batchno = :batchNo AND b.gname= :binNo AND b.refid= :refId AND b.trnno = :trnno  ", nativeQuery = true)
    Double findByDispatchDetails(@Param("batchNo") String batchNo, @Param("binNo") Long binNo,
                                 @Param("refId") Long refId, @Param("trnno") String trnno);

    @Query(value = "select * from binstock b where b.branch =:branch and b.cid =:company  and b.refid =:refid\r\n"
            + "			and b.stkqty >0 ORDER BY b.id asc limit 1", nativeQuery = true)
    BinStock findByBranchAndCidAndDeptAndRefid(@Param("branch") Long branch, @Param("company") Long company,
                                               @Param("refid") Long refid);

    @Query(value = "  SELECT  * FROM binstock b  left join dpatch_despatch dd on dd.trn_no = b.ltrnno where dd.order_ref_id = :id ", nativeQuery = true)
    List<BinStock> findByDispatchDetailss(@Param("id") Long id);


    @Query(value = "SELECT  *  FROM binstock b "
            + " WHERE b.lot =:lot AND b.refid =:productId AND b.stkqty > 0 ORDER BY b.id ASC ", nativeQuery = true)
    List<BinStock> findByBinStocks(@Param("productId") Long productId, @Param("lot") Double lot);


    @Query(value = "SELECT * FROM binstock b " + " WHERE (:binName is null or b.binname = :binName) "
            + " AND (:batchNo is null or b.batchno = :batchNo) AND b.dept = :dept AND b.branch = :branch LIMIT 1 ", nativeQuery = true)
    Optional<BinStock> findBySplit(@Param("binName") Long binName, @Param("batchNo") String batchNo, @Param("branch") Long branch, @Param("dept") Long dept);


    // Day End Stock Group branch wise

    @Query(value = " SELECT max(pm.name) as itemName, pm.id as itemId, b.branch as branchId, COALESCE( SUM( (b.stkqty * lot) ), 0.0 ) as qty "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE "
            + " b.refid NOT IN (SELECT br.id as itemId FROM brmproids br WHERE br.day_end_closing_flag = true AND (br.bid IN (:branchList) ) ) "
            + " AND b.stkqty > 0 AND b.binname <> 0 AND (b.branch IN (:branchList))  "
            + " GROUP BY b.refid , b.branch, pm.id order by pm.id ", nativeQuery = true)
    List<DayEndStockList> findByClosingStockDetails(@Param("branchList") List<Long> branchList);


    @Query(value = "SELECT  b.refid, b.branch, sum(b.stkqty) as qty , b.lot , max(pm.name) as productName, b.binname as binname , b.gid as gid "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE b.stkqty > 0 AND b.binname <> 0 AND b.gid <> 0 "
            + " AND ( b.lot IS NOT NULL or b.lot <> 0 )   "
            + " AND ( b.branch IN (:branchList)) "
            + " GROUP BY  b.branch,  b.lot, b.refid, b.gid, b.binname "
            + " ORDER BY  b.refid  ASC ", nativeQuery = true)
    List<DayEndClosingStockList> getDayEndClosingStockItems(@Param("branchList") List<Long> branchList);

   /* @Query(value = "SELECT  b.refid, b.binid, b.binname, b.branch,  b.dept, sum(b.stkqty) as qty , b.lot , max(pm.name) as productName "
            + " FROM binstock b left join products_master pm on pm.id = b.refid WHERE b.stkqty > 0 AND b.binname <> 0 "
            + " AND ( b.lot IS NOT NULL or b.lot <> 0 )   "
            + " AND ( b.refid IN ( select distinct br.id from brmproids br where br.bid =:branch ) )"
            + " AND ( b.branch IN (:branchList)) "
            + " GROUP BY b.binid, b.binname, b.branch, b.dept, b.lot, b.refid "
            + " ORDER BY  b.binname ASC ", nativeQuery = true)
    List<PhysicalStockItems> getPhysicalStockItems(@Param("branchList") List<Long> branchList);*/


    @Query(value =
            "SELECT b.refid, b.binid, b.binname, b.branch, b.dept, " +
                    "       SUM(b.stkqty) AS qty, b.lot, MAX(pm.name) AS productName " +
                    " FROM binstock b " +
                    "LEFT JOIN products_master pm ON pm.id = b.refid " +
                    "WHERE b.stkqty > 0 " +
                    "AND b.binname IS NOT NULL " +
                    "AND b.binname <> 0 " +
                    "AND b.lot IS NOT NULL " +
                    "AND b.lot <> 0 " +
                    "AND b.branch IN (:branchList) " +
                    "AND EXISTS ( " +
                    "    SELECT 1 " +
                    "    FROM brmproids br " +
                    "    WHERE br.id = b.refid " +
                    "    AND br.bid IN (:branchList) " +
                    ") " +
                    "GROUP BY b.refid, b.binid, b.binname, b.branch, b.dept, b.lot " +
                    "ORDER BY b.binname ASC",
            nativeQuery = true)
    List<PhysicalStockItems> getPhysicalStockItems(@Param("branchList") List<Long> branchList);


}
