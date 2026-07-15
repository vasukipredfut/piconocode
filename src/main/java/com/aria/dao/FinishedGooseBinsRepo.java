package com.aria.dao;


import com.aria.model.BinLock;
import com.aria.model.FinishedGooseBins;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinishedGooseBinsRepo extends JpaRepository<FinishedGooseBins, Long> {


    @Query(value = "select  max(a.id) as id, max(a.refid) as  refid, max(a.bid ) as bid,"
            + " max(a.type) as type, a.binname, a.name "
            + " from fgbins as a inner join gogroup as b on a.binname = b.goname   "
            + " WHERE b.journal_bye = 1 AND (:binId is null or a.binname = :binId) "
            + " AND (:branchId is null or a.bid = :branchId) "
            + " AND (:productId is null or a.refid = :productId) "
            + " AND (:type is null or a.type = :type) "
            + " AND (:name is null or (lower(a.name) LIKE ('%' || lower(:name) || '%')))  "
            + " group by b.goname, a.binname, a.name ", nativeQuery = true)
    List<FinishedGooseBins> finishedGooseBinsList(@Param("binId") Long binId,
                                                  @Param("branchId") Long branchId,
                                                  @Param("productId") Long productId,
                                                  @Param("type") String type,
                                                  @Param("name") String name);

    @Query(value = "select  "
            + " max(a.id) as id, max(a.refid) as  refid, max(a.bid ) as bid,"
            + " max(a.type) as type, a.binname, a.name "
            + " from fgbins as a "
            + " left join gogroup as b on a.binname = b.goname   "
            + " left join gogroup_palletnumber_map as c on a.binname = c.bin_id   "
            + " WHERE  c.bin_id = a.binname  AND  b.journal_bye = 1 "
            + " AND (:binId is null or a.binname = :binId) "
            + " AND (:branchId is null or a.bid = :branchId) "
            + " AND (:productId is null or a.refid = :productId) "
            + " AND (:type is null or a.type = :type) "
            + " AND (:name is null or (lower(a.name) LIKE ('%' || lower(:name) || '%')))  "
            + " group by b.goname, a.binname, a.name ", nativeQuery = true)
    List<FinishedGooseBins> asrsFinishedGooseBinsList(@Param("binId") Long binId,
                                                  @Param("branchId") Long branchId,
                                                  @Param("productId") Long productId,
                                                  @Param("type") String type,
                                                  @Param("name") String name);


}
