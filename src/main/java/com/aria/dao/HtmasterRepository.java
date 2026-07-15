package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.ServiceNameDtoProjection;
import com.aria.dto.ServicenameDto;
import com.aria.model.HTMasters;

@Repository
public interface HtmasterRepository extends JpaRepository<HTMasters, Long> {
	@Query(value="select id,name from htmasters",nativeQuery = true)
	List<ServiceNameDtoProjection> getList();

}
