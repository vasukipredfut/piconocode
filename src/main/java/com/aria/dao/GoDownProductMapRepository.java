package com.aria.dao;

import com.aria.dto.GoDownSummaryList;
import com.aria.model.GoDownProductMap;
import com.aria.model.GoDownReportProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoDownProductMapRepository extends JpaRepository<GoDownProductMap,Long> {

    
    @Query(value = "SELECT gp.gid as godownId, gp.gd_name as godownName " +
            " FROM go_down_product_map gp " +
            " where (:branch is null or gp.bid =:branch ) " +
            " and ( :year is null  or :toYear is null or gp.year BETWEEN :year AND :toYear  ) " +
            " and ( :month is null  or :toMonth is null or gp.month BETWEEN :month AND :toMonth ) " +
            " and ( :itemId is null or gp.refid = :itemId ) " +
            " and ( :godownId is null or gp.gid = :godownId ) " +
            " group  by  gp.gid , gp.gd_name  ", nativeQuery = true)
    List<GoDownSummaryList> goDown (@Param("branch") Long branch ,
                                                      @Param("year") Long year ,
                                                      @Param("month") Long month ,
                                                      @Param("itemId") Long itemId ,
                                                      @Param("godownId") Long godownId,
                                                      @Param("toYear") Long toYear ,
                                                      @Param("toMonth") Long toMonth );

    @Query(value = "SELECT gp.gid as godownId, gp.gd_name as godownName ,gp.refid as itemId, pm.name as itemName  " +
            " FROM go_down_product_map gp left join products_master pm on pm.id = gp.refid " +
            " where (:branch is null or gp.bid =:branch ) " +
            " and ( :year is null  or :toYear is null or gp.year BETWEEN :year AND :toYear ) " +
            " and ( :month is null or :toMonth is null or gp.month BETWEEN :month AND :toMonth ) " +
            " and ( :itemId is null or gp.refid = :itemId ) " +
            " and ( :godownId is null or gp.gid = :godownId ) " +
            " order by gp.gid , gp.refid ASC ", nativeQuery = true)
    List<GoDownSummaryList> goDownSummaryProductList (@Param("branch") Long branch ,
                                                      @Param("year") Long year ,
                                                      @Param("month") Long month ,
                                                      @Param("itemId") Long itemId ,
                                                      @Param("godownId") Long godownId ,
                                                      @Param("toYear") Long toYear ,
                                                      @Param("toMonth") Long toMonth);


}
