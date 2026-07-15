package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.model.DropDownList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TaskCategory;
import com.aria.model.TaskName;
import com.aria.model.TrainingType;

import jakarta.persistence.Tuple;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

	@Query(value = "SELECT tn.* FROM task_category tn where "
			+ " tn.task_category_name IS NOT NULL  "
			+" AND(:taskCategoryName is null or (lower(tn.task_category_name) LIKE ('%' || lower(:taskCategoryName) || '%'))) "
			+ " AND ( :modeOfEntry is null or  (case when lower(:modeOfEntry)='i' then tn.inw_ins_flag='1' else (case when lower(:modeOfEntry)='m' then (tn.inw_ins_flag !='1' and tn.inp_ins_flag!='1') else tn.inp_ins_flag='1' end) end )) "
			, nativeQuery = true)
	List<TaskCategory> findAllTaskCategoryName(@Param("taskCategoryName") String taskCategoryName, @Param("modeOfEntry") String modeOfEntry);

	@Query(value = "SELECT id, task_category_name as name FROM task_category tn where tn.task_category_name IS NOT NULL AND  (:taskCategoryName is null or (lower(tn.task_category_name) LIKE ('%' || lower(:taskCategoryName) || '%')))",nativeQuery = true)
	List<DropDownList> searchAllTaskCategoryName(@Param("taskCategoryName") String taskCategoryName);


	@Query(value = "SELECT * FROM task_category tn where tn.task_category_name =:taskCategoryName LIMIT 1",nativeQuery = true)
	Optional<TaskCategory> findByTaskCategoryName(@Param("taskCategoryName") String taskCategoryName);


}
