package com.aria.dao;

import com.aria.model.PurchaseBrokerageBreakup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseBrokerageBreakupRepository  extends JpaRepository<PurchaseBrokerageBreakup,Long> {


}
