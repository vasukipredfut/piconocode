package com.aria.dao;

import com.aria.model.PurchaseBrokerage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseBrokerageRepository extends JpaRepository<PurchaseBrokerage, Long> {

}
