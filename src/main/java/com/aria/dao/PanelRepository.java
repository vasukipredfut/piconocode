package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.MachineDropDown;
import com.aria.model.Panel;

@Repository
public interface PanelRepository extends JpaRepository<Panel, Long> {

	@Query(value = "select p from Panel p where "
			+ " (:panelName is null or (lower(p.panelName) LIKE ('%' || lower(:panelName) || '%'))) "
			+ " AND  (:machineName is null or (lower(p.machine.name) LIKE ('%' || lower(:machineName) || '%'))) "
			+ " AND  (:ebServiceName is null or (lower(p.eb.serviceName) LIKE ('%' || lower(:ebServiceName) || '%'))) order by p.id desc")
	Page<Panel> findAllWithFilters(Pageable pageable,
			@Param("panelName") String panelName,
			@Param("machineName") String machineName,
			@Param("ebServiceName") String ebServiceName);

	@Query(value = "SELECT cm.id as id, cm.panel_name as name FROM panel cm" +
			" where (:machineId is null or (cm.machine_id = :machineId )) ", nativeQuery = true)
	List<MachineDropDown> getPanelNames(@Param("machineId") Long machineId);

	@Query(value = "select p from Panel p" +
			" where p.eb.id = :ebServiceId ")
	List<Panel> getPanelByEbServiceId(@Param("ebServiceId") Long ebServiceId);

	@Query(value = "select * from Panel p where p.machine_id =:machineId  AND p.machine_On_Off=true", nativeQuery = true)
	List<Panel> getPanelByMachineIdANDONOFFStatus(@Param("machineId") Long machineId);
	

}
