package com.aria.dao;

import com.aria.model.PurchaseClaimPremiumBreakup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseClaimPremiumBreakupRepository extends JpaRepository<PurchaseClaimPremiumBreakup,Long> {


}
