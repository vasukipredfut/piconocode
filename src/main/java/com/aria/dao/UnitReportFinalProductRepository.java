package com.aria.dao;

import com.aria.dto.UnitReportMapList;
import com.aria.model.UnitReportFinalProduct;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitReportFinalProductRepository extends JpaRepository<UnitReportFinalProduct,Long> {

	@Query(value = " SELECT u.refid as productId1,  u.name as productName1  FROM urepfin u where u.sjid = :sjid ", nativeQuery = true)
	 List<UnitReportMapList> findBySjid(Long sjid);

}
