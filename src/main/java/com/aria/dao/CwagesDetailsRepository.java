package com.aria.dao;

import com.aria.dto.CwagesDet;
import com.aria.dto.CwagesDetailsList;
import com.aria.model.CwagesDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CwagesDetailsRepository extends JpaRepository<CwagesDetails,Long> {

    @Query(value = "SELECT * from cwagesdetail c "
            + " WHERE (:trnId is null or c.trnid =:trnId ) "
            + " AND  (:trnNo is null or c.trnno =:trnNo ) ",nativeQuery = true)
    List<CwagesDetails> findBy(@Param("trnId") Long trnId, @Param("trnNo") String trnNo);


    @Query(value = "SELECT cd.trnid as trnId, cd.trnno as trnNo , cd.cwagesid as cwagesId , c.name as wagesWorkName , c.codeno as codeNo , "
            + " cd.reftrnno as refTrnNo , cs.refid as productId , pm.name as productName , cd.urate as unitRate, cd.qty as qty   "
            + " from cwagesdetail cd left Join cwages c on c.id = cd.cwagesid left join  cwagessum cs on cs.id = cd.trnid "
            + " left join products_master pm on pm.id = cs.refid   "
            + " WHERE (:trnId is null or cd.trnid =:trnId ) "
            + " AND  (:trnNo is null or cd.trnno =:trnNo ) ",nativeQuery = true)
    List<CwagesDetailsList> findDetailList(@Param("trnId") Long trnId,
                                   @Param("trnNo") String trnNo);



    @Query(value = "SELECT cd.reftrnno as refTrnNo , max(cs.refid ) as productId , max(pm.name) as productName , cd.reftrntable as refTable  "
            + " from cwagesdetail cd left join  cwagessum cs on cs.id = cd.trnid left join products_master pm on pm.id = cs.refid "
            + " WHERE (:trnId is null or cd.trnid =:trnId ) AND  (:trnNo is null or cd.trnno =:trnNo ) "
            + " group by cd.trnno , cd.reftrnno , cd.reftrntable limit 1", nativeQuery = true)
    CwagesDet findByDetail(@Param("trnId") Long trnId, @Param("trnNo") String trnNo);

}
