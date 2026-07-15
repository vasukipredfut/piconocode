package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.QtyPfList;
import com.aria.model.QtyRefid;

@Repository
public interface QtyRefidRepository extends JpaRepository<QtyRefid, Long>{
	@Query(value = "select distinct s.refid as refid , s.pf as pf from qtyrefid s where s.gparent = :parent AND s.ggparent = :gparent AND s.gggparent = :ggparent group by s.refid,s.pf ", nativeQuery = true )
	List <QtyPfList> findByRefIdAndParentAndCreatedBy(@Param("parent") Long parent,@Param("gparent") Long gparent,@Param("ggparent") Long ggparent);

}
