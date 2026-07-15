package com.aria.dao;

import com.aria.model.ExcessStoredBinDetails;
import com.aria.model.PurchaseReturnStoredBinDetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcessStoredBinDetailsRepository extends JpaRepository<ExcessStoredBinDetails, Long> {

//	List<PurchaseReturnStoredBinDetails> findByPurchaseReturnProductId(Long productId);

	List<ExcessStoredBinDetails> findByExcessProductId(Long productId);

}
