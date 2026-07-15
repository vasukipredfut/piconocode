package com.aria.dao;

import com.aria.model.PurchaseReturnStoredBinDetails;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseReturnStoredBinDetailsRepository extends JpaRepository<PurchaseReturnStoredBinDetails, Long> {

	List<PurchaseReturnStoredBinDetails> findByReturnProductId(Long productId);

}
