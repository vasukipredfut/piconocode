package com.aria.dao;

import com.aria.dto.FilterPhysicalStockList;
import com.aria.model.PhysicalStock;
import com.aria.dto.PhysicalStockList;
import com.aria.dto.ProductStockList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalStockRepository extends JpaRepository<PhysicalStock, Long> {

    @Query(value = "SELECT * FROM physical_stock p WHERE  "
            + " ( Cast(:date as date) is null or (p.adate =:date) ) "
            + " AND (:branch is null or (p.bid =:branch) ) LIMIT 1", nativeQuery = true)
    Optional<PhysicalStock> findBY(@Param("branch") Long branch,
                                   @Param("date") Date date);

    @Query(value = "SELECT distinct p.id as id, p.refid as refid, p.name as name, p.binid as binid, p.binname as binname,  p.bid as bid, p.did as did, "
            + " p.qty as qty, p.lot as lot, p.adate as Adate , "
            + " ( case WHEN (s.gname IS NULL OR s.gname = 0) then false else true end ) as check , pm.name as productName "
            + " FROM physical_stock p left join products_master pm on pm.id = p.refid  "
            + " left join stbin s on s.trn_date = p.adate and s.gname = p.binname  "
            + " WHERE (s.gname IS NOT NULL  OR s.gname <> 0) AND ( Cast(:date as date) is null or (p.adate =:date) ) "
            + " AND (:branch is null or (p.bid =:branch) ) ", nativeQuery = true)
    List<PhysicalStockList> findAll(@Param("branch") Long branch, @Param("date") Date date);


    @Query(value = "SELECT  p.refid as itemId,  COALESCE( SUM( (p.physical_stk_qty * p.lot) ),0.0) as qty  FROM physical_stock p "
            + " WHERE  (:branch is null or p.bid = :branch) "
            + " AND ( Cast(:date as date) is null or (p.adate =:date) ) "
            + " AND p.refid  in (SELECT distinct id from brmproids br where br.bid =:branch AND br.day_end_closing_flag = true) "
            + " GROUP BY p.refid, p.adate order by p.refid  ", nativeQuery = true)
    List<ProductStockList> getPhysicalStock(@Param("branch") Long branch, @Param("date") Date date);


    @Query(value = "SELECT p.id as id, p.aby as aby,  p.aon as aon, p.refid as refid, p.name as name, p.binid as binid, p.binname as binname, p.gid as gid, p.bid as bid, "
            + " p.did as did, p.qty as qty, p.lot as lot, ( p.physical_stk_qty *  p.lot ) as physicalStkQty,  p.adate as Adate , "
            + " pm.name as productName , g.name as goDownName FROM physical_stock p left join products_master pm on pm.id = p.refid  "
            + " left join godown g on g.id = p.gid  WHERE ( Cast(:date as date) is null or (p.adate =:date) ) "
            + " AND (:branch is null or (p.bid =:branch) ) "
            + " AND (:lot is null or (p.lot =:lot) ) "
            + " AND (:binName is null or (p.binname =:binName) ) "
            + " AND (:dept is null or (p.did =:dept) ) ", nativeQuery = true)
    List<FilterPhysicalStockList> findBy(@Param("branch") Long branch,
                                         @Param("dept") Long dept,
                                         @Param("lot") Double lot,
                                         @Param("binName") Long binName,
                                         @Param("date") Date date);


    @Query(value = "SELECT * FROM physical_stock p WHERE  "
            + " ( Cast(:date as date) is null or (p.adate =:date) ) "
            + " AND (:productId is null or (p.refid =:productId) ) "
            + " AND (:branch is null or (p.bid =:branch) ) "
            + " AND (:dept is null or (p.did =:dept) ) "
            + " AND (:binId is null or (p.binid =:binId) ) "
            + " AND (:binName is null or (p.binname =:binName) ) "
            + " AND (:lot is null or (p.lot =:lot) ) limit 1", nativeQuery = true)
    Optional<PhysicalStock> findBY(@Param("productId") Long productId,
                                   @Param("branch") Long branch,
                                   @Param("dept") Long dept,
                                   @Param("binId") String binId,
                                   @Param("binName") Long binName,
                                   @Param("lot") Double lot,
                                   @Param("date") Date date);


    @Query(value = "SELECT COALESCE(SUM(ps.qty),0.0) from physical_stock ps WHERE " +
            "(:productId is null or ps.refid = :productId) " +
            "AND (:branch is null or ps.bid = :branch) " +
            "AND (:lot is null or ps.lot =:lot) " +
            "AND ( cast(:trnDate as date) is null or ps.adate = :trnDate) ", nativeQuery = true)
    Object rawMaterialMovementSummary(@Param("productId") Long productId,
                                      @Param("branch") Long branch,
                                      @Param("lot") Double lot,
                                      @Param("trnDate") Date trnDate);


    @Query(value = "SELECT * FROM physical_stock ps " +
            "WHERE ps.bid IN :branchList AND ps.adate = :adate", nativeQuery = true)
    List<PhysicalStock> findByBranchInAndAdate(
            @Param("branchList") List<Long> branchList,
            @Param("adate") Date adate
    );
}
