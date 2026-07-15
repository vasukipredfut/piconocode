package com.aria.dao;

import com.aria.dto.FinishedGoodsSummary;
import com.aria.model.FinishedGoodsMappingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinishedGoodsMappingDataRepository extends JpaRepository<FinishedGoodsMappingData, Long> {


    @Query(value = "SELECT   fd.ms as name , fd.table_id as id  FROM finished_goods_mapping_data fd where fd.table_id IS NOT NULL " +
            " group by fd.ms ,  fd.table_id order by fd.table_id  asc", nativeQuery = true)
    public List<FinishedGoodsSummary> getFinishedGoodsSummaryTableList();

    @Query(value = "SELECT fd.ss as name ,  fd.storder as orderNo FROM finished_goods_mapping_data fd where fd.table_id =:tableId " +
            "  group by fd.ss ,  fd.storder  order by fd.storder", nativeQuery = true)
    public List<FinishedGoodsSummary> getFinishedGoodsSummaryProductNameList(@Param("tableId") Long tableId);


    @Query(value = "SELECT fd.ss as sName,  fd.name as name , fd.itemorder  as orderNo , fd.refid as productId , fd.pds as pds " +
            " FROM finished_goods_mapping_data fd where fd.table_id =:tableId " +
            "  order by fd.itemorder  asc", nativeQuery = true)
    public List<FinishedGoodsSummary> getFinishedGoodsSummaryProductTypeNameList(@Param("tableId") Long tableId);


}
