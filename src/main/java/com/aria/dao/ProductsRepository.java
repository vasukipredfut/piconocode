package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.Products;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {

}