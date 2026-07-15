package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.QtySjid;


@Repository
public interface QtySjidRepository extends JpaRepository<QtySjid, Long>{
	
	@Query(value = "select distinct s.sjid from qtysjid s where s.parent = :parent AND s.gparent = :gparent AND s.ggparent = :ggparent AND s.sjid != 0 ", nativeQuery = true )
	List<Long> findBySjidAndParentAndCreatedBy(@Param("parent")Long parent,@Param("gparent") Long gparent,@Param("ggparent") Long ggparent);

}
