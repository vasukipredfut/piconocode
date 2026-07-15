package com.aria.dao;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TaskName;

@Repository
public interface TaskNameRepository extends JpaRepository<TaskName, Long> {


	
	@Query(value = "SELECT tn.* FROM task_name tn left join task_category tc on tc.id=tn.task_category_id "
			+ " where (:taskName1 is null or (lower(tn.task_name) LIKE ('%' || lower(:taskName1) || '%')))"
			+"  AND (:taskCategoryName1 is null or (lower(tc.task_category_name) LIKE ('%' || lower(:taskCategoryName1) || '%')))"
			+"  AND (:taskCategoryId is null or (tc.id = :taskCategoryId))"
			,nativeQuery = true)
	Page<TaskName> findAllTaskName(@Param("taskName1") String taskName1,
			@Param("taskCategoryName1") String taskCategoryName1, 
			@Param("taskCategoryId") Long taskCategoryId
			, Pageable pageable);

	@Query(value = "SELECT tn.* FROM task_name tn where tn.task_category_id=:taskCategoryId "
			,nativeQuery = true)
	Page<TaskName> findAllTaskNameByCategory(@Param("taskCategoryId") Long taskCategoryId
			, Pageable pageable);

	@Query(value = "SELECT tn.* FROM task_name tn where lower(tn.task_name) LIKE ( lower(:taskName1) || '%') "
			,nativeQuery = true)
	List<TaskName> searchByTaskName(@Param("taskName1") String taskName1);

	@Query(value = "SELECT tn FROM TaskName tn right join TaskCategory c on c.id=tn.taskCategroyDet.id and c.inw_ins_flag='0' and c.inp_ins_flag='0'")
	Page<TaskName> getMaintainanceTaskName(Pageable pageable);

	@Query(value = "SELECT tn FROM TaskName tn where lower(tn.taskCategroyDet.taskCategoryName) like ( lower(:taskCategory) || '%')  and tn.taskCategroyDet.inw_ins_flag='0' and tn.taskCategroyDet.inp_ins_flag='0'")
	Page<TaskName> getMaintainanceTaskNameWithCategory(Pageable pageable,String taskCategory);

	@Query(value = "SELECT tn FROM TaskName tn where lower(tn.taskName) like ( lower(:taskName) || '%')  and tn.taskCategroyDet.inw_ins_flag='0' and tn.taskCategroyDet.inp_ins_flag='0'")
	Page<TaskName> getMaintainanceTaskNameWithTaskName(Pageable pageable,String taskName);

	@Query(value = "SELECT tn FROM TaskName tn where lower(tn.taskName) like ( lower(:taskName) || '%') and  lower(tn.taskCategroyDet.taskCategoryName) like ( lower(:taskCategory) || '%')  and tn.taskCategroyDet.inw_ins_flag='0' and tn.taskCategroyDet.inp_ins_flag='0'")
	Page<TaskName> getMaintainanceTaskNameWithCategoryAndTask(Pageable pageable,String taskCategory,String taskName);


	@Query(value = "SELECT * FROM task_name tn where tn.task_category_id=:taskCategoryId  AND tn.task_name =:taskName LIMIT 1",nativeQuery = true)
	Optional<TaskName> findByTaskName(@Param("taskCategoryId") Long taskCategoryId, @Param("taskName") String taskName);


	@Query(value = "SELECT tn.* FROM task_name tn where tn.task_category_id=:taskCategoryId  AND lower(tn.task_name) LIKE ( lower(:taskName) || '%') ",nativeQuery = true)
	List<TaskName> searchByTaskName(@Param("taskCategoryId") Long taskCategoryId, @Param("taskName") String taskName);



	@Query(value = "select tn.* from task_name tn left join task_group tg on tg.task = tn.id and tg.taskcategory = tn.task_category_id  "
			+ " where  tg.taskcategory =:taskCategoryId "
			+ " AND tg.taskgroup =:taskGroupId  "
			+ " AND (:taskName is null or (lower(tn.task_name) LIKE ( lower(:taskName) || '%') ) )",nativeQuery = true)
	List<TaskName> findByTaskList(@Param("taskCategoryId") Long taskCategoryId, @Param("taskGroupId") Long taskGroupId, @Param("taskName") String taskName);

	@Query(value = "SELECT tn.* FROM task_name tn left join task_category tc on tc.id=tn.task_category_id "
			+ " where (:taskName1 is null or (lower(tn.task_name) LIKE ('%' || lower(:taskName1) || '%')))"
			+"  AND (:taskCategoryName1 is null or (lower(tc.task_category_name) LIKE ('%' || lower(:taskCategoryName1) || '%')))"
			+"  AND (:taskCategoryId is null or (tc.id = :taskCategoryId))"
			,nativeQuery = true)
	List<TaskName> findAllTaskName(@Param("taskName1") String taskName1,
								   @Param("taskCategoryName1") String taskCategoryName1,
								   @Param("taskCategoryId") Long taskCategoryId);


}
