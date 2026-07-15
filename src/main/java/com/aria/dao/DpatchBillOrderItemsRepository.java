package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aria.model.DpatchBillorderItems;

public interface DpatchBillOrderItemsRepository extends JpaRepository<DpatchBillorderItems,Long>{
	@Query(value="Select * from dpatch_billorder_items where dpatch_bill_order_ref_id =:id",nativeQuery = true)
	public List<DpatchBillorderItems> findByDpatch_bill_order_ref_id(Long id);

	@Query(value="SELECT * FROM dpatch_billorder_items WHERE dpatch_bill_order_ref_id =:orderId and product_ref_id =:productId",nativeQuery = true)
	public DpatchBillorderItems findByIdAndProductRefId(Long orderId,Long productId);
}
