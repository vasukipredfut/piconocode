package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Generator;
import com.aria.model.MachineDropDown;

@Repository
public interface GeneratorRepository extends JpaRepository<Generator, Long> {

	@Query(value = "SELECT du FROM Generator du WHERE "
			+ " (:name is null or (lower(du.name) LIKE ('%' || lower(:name) || '%'))) "
			+ "AND (:status is null or (lower(du.status) LIKE ('%' || lower(:status) || '%'))) "
			+ "AND (:machineId is null or (lower(du.machineId) LIKE ('%' || lower(:machineId) || '%'))) "
			+ "AND (cast(:dateFrom as timestamp) is null or (du.created_date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (du.created_date <= :dateTo)) ORDER BY du.id DESC")
	Page<Generator> findAllGenDetails(@Param("name") String name, @Param("status") String status,@Param("machineId") String machineId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

	@Query(value = "SELECT cm.id as id, cm.name as name FROM generator cm", nativeQuery = true)
	List<MachineDropDown> getGeneratorNames();

}
