package com.aria.dao;

import com.aria.model.Machine;
import com.aria.model.MachineCluster;
import com.aria.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepo extends JpaRepository<Task,Long> {

   @Query(value = "select * from task where task_name=:task limit 1",nativeQuery = true)
    Optional<Task> findByTaskName(@Param("task") String task);


    @Query(value = "select * from task where (:task is null or (lower(task_name) LIKE ('%' || lower(:task) || '%')))",nativeQuery = true)
    List<Task> findAllTaskName(@Param("task") String task);

}
