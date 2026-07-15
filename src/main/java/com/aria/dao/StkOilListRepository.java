package com.aria.dao;

import com.aria.model.StkOilList;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StkOilListRepository extends JpaRepository<StkOilList, Long> {

	 @Query(value ="select Distinct s.refid from stkoillist s where s.parentid = :productId1 ",nativeQuery = true)
	List<Long> findByRefid(Long productId1);


}
