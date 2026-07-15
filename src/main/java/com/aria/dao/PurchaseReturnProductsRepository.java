package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.PurchaseReturnProducts;

import jakarta.transaction.Transactional;

@Repository
public interface PurchaseReturnProductsRepository extends JpaRepository<PurchaseReturnProducts, Long> {
	

	List<PurchaseReturnProducts> findByPurchaseReturnId(Long purchaseReturnId);

	@Transactional
	@Modifying
	@Query("DELETE FROM PurchaseReturnProducts prp WHERE prp.purchaseReturnId = ?1")
	void deleteByPurchaseReturnId(Long purchaseReturnId);

}
