package com.aria.dao;

import com.aria.dto.BinList;
import com.aria.model.BinRange;
import com.aria.model.BinRangeList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BinRangeRepository extends JpaRepository<BinRange, Long> {


    @Query(value = "SELECT bg.id as binRangeId, dt.name as departmentName , br.name as branchName, og.name as firmName , gd.name as goDownName, bg.slfm as slFm, bg.slto as slTo FROM binrange bg LEFT JOIN branch br ON br.id = bg.branch LEFT JOIN department_details dt ON dt.id = bg.dept LEFT JOIN godown gd ON gd.id = bg.godown_id LEFT JOIN organisation og ON og.id = bg.org_id  "
            + "WHERE (:department is null or (lower(dt.name) LIKE ('%' || lower(:department) || '%')) )"
            + "AND (:goDownName is null or (lower(gd.name) LIKE ('%' || lower(:goDownName) || '%')) )"
            + "AND (:binNo is null or (:binNo BETWEEN bg.slfm and bg.slto ) ) ORDER BY bg.id DESC",nativeQuery = true)
    Page<BinRangeList> binRangeFilterList(@Param("goDownName") String goDownName,
                                        @Param("department") String department, @Param("binNo") Long binNo, Pageable pageable);

    @Query(value = "select gd.name as godownName, gd.id as godownId,  br.slfm as fromBin , br.slto as toBin , og.name as firmName, og.id as firmId from binrange br LEFT JOIN godown gd ON gd.id = br.godown_id LEFT JOIN organisation og ON og.id = br.org_id  " +
            "WHERE (:branch is null or br.branch =:branch) AND  (:dept is null or br.dept =:dept)  "
            + "AND (:goDownName is null or (lower(gd.name) LIKE ('%' || lower(:goDownName) || '%')) ) "
            + "AND (:firmName is null or (lower(og.name) LIKE ('%' || lower(:firmName) || '%')) ) ",nativeQuery = true)
    Page<BinList> binFilterList(@Param("branch") Long branch, @Param("dept") Long dept, @Param("goDownName") String goDownName, @Param("firmName") String firmName, Pageable pageable);


    @Query(value = "SELECT  * FROM binrange b WHERE ( ( b.slfm BETWEEN  :slfm AND :slto ) OR  ( b.slto BETWEEN  :slfm AND :slto ) )",nativeQuery = true)
    List<BinRange> findByBinFilterList(@Param("slfm") Long slfm, @Param("slto") Long slto);

	
    
    @Query(value="SELECT  COUNT(b) > 0 FROM BinRange b WHERE ( (:slfm <= :slto AND b.slfm <= :slto AND :slfm <= b.slto) OR (:slfm > :slto AND ((b.slfm >= :slfm OR b.slto >= :slfm) OR (b.slfm <= :slto OR b.slto <= :slto))))",nativeQuery = true)
    boolean existsOverlappingRange(@Param("slfm") Long slfm, @Param("slto") Long slto);

}
