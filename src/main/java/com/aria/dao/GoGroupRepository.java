package com.aria.dao;


import com.aria.dto.BinNoWithLockTypeList;
import com.aria.dto.GoGroupList;
import com.aria.model.GoGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoGroupRepository extends JpaRepository<GoGroup, Long> {

    @Query(value = "SELECT  *  FROM gogroup g WHERE g.goname =:binNo", nativeQuery = true)
    Optional<GoGroup> findByBinNo(@Param("binNo") Long binNo);

    @Query(value = "SELECT  *  FROM gogroup g WHERE g.gcode =:binNo", nativeQuery = true)
    Optional<GoGroup> findByBinId(@Param("binNo") String binNo);

    @Query(value = "select count(distinct binprimaryid) as count from gogroup gg LEFT JOIN binrange br ON br.dept = gg.dept WHERE br.branch = gg.branch " +
            "AND ( :branch is null or br.branch =:branch) AND  (:dept is null or br.dept =:dept) and (gg.goname >=:slfm AND gg.goname <=:slto)", nativeQuery = true)
    Integer findAllocatedBinCount(@Param("branch") Long branch, @Param("dept") Long dept, @Param("slfm") Long slfm, @Param("slto") Long slto);

    @Query(value = "Select og.id as companyId, og.name as companyName, br.id as branchId, br.name as branchName, gd.id as godownId , gd.name as godownName, gg.gcode as binCode ,  gg.bintype as binType ,  gg.capacity as capacity , gg.packages as packages ,  "
            +" gg.goname as binId ,gg.display as display ,Max(gmap.pallet_type) AS palletType , STRING_AGG(DISTINCT gmap.pallet_number , ' , ' ORDER BY gmap.pallet_number) as palletNumber  FROM gogroup gg LEFT JOIN godown gd ON gd.id = gg.gid LEFT JOIN organisation og ON og.id = gg.cid LEFT JOIN branch br ON br.id = gg.branch LEFT JOIN gogroup_palletnumber_map gmap ON gmap.bin_id = gg.goname "
            + "where (:godownId is null or gg.gid = :godownId) AND (:firmId is null or gg.cid = :firmId) AND (:branch is null or gg.branch = :branch) AND  (:dept is null or gg.dept = :dept) and (gg.goname >= :slfm AND gg.goname <= :slto)    "
            + "   GROUP BY og.id, og.name, br.id, br.name, gd.id, gd.name, gg.gcode, gg.bintype, gg.capacity, gg.packages, gg.goname, gg.display  ORDER BY gg.goname ASC ", nativeQuery = true)
    List<GoGroupList> getViewList(@Param("godownId") Long godownId, @Param("firmId") Long firmId, @Param("branch") Long branch, @Param("dept") Long dept, @Param("slfm") Long slfm, @Param("slto") Long slto);

    @Query(value = "SELECT  *  FROM gogroup g WHERE g.gcode =:binNo or g.goname=:goname LIMIT 1", nativeQuery = true)
    Optional<GoGroup> findByBinNo(@Param("binNo") String binNo , @Param("goname") Long goname);

    @Query(value = "SELECT   gcode  FROM gogroup where gcode <> ''  ORDER BY binprimaryId desc LIMIT 1", nativeQuery = true)
    String findByDescOne();


    @Query(value = " Select gg.goname as binId  FROM gogroup gg LEFT JOIN godown gd ON gd.id = gg.gid LEFT JOIN organisation og ON og.id = gg.cid LEFT JOIN branch br ON br.id = gg.branch "
            + " where  (:branch is null or gg.branch =:branch) AND  (:dept is null or gg.dept =:dept) and (:binNo is null or ( CAST(gg.goname AS text) like ('%' || (:binNo) || '%')) ) "
            + " AND  (:binType is null or (lower(gg.bintype) LIKE ('%' || lower(:binType) || '%')) ) AND (:godownId is null or gg.gid =:godownId)  ORDER BY gg.goname ASC", nativeQuery = true)
    List<Long> getFindBinList(@Param("binType") String binType, @Param("binNo") Long binNo, @Param("branch") Long branch, @Param("dept") Long dept, @Param("godownId") Long godownId );

    @Query(value = "SELECT  *  FROM gogroup g WHERE g.goname=:binId", nativeQuery = true) 
	Optional<GoGroup> findBygoName(@Param("binId")Long binId);


    @Query(value = " Select gg.goname as binNo , max(b.locktype) as lockType  FROM gogroup gg LEFT JOIN godown gd ON gd.id = gg.gid LEFT JOIN organisation og ON og.id = gg.cid LEFT JOIN branch br ON br.id = gg.branch " +
            "   LEFT JOIN binstock b on b.binname = gg.goname  where   (:branch is null or gg.branch =:branch) AND  (:dept is null or gg.dept =:dept) and (:binNo is null or ( CAST(gg.goname AS text) like ('%' || (:binNo) || '%')) ) "
            + " AND  (:binType is null or (lower(gg.bintype) LIKE ('%' || lower(:binType) || '%')) )  GROUP  BY gg.goname ORDER BY gg.goname ASC", nativeQuery = true)
    List<BinNoWithLockTypeList> findBinNoWithLockType(@Param("binType") String binType, @Param("binNo") Long binNo, @Param("branch") Long branch, @Param("dept") Long dept );


}
