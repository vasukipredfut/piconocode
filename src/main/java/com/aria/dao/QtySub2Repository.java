package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.QtySub2;

@Repository
public interface QtySub2Repository extends JpaRepository<QtySub2, Long> {
	@Query(value = "select * from qtysub2 where parent = :parent AND gparent = :gparent order by lineid asc", nativeQuery = true )
	List<QtySub2> findByParentAndGparent( @Param("parent") Long parent,@Param("gparent")  Long gparent);

}
