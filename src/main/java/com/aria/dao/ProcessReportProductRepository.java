package com.aria.dao;

import com.aria.dto.ProcessReportProductList;
import com.aria.model.ProcessReportProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessReportProductRepository extends JpaRepository<ProcessReportProduct, Long> {

    @Query(value = " SELECT prp.proid as productId , pm.name as productName " +
            " FROM process_report_product prp left join products_master pm on pm.id = prp.proid " +
            " where prp.prid =:reportNameId and prp.prgrpid =:reportGroupId and prp.proid <> 0 and prp.prprogrp =:reportProductGroupId " +
            " group by prp.proid , pm.name order by max(prp.id) ASC " , nativeQuery = true)
    List<ProcessReportProductList> findBy(@Param("reportNameId") Long reportNameId ,
                                          @Param("reportGroupId") Long reportGroupId,
                                          @Param("reportProductGroupId") Long reportProductGroupId);

}
