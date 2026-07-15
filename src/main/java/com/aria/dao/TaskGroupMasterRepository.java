package com.aria.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TaskGroupMaster;

import java.util.Optional;

@Repository
public interface TaskGroupMasterRepository extends JpaRepository<TaskGroupMaster, Long> {

	@Query(value = "SELECT * FROM task_group_master tn where "
			+ " (:taskGroupName is null or (lower(tn.task_group_name) LIKE ('%' || lower(:taskGroupName) || '%')))", nativeQuery = true)
	Page<TaskGroupMaster> findAllByTaskGroupMasterName(@Param("taskGroupName") String taskGroupName, Pageable pageable);


	@Query(value = "SELECT * FROM task_group_master tn where tn.task_group_name =:taskGroupName LIMIT 1", nativeQuery = true)
	Optional<TaskGroupMaster> findByTaskGroupMasterName(@Param("taskGroupName") String taskGroupName);

}
