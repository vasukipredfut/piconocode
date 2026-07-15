package com.aria.dao;

import com.aria.model.Kpiname;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface KpiNameRepository extends JpaRepository<Kpiname, Long> {

	@Query(value = "SELECT id, name FROM kpiname where type=:type", nativeQuery = true)
	List<Tuple> findAlls(@Param("type") String type);

	@Query(value = "SELECT id, name FROM kpiname where type=:type and proId=:id", nativeQuery = true)
	Optional<Tuple> findByTypeAndId(@Param("type") String type, @Param("id") Long id);

	@Query(value = "SELECT k.* FROM kpiname k where lower(k.type)=lower(:type)", nativeQuery = true)
	Page<Kpiname> findAllKpiDetails(@Param("type") String type, Pageable pageable);

	@Query(value = "SELECT * FROM Kpiname k "
			+ " WHERE  (:modeOfEntry is null or lower(k.type)=lower(:modeOfEntry) ) "
			+ "AND (:kpiName is null or (lower(k.name) LIKE ('%' || lower(:kpiName) || '%')) ) ", nativeQuery = true)
	Page<Kpiname> getAllKpiName(@Param("kpiName") String kpiName, @Param("modeOfEntry") String modeOfEntry, Pageable pageable);


	@Query(value = "SELECT formno FROM kpiname where name=:name", nativeQuery = true)
	String findByISO(@Param("name") String name);


}
