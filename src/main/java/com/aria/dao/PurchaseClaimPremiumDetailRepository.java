package com.aria.dao;

import com.aria.model.PurchaseClaimPremiumDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseClaimPremiumDetailRepository extends JpaRepository<PurchaseClaimPremiumDetail, Long> {


}
