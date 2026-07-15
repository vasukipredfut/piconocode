package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.UnitReportProductsList;
import com.aria.model.ReptGrp;


@Repository
public interface ReptGrpRepository extends JpaRepository<ReptGrp, Long> {
	 @Query(value = " SELECT  *  FROM reptgrp r where r.rid = :rId AND r.rsid= :rsId ", nativeQuery = true)
	List<ReptGrp> findByRnameAndRsubhead(@Param("rId") Long rId,@Param("rsId") Long rsId);
	 
	 @Query(value = " SELECT  r.refid as productId ,pm.name as productName ,pm.unit as unit,r.dlot as lot FROM reptgrp r left join products_master pm on pm.id = r.refid where r.rid = :rId AND r.rsid= :rsId ", nativeQuery = true)
	List<UnitReportProductsList> findByRnameAndRsubheadName(@Param("rId") Long rId,@Param("rsId") Long rsId);

}
