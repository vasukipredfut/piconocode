package com.aria.dao;

import com.aria.model.PurchaseClaimPremium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseClaimPremiumRepository extends JpaRepository<PurchaseClaimPremium,Long> {


}
