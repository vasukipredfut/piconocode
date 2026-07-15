package com.aria.dao;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2ProductsPartyMapping;
import com.aria.model.ProductPartyPrice;

@Repository
public interface No2ProductsPartyMappingRepository extends JpaRepository<No2ProductsPartyMapping, Long> {

	@Query(value = "SELECT pm.partyid as partyid, pd.party_name as partyName,pm.amount as price, pd.primary_party as primaryParty FROM no2productpartymapping pm, no2productsparty pd WHERE pd.partyid=pm.partyid AND pm.productid =:productId AND pm.product_type =:productType and pm.active=1", nativeQuery = true)
	List<ProductPartyPrice> findProductsByProductType(@Param("productId") Long productId,
			@Param("productType") String productType);

	@Query(value = "SELECT pm.* FROM no2productpartymapping pm " + "WHERE pm.partyid = :partyId "
			+ "AND pm.productid = :productId " + "AND pm.product_type = :productType " + "AND pm.active = 1 "
			+ "AND (:lot IS NULL OR pm.lot = :lot)", nativeQuery = true)
	List<No2ProductsPartyMapping> findAllByPartyProductOrLot(@Param("productId") Long productId,
			@Param("partyId") Long partyId, @Param("productType") String productType, @Param("lot") Long lot);

	@Query(value = "SELECT *  FROM no2productpartymapping pm WHERE  pm.productid =:productId AND pm.lot =:lot AND pm.product_type = :productType AND pm.active=1", nativeQuery = true)
	List<No2ProductsPartyMapping> findAllProductWithLot(@Param("productId") Long productId, @Param("lot") Long lot,
			@Param("productType") String productType);

	@Query(value = "select * from no2productpartymapping pm  where pm.productid = :productid AND pm.product_type = :productType", nativeQuery = true)
	List<No2ProductsPartyMapping> getPartymappingforProduct(@Param("productid") Long productid,
			@Param("productType") String productType);

	@Query(value = "select * from no2productpartymapping pm  where pm.partyid = :partyId AND pm.product_type = :productType", nativeQuery = true)
	List<No2ProductsPartyMapping> getmappingforParty(@Param("partyId") Long partyId,
			@Param("productType") String productType);

	@Query(value = "SELECT * FROM no2productpartymapping pm WHERE pm.partyid =:partyId AND pm.productid =:productId AND pm.product_type = :productType AND pm.active=1", nativeQuery = true)
	Optional<No2ProductsPartyMapping> findByPartyAndProduct(@Param("partyId") Long partyId,
			@Param("productId") Long productId, @Param("productType") String productType);

	@Modifying
	@Query(value = "UPDATE no2productpartymapping  SET active = 0 WHERE productid IN (:productList) and partyid =:partyId", nativeQuery = true)

	@Transactional
	void updatePartymapping(@Param("productList") List<Long> productList, @Param("partyId") Long partyId);

	@Modifying
	@Query(value = "update no2productpartymapping pm set pm.active=0 where pm.productid = :productid", nativeQuery = true)
	void updatePartymappingforProduct(@Param("productid") Long productid);

}
