package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.Htatm;

@Repository
public interface HtatmRepository extends JpaRepository<Htatm, Long> {
	
	@Query(value="SELECT h.name, h.id,h.eord FROM (SELECT DISTINCT ON (name) name, id, eord\r\n"
			+ "FROM htatm ORDER BY name, id) h ORDER BY h.eord asc",nativeQuery = true)
	List<Htatm> findList();

}
