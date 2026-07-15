package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.RSubHead;

@Repository
public interface RSubHeadRepository extends JpaRepository<RSubHead, Long> {
	 @Query(value = " SELECT  *  FROM rsubhead r where r.rid = :fgId ", nativeQuery = true)
	List<RSubHead> findAllSubName(@Param("fgId") Long fgId);

}
