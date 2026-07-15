package com.aria.dao;

import com.aria.model.Machine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.MachineCluster;

import javax.crypto.Mac;

@Repository
public interface MachineClusterRepository extends JpaRepository<MachineCluster, Long> {
	@Query(value = "SELECT cm FROM MachineCluster cm "
			+ "WHERE (:name is null or (lower(cm.machine.name) LIKE ('%' || lower(:name) || '%'))) "
			+ " AND (:id is null or  cm.machine.id = :id )")
	Page<MachineCluster> machineClusterFilterList(@Param("name") String name, @Param("id") Long id, Pageable pageable);

	@Query(value = "SELECT cm FROM MachineCluster cm "
			+ "WHERE cm.machine.id = :machineId  ")
	MachineCluster getByMachineId(Long machineId);

	@Query(value = "SELECT cm FROM Machine cm "
			+ " WHERE cm.isMainMachine='YES' AND cm.status in ('ACTIVE','active') AND (:name is null or (lower(cm.name) LIKE ('%' || lower(:name) || '%'))) "
			+ " AND (:classificationId is null or  cm.classifyDet.id = :classificationId)"
			+ " AND (:departmentId is null or  cm.departmentDetails.id = :departmentId)"
			+ " AND (:branchId is null or  cm.branch.id = :branchId)")
	Page<Machine> machineWorkFlowFilterList(@Param("name") String name, @Param("classificationId") Long classificationId,@Param("departmentId") Long departmentId ,@Param("branchId") Long branchId,Pageable pageable);

	MachineCluster findByMachine(Machine machine);

}