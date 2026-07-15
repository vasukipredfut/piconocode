package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.RequiredItemFood;

@Repository
public interface RequiredItemFoodRepository extends JpaRepository<RequiredItemFood, Long> {

}
