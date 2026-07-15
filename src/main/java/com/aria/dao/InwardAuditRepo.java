package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.InwardAudit;

@Repository
public interface InwardAuditRepo extends JpaRepository<InwardAudit, Long> {

	@Query(value = "SELECT * FROM inward_audit ia WHERE ia.batch_no =:batchNo AND ia.product_id =:productId AND ia.entry_type =:entryType limit 1", nativeQuery = true)
	Optional<InwardAudit> findByInwardCheck(@Param("batchNo") String batchNo, @Param("entryType") String entryType,
			@Param("productId") Long productId);

	@Query(value = "Select * FROM inward_audit ia WHERE ia.batch_no =:batchNo AND ia.entry_type =:entryType", nativeQuery = true)
	Optional<InwardAudit> findroqComment(@Param("batchNo") String batchNo, @Param("entryType") String entryType);
}
