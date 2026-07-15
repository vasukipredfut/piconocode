package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Biomast;

@Repository
public interface BiomastRepository extends JpaRepository<Biomast, Long> {

	@Query(value = "SELECT * FROM biomast bm WHERE bm.machineip = :sysIp", nativeQuery = true)
	Optional<Biomast> findByMachineIp(@Param("sysIp") String sysIp);
}
