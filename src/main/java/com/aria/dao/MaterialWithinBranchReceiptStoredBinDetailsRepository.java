package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.MaterialWithinBranchReceiptStoredBinDetails;

public interface MaterialWithinBranchReceiptStoredBinDetailsRepository extends JpaRepository<MaterialWithinBranchReceiptStoredBinDetails, Long> {

	List<MaterialWithinBranchReceiptStoredBinDetails> findByWithinItemId(Long id);

}
