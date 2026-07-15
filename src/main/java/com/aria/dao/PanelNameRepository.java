package com.aria.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.PanelName;

@Repository
public interface PanelNameRepository extends JpaRepository<PanelName, Long> {

	@Query(value = "SELECT tn.* FROM panel_name_master tn where (:panelName is null or (lower(tn.name) LIKE ('%' || lower(:panelName) || '%')))",nativeQuery = true)
//	List<TaskName> findAllTaskName(@Param("taskCategory1") String taskCategory1);
	Page<PanelName> findAllPanelName(@Param("panelName") String panelName, Pageable pageable);

	@Query(value = "SELECT tn.* FROM panel_name_master tn where (:panelName is null or (lower(tn.name) LIKE ('%' || lower(:panelName) || '%')))",nativeQuery = true)
//	List<TaskName> findAllTaskName(@Param("taskCategory1") String taskCategory1);
	List<PanelName> findAllPanelName(@Param("panelName") String panelName);

}
