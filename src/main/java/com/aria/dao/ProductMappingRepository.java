package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.ProductMapping;

public interface ProductMappingRepository extends JpaRepository<ProductMapping,Long> {

	@Query(value="select * from product_mapping where product_category_ref_id =:categoryId",nativeQuery = true)
	public List<ProductMapping> findAllByCateId(@Param("categoryId") int categoryId);
	
	@Query(value="select product_ref_id from product_mapping where product_category_ref_id =:categoryId",nativeQuery = true)
	public List<Long> findidByCateId(@Param("categoryId") int categoryId);
	
	
	@Query(value="select * from product_mapping where product_category_ref_id =:categoryId and product_ref_id =:refid", nativeQuery = true)
	public List<ProductMapping> findidByCateIdandrefid(@Param("categoryId") Long categoryId, @Param("refid") Long refid);


	@Query(value="select * from product_mapping where  product_ref_id =:refid", nativeQuery = true)
	public List<ProductMapping> findidByrefid(@Param("refid") Long refid);

	@Query(value="select product_category_ref_id from product_mapping where product_ref_id =:refid",nativeQuery = true)
	public List<Long> findBy(@Param("refid") Long refid);
}
