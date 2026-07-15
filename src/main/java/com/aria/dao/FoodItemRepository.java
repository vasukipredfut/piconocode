package com.aria.dao;

import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.FoodItem;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

	@Query(value = "SELECT * FROM food_item fi WHERE (:foodName is null or fi.food_name = :foodName) "
			+ " AND (cast(:dateFrom as timestamp) is null or fi.created_date >= :dateFrom ) "
			+ " AND (cast(:dateTo as timestamp) is null or fi.created_date <= :dateTo )" , nativeQuery = true)
	Page<FoodItem> getFoodItemFilterList(@Param("foodName") String foodName,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

	@Query(value = " SELECT DISTINCT ON (LOWER(fi.food_name)) * FROM food_item fi " , nativeQuery = true) 
	List<FoodItem> findAllFood(); 

}
