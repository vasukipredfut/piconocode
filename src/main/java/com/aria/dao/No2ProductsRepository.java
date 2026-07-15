package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2Products;

@Repository
public interface No2ProductsRepository extends JpaRepository<No2Products, Long> {

	@Query(value = "SELECT *  FROM no2products ad WHERE ad.producttype =:producttype and ad.active=1 order by id DESC ", nativeQuery = true)
	List<No2Products> findAllByProductType(@Param("producttype") String producttype);

	@Query(value = "SELECT * from no2products ad WHERE ad.refid =:refId AND ad.producttype =:productType order by id DESC LIMIT 1", nativeQuery = true)
	Optional<No2Products> findByProductIdANDType(@Param("refId") Long refId, @Param("productType") String productType);

	@Query(value = "SELECT DISTINCT(ad.refid)  FROM no2products ad WHERE ad.producttype =:producttype and ad.active=1", nativeQuery = true)
	List<Long> findRefidByProductType(@Param("producttype") String producttype);

	@Modifying
	@Query(value = "update no2products pm set pm.active=0 where pm.refid =:productId", nativeQuery = true)
	void updateProductmapping(@Param("productId") Long productId);

	@Modifying
	@Query(value = "select * from no2products pm where pm.refid =:productId", nativeQuery = true)
	List<No2Products> getProductMapping(@Param("productId") Long productId);

	@Query(value = "SELECT * from no2products p WHERE p.refid =:productId LIMIT 1", nativeQuery = true)
	Optional<No2Products> findByProductId(@Param("productId") Long productId);
}
