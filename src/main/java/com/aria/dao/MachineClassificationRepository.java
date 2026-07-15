package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Machine;
import com.aria.model.MachineClassification;

import java.util.List;

@Repository
public interface MachineClassificationRepository extends JpaRepository<MachineClassification, Long> {
	@Query(value = "SELECT cm FROM MachineClassification cm WHERE "
			+ " (:name is null or (lower(cm.name) LIKE ('%' || lower(:name) || '%')))")
	Page<MachineClassification> machineClassificationFilterList(
	        @Param("name") String name, Pageable pageable);

	@Query(value = "SELECT cm FROM MachineClassification cm WHERE "
			+ " (:name is null or (lower(cm.name) LIKE ('%' || lower(:name) || '%')))")
	List<MachineClassification> machineClassificationFilterList(
			@Param("name") String name);

}