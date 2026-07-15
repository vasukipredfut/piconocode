package com.aria.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.aria.dto.MaintenanceList;
import com.aria.model.TaskName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TaskGroup;

import jakarta.persistence.Tuple;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Long> {

    @Query(value = "SELECT tn FROM TaskGroup tn where "
            + " (:taskGroup is null or (lower(tn.taskGroupName) LIKE ('%' || lower(:taskGroup) || '%')))"
            + " AND (:taskCategoryName is null or (lower(tn.taskCategory.taskCategoryName) LIKE ('%' || lower(:taskCategoryName) || '%')))"
            + " AND (:taskCategoryId is null or (tn.taskCategory.id = :taskCategoryId))"
            + " AND (:taskNameId is null or (tn.taskName.id = :taskNameId))")
    Page<TaskGroup> findAllByTaskGroupName(@Param("taskGroup") String taskGroup,
                                           @Param("taskCategoryId") Long taskCategoryId, @Param("taskCategoryName") String taskCategoryName,
                                           @Param("taskNameId") Long taskNameId, Pageable pageable);


    @Query(value = "SELECT tn FROM TaskGroup tn where tn.taskCategory.id = :taskCategoryId ")
    List<TaskGroup> findAllByTaskCategoryId(@Param("taskCategoryId") Long taskCategoryId);


    @Query(value = "SELECT tn.task FROM task_group tn where tn.taskcategory=:taskCategoryId and taskgroup=:taskGroupId "
            , nativeQuery = true)
    List<Long> findTaskNameByTaskCategoryAndGroup(@Param("taskCategoryId") Long taskCategoryId,
                                                  @Param("taskGroupId") Long taskGroupId);

    @Query(value = "select DISTINCT ON (taskgroup) taskgroup taskGroupId, tg.taskcategory as taskCategoryId,task as taskNameId,task_group_name taskGroupName, "
            + "tn.task_name taskName, astep.id as stepId, astep.step_number as stepNumber, astep.attibutes_assign_id as attributesAssignId "
            + "tc.task_category_name taskCategoryName, kpi.id as kpiNameId, kpi.name as kpiName, pc.id as productId, pc.name as productName "
            + " from attributes_steps  astep,task_group tg, task_name tn, task_category tc, kpiname kpi, products_master as pc "
            + " where astep.kpi_name_id=:id and astep.kpi_name_id=kpi.id and pc.id=astep.product_master_id "
            + "				and astep.task_group_id=tg.taskgroup and tn.id=tg.task and tc.id=tg.taskcategory  ", nativeQuery = true)
    List<Tuple> findAllByTaskGroupName2(@Param("id") Long id);

    @Query(value = "SELECT task_group_Name  FROM task_group tn where tn.id IN (:taskgroupId)", nativeQuery = true)
    Set<String> findAllTaskGroupById(@Param("taskgroupId") List<Long> taskgroupId);

    @Query(value = "SELECT tn.* FROM task_group tn where lower(tn.task_group_name) LIKE ( lower(:taskGroup) || '%') "
            , nativeQuery = true)
    List<TaskGroup> searchByTaskGroup(@Param("taskGroup") String taskGroup);

    @Query(value = "SELECT tn FROM TaskGroup tn where "
            + " (:taskGroup is null or (lower(tn.taskGroupName) LIKE ('%' || lower(:taskGroup) || '%')))"
            + " AND (:taskCategoryName is null or (lower(tn.taskCategory.taskCategoryName) LIKE ('%' || lower(:taskCategoryName) || '%')))"
            + " AND ( :modeOfEntry is null or  (tn.taskCategory.inw_ins_flag !='1' and tn.taskCategory.inp_ins_flag!='1')) "
            + " AND (:taskName is null or (tn.taskName.taskName = :taskName)) ORDER BY tn.id DESC ")
    Page<TaskGroup> findAllByTaskGroupNameForMaintaince(@Param("taskGroup") String taskGroup,
                                                        @Param("taskCategoryName") String taskCategoryName,
                                                        @Param("taskName") String taskName, @Param("modeOfEntry") String modeOfEntry, Pageable pageable);

    @Query(value = "SELECT tn FROM TaskGroup tn where "
            + " (:taskGroup is null or (lower(tn.taskGroupName) LIKE ('%' || lower(:taskGroup) || '%')))"
            + " AND (:taskCategoryName is null or (lower(tn.taskCategory.taskCategoryName) LIKE ('%' || lower(:taskCategoryName) || '%')))"
            + " AND ( :modeOfEntry is null or  (tn.taskCategory.inw_ins_flag ='1' or tn.taskCategory.inp_ins_flag='1')) "
            + " AND (:taskName is null or (lower(tn.taskName.taskName) LIKE ('%' || lower(:taskName) || '%'))) ORDER BY tn.id DESC")
    Page<TaskGroup> findAllByTaskGroupNameForInProcess(@Param("taskGroup") String taskGroup,
                                                       @Param("taskCategoryName") String taskCategoryName,
                                                       @Param("taskName") String taskName, @Param("modeOfEntry") String modeOfEntry, Pageable pageable);


    @Query(value = "select tn.id as taskId, max(tn.task_name) as taskName,   max(tgm.id) as taskGroupId , max(tgm.task_group_name) as taskGroupName, "
            + " tc.id as taskCategoryId , max(tc.task_category_name) as taskCategoryName,  tg.id as taskGroupMapId  FROM  proreportdb p left join task_group tg on tg.id = p.tgid "
            + " left join task_name tn on tn.id = tg.task left join task_category tc on tc.id = tg.taskcategory "
            + " and tc.inw_ins_flag = '0' and tc.inp_ins_flag = '0' left join  task_group_master tgm on tgm.id = tg.taskgroup "
            + " where tg.taskgroup = tgm.id and tn.id=tg.task and tc.id=tg.taskcategory "
            + " and (:taskId is null or tn.id = :taskId ) "
            + " AND (:taskName is null or (lower(tn.task_name) LIKE ('%' || lower(:taskName) || '%'))) "
            + " AND (:taskCategoryName is null or (lower(tc.task_category_name) LIKE ('%' || lower(:taskCategoryName) || '%'))) "
            + " and (:taskCategoryId is null or tc.id = :taskCategoryId )  group by tg.id , tn.id , tc.id "
            + " ORDER BY tg.id DESC", nativeQuery = true)
    Page<MaintenanceList> findAllMaintenanceList(@Param("taskId") Long taskId,
                                                 @Param("taskCategoryId") Long taskCategoryId,
                                                 @Param("taskCategoryName") String taskCategoryName,
                                                 @Param("taskName") String taskName, Pageable pageable);


    @Query(value = " select tn.id  as taskId, tn.task_name as taskName,   tgm.id as taskGroupId , tgm.task_group_name as taskGroupName,  "
            + " tc.id as taskCategoryId , tc.task_category_name as taskCategoryName,  tg.id as taskGroupMapId FROM task_group tg "
            + " left join task_name tn on tn.id = tg.task left join task_category tc on tc.id = tg.taskcategory "
            + " and tc.inw_ins_flag = '0' and tc.inp_ins_flag = '0' left join  task_group_master tgm on tgm.id = tg.taskgroup "
            + " where tg.taskgroup = tgm.id and tn.id=tg.task and tc.id=tg.taskcategory "
            + " and (:taskId is null or tn.id = :taskId ) "
            + " and (:taskCategoryId is null or tc.id = :taskCategoryId ) "
            + " and (:taskGroupMapId is null or tg.id = :taskGroupMapId ) "
            + " limit 1 ", nativeQuery = true)
    Optional<MaintenanceList> findAllMaintenanceDetails(@Param("taskId") Long taskId,
                                                        @Param("taskCategoryId") Long taskCategoryId,
                                                        @Param("taskGroupMapId") Long taskGroupMapId);

    @Query(value = " select *   from task_group tg  where  tg.id not in (select distinct p.tgid  from proreportdb p)  and tg.taskcategory in (select id from task_category tc where  tc.inw_ins_flag ='0' and tc.inp_ins_flag = '0') and tg.taskcategory  >0 and tg.task >0 and tg.task in (select tn.id from task_name tn where tn.task_category_id not in(0) )  group by tg.id order by tg.id desc ", nativeQuery = true)
    List<TaskGroup> findAllMaintenanceDetails1();



    @Query(value = "SELECT * FROM task_group tg where tg.taskgroup =:taskgroup AND tg.taskcategory =:taskcategoryId AND tg.task =:taskId LIMIT 1", nativeQuery = true)
    Optional<TaskGroup> findByTaskTaskGroup(@Param("taskgroup") Long taskgroup, @Param("taskId") Long taskId, @Param("taskcategoryId") Long taskcategoryId);


}
