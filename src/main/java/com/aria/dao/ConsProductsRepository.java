package com.aria.dao;

import com.aria.model.ConsProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsProductsRepository extends JpaRepository<ConsProducts,Long> {

}
