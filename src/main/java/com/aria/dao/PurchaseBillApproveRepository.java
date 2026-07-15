package com.aria.dao;

import com.aria.model.PurchaseBillApprove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseBillApproveRepository  extends JpaRepository<PurchaseBillApprove, Long> {

}
