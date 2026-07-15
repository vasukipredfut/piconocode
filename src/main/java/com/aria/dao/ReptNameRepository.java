package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.QtyMain;
import com.aria.model.ReptName;

@Repository
public interface ReptNameRepository extends JpaRepository<ReptName, Long> {

	 @Query(value = " SELECT  *  FROM reptname r where r.activeflag = 1", nativeQuery = true)
	List<ReptName> findfgname();

}
