package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aria.model.DpatchItemsBatch;

public interface DpatchItemsBatchRepository extends JpaRepository<DpatchItemsBatch,Long>{
	@Query(value="SELECT * FROM dpatch_items_batch WHERE dpatch_despatch_ref_id =:orderId",nativeQuery = true)
	public List<DpatchItemsBatch> findByDpatch_desptach_ref_id(Long orderId);
}
