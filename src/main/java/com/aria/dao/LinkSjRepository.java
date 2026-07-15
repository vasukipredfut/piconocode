package com.aria.dao;

import com.aria.dto.LinkSjList;
import com.aria.model.LinkSj;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkSjRepository extends JpaRepository<LinkSj, Long> {

    @Query(value = "SELECT  sj.proid as productId, p.name as productName, sj.cate as category  FROM linksj sj LEFT JOIN products_master p ON p.id = sj.proid "
            + "WHERE sj.cate =:cate  AND (:journalId is null or sj.sjid = :journalId) order by sj.proid asc", nativeQuery = true)
    List<LinkSjList> findByRawMaterialList(@Param("journalId") Long journalId, @Param("cate") String cate);

}
