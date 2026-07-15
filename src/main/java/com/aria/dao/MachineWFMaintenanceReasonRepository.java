package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.MachineWFMaintenanceReason;

@Repository
public interface MachineWFMaintenanceReasonRepository extends JpaRepository<MachineWFMaintenanceReason, Long> {
	@Query(value = "SELECT cm FROM MachineWFMaintenanceReason cm ")
//			+ "WHERE (:name is null or (lower(cm.machine.name) LIKE ('%' || lower(:name) || '%'))) "
//			+ " AND (:id is null or(lower(cm.machine.id) LIKE ('%' || lower(:id) || '%')))")
	Page<MachineWFMaintenanceReason> machineWFMaintenanceList(Pageable pageable);

}