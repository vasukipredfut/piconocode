package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesProductFilmPricesEntity;

@Repository
public interface SalesProductFilmPricesRepository extends JpaRepository<SalesProductFilmPricesEntity, Long> {

	SalesProductFilmPricesEntity findBySalesProductIdAndSalesLocationIdAndSalesProductGroupId(Long salesProductId,
			Long salesLocationId, Long salesProductGroupId);

	List<SalesProductFilmPricesEntity> findBySalesLocationIdInAndSalesProductGroupId(List<Long> salesLocationId,
			Long salesProductGroupId);

}
