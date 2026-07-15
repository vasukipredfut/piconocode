package com.aria.dao;

import jakarta.persistence.Tuple;

import com.aria.model.MachineCluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.MachineWorkFlow;

@Repository
public interface MachineWorkFlowRepository extends JpaRepository<MachineWorkFlow, Long> {

	@Query(value="select b.* from machine_workflow b "
			+ "inner join "
			+ "(select max(log_id) log_id, machine_id, max(mw.id) id from machine_workflow mw "
			+ " left join machine m on machine_id=m.id"
			+ " left join machine_classification mc on m.classifyid=mc.id  "
			+ " where (:machineName is null or (lower(m.name) LIKE ('%' || lower(:machineName) || '%'))) "
			+ " AND (:machineClassificationName is null or (lower(mc.name) LIKE ('%' || lower(:machineClassificationName) || '%'))) "
			+ " group by machine_id) d "
			+ "on b.machine_id=d.machine_id and b.id=d.id "
			, nativeQuery = true)
	Page<MachineWorkFlow> machineWorkFlowFilterList(@Param("machineName") String machineName, 
			@Param("machineClassificationName") String machineClassificationName,
			Pageable pageable);

	@Query(value="select * from consumables", nativeQuery=true)
	Page<Tuple> getMachineWorkFlowConsumablesDetails(Pageable pageable);

	MachineWorkFlow findByMachineCluster(MachineCluster machineCluster);

	@Query(value="select b.* from machine_workflow b where b.machine_id=:machineId "
			, nativeQuery = true)
	MachineWorkFlow findByMachineId(@Param("machineId") Long machineId);
}