package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.DispatchOrderWithPrice;

@Repository
public interface DispatchOrderWithPriceRepository extends JpaRepository<DispatchOrderWithPrice, Long> {

}
