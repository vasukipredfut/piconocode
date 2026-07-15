package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CanteenMenu;

@Repository
public interface CanteenMenuRepository extends JpaRepository<CanteenMenu, Long> {

    @Query(value = "SELECT * FROM canteen_menu cm WHERE ((cm.from_date >= :dateFrom) AND (cm.to_date <= :dateTo)) ", nativeQuery = true)
    List<CanteenMenu> canteenMenufilterList(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);


    @Query(value = "SELECT * FROM canteen_menu cm WHERE (cm.from_date >= :dateFrom) ", nativeQuery = true)
    List<CanteenMenu> canteenMenufilterListFromDate(@Param("dateFrom") Date dateFrom);

    @Query(value = "SELECT * FROM canteen_menu cm WHERE (cm.to_date <= :dateTo) ", nativeQuery = true)
    List<CanteenMenu> canteenMenufilterListToDate(@Param("dateTo") Date dateTo);

    @Query(value = "SELECT cm FROM CanteenMenu cm WHERE "
            + " ( (cm.fromDate = :dateFrom) OR (cm.toDate = :dateTo) )")
    List<CanteenMenu> canteenMenuFilterList(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

    @Query(value = "SELECT * FROM canteen_menu cm WHERE (cm.from_date =:date OR cm.to_date =:date)", nativeQuery = true)
    List<CanteenMenu> canteenMenuFilterList(@Param("date") Date date);
}