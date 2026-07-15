package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.No2ProductsParty;

@Repository
public interface No2ProductsPartyRepository extends JpaRepository<No2ProductsParty, Long> {

	@Query(value = "SELECT * from no2productsparty pt WHERE pt.partyid =:partyid LIMIT 1", nativeQuery = true)
	Optional<No2ProductsParty> findByPartyId(@Param("partyid") Long partyid);

	@Query(value = "SELECT * from no2productsparty pt WHERE pt.partyid =:partyId AND pt.producttype =:productType LIMIT 1", nativeQuery = true)
	Optional<No2ProductsParty> findByPartyIdANDType(@Param("partyId") Long partyId,
			@Param("productType") String productType);

	@Query(value = "SELECT DISTINCT(pt.*)  FROM no2productpartymapping pm, no2productsparty pt WHERE pt.partyid = pm.partyid AND pm.productid IN (:productList) and pm.active=1 and pt.active=1", nativeQuery = true)
	List<No2ProductsParty> findAllByPartyProduct(@Param("productList") List<Long> productList);

	@Modifying
	@Query(value = "update no2productsparty set active = 0 where partyid =:partyId and producttype =:producttype", nativeQuery = true)
	void updatePartymapping(@Param("partyId") Long partyId, @Param("producttype") String productType);

	@Query(value = "SELECT * from no2productsparty pt WHERE active = 1 and pt.producttype =:productType", nativeQuery = true)
	List<No2ProductsParty> findByProductType(@Param("productType") String productType);

	@Query(value = "SELECT * from no2productsparty pt WHERE pt.producttype =:productType", nativeQuery = true)
	List<No2ProductsParty> findAllByProductType(@Param("productType") String productType);

	@Modifying
	@Query(value = "UPDATE no2productsparty " + "SET primary_party = CASE "
			+ "WHEN partyid = :partyId AND producttype = :productType THEN 1 " + "ELSE 0 " + "END "
			+ "WHERE producttype = :productType", nativeQuery = true)
	void UpdatePrimaryParty(Long partyId, String productType);

	@Query(value = "SELECT * from no2productsparty pt WHERE pt.ref_party =:partyId AND pt.producttype =:productType and pt.active = 1", nativeQuery = true)
	List<No2ProductsParty> findByRefPartyIdANDType(@Param("partyId") Long partyId,
			@Param("productType") String productType);

}
