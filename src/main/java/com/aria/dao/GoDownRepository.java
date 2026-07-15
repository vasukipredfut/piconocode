package com.aria.dao;


import com.aria.model.GoDown;
import com.aria.model.GoDownList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GoDownRepository extends JpaRepository<GoDown, Long> {


    @Query(value = "select gd.id as goDownId , gd.name as goDownName, gd.address as goDownAddress, gd.godowntype as goDownType, br.name as branchName FROM godown gd LEFT JOIN branch br ON br.id = gd.branch "
            + "WHERE (:branchName is null or (lower(br.name) LIKE ('%' || lower(:branchName) || '%')) )"
            + "AND (:goDownName is null or (lower(gd.name) LIKE ('%' || lower(:goDownName) || '%')) )"
            + "AND (:goDownType is null or (lower(gd.godowntype) LIKE ('%' || lower(:goDownType) || '%')) ) "
            + "AND (:status is null or  (lower(gd.final) LIKE ('%' || lower(:status) || '%')) OR (gd.final='') ) order by gd.id desc",nativeQuery = true)
    Page<GoDownList> goDownFilterList(@Param("branchName") String branchName, @Param("goDownName") String goDownName,
                                                 @Param("goDownType") String goDownType, @Param("status") String status, Pageable pageable);


    @Query(value = "select gd.id as goDownId , gd.name as goDownName, gd.address as goDownAddress, gd.godowntype as goDownType, br.name as branchName FROM godown gd LEFT JOIN branch br ON br.id = gd.branch "
            + "WHERE (:branchName is null or (lower(br.name) LIKE ('%' || lower(:branchName) || '%')) )"
            + "AND (:goDownName is null or (lower(gd.name) LIKE ('%' || lower(:goDownName) || '%')) )"
            + "AND (:goDownType is null or (lower(gd.godowntype) LIKE ('%' || lower(:goDownType) || '%')) ) and (:firmId is null or gd.org_id =:firmId)"
            + "AND (:status is null or  (lower(gd.final) LIKE ('%' || lower(:status) || '%')) OR (gd.final='') )",nativeQuery = true)
    List<GoDownList> goDownFilterList(@Param("branchName") String branchName, @Param("goDownName") String goDownName, @Param("goDownType") String goDownType, @Param("status") String status, @Param("firmId") Long firmId);

    List<GoDown> findByBranch(Long branchId);

    @Query(value = "SELECT  *  FROM godown b WHERE b.name =:goDownName limit 1 ",nativeQuery = true)
    Optional<GoDown> findByName(@Param("goDownName") String goDownName);
    @Query(value = "select g.* from godown g left join gogroup gg on gg.gid = g.id where gg.goname=:binNo limit 1 ",nativeQuery = true)
    Optional<GoDown> findByGoName(@Param("binNo") Long binNo);


}
