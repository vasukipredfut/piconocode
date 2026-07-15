package com.aria.dao;

import com.aria.model.CanteenMenuDayMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.MenuItems;

import java.util.List;

@Repository
public interface MenuItemsRepository extends JpaRepository<MenuItems, Long> {


    @Query(value = "SELECT  *  FROM menu_items m WHERE m.food_id =:foodId and m.food_item_id =:foodItemId and m.menutype=:menuType", nativeQuery = true)
    List<MenuItems> findAll(@Param("foodId") Long foodId, @Param("foodItemId") Long foodItemId, @Param("menuType") String menuType);
}