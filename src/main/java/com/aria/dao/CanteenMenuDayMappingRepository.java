package com.aria.dao;


import com.aria.model.CanteenMenu;
import com.aria.model.CanteenMenuDayMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CanteenMenuDayMappingRepository extends JpaRepository<CanteenMenuDayMapping, Long> {


    @Query(value = "SELECT  *  FROM canteen_menu_day c WHERE c.food_id =:foodId and c.day_id =:dayId ", nativeQuery = true)
    Optional<CanteenMenuDayMapping> findByCanteenMenuDay(@Param("foodId") Long foodId, @Param("dayId") Long dayId);



    @Query(value = "SELECT  *  FROM canteen_menu_day c WHERE c.food_id =:foodId and c.day_id =:dayId ", nativeQuery = true)
    List<CanteenMenuDayMapping> findAll(@Param("foodId") Long foodId, @Param("dayId") Long dayId);
}
