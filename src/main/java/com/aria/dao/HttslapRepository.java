package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.ServiceNameDtoProjection;
import com.aria.model.Httslap;

@Repository
public interface HttslapRepository extends JpaRepository<Httslap, Long> {
	
	@Query(value="select id,name from httslap",nativeQuery = true)
	List<ServiceNameDtoProjection> gettimeslap();

}
