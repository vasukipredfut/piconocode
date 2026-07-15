package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Specification_parameter;

@Repository
public interface SpecificationParameterRepository extends JpaRepository<Specification_parameter, Long> {

	 @Query(value = "SELECT *  FROM specification_parameter s WHERE s.specification_type =:spectype and s.active =1", nativeQuery = true)
	    List<Specification_parameter> findAllActive(@Param("spectype") Long spectype);

	
}
