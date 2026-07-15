package com.aria.dao;

import com.aria.model.BillReceiveProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillReceiveProductRepository extends JpaRepository<BillReceiveProduct, Long> {

}
