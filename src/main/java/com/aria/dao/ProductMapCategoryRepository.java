package com.aria.dao;

import com.aria.model.ProductCategory;
import com.aria.model.Units;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMapCategoryRepository extends JpaRepository<ProductCategory, Long> {


    @Query(value = "SELECT id FROM product_map_category WHERE name = :name", nativeQuery = true)
    List<Long> findAllBytype(@Param("name") String name);
}
