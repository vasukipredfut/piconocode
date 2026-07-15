package com.aria.dao;

import com.aria.dto.PMScheduleList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.PMSchedule;

import java.util.Date;
import java.util.List;

@Repository
public interface PMScheduleRepository extends JpaRepository<PMSchedule, Long> {

	@Query(value = "SELECT cm FROM PMSchedule cm WHERE "
			+ " (:scheduleName is null or (lower(cm.name) LIKE ( lower(:scheduleName) || '%'))) "
			+ " AND (:taskCategory is null or (lower(cm.taskCategory.taskCategoryName) LIKE ( lower(:taskCategory) || '%'))) "
			+ " AND (:taskGroup is null or (lower(cm.taskGroup.taskGroupName) LIKE ( lower(:taskGroup) || '%'))) "
			+ " AND (:machineName is null or (lower(cm.machine.name) LIKE ( lower(:machineName) || '%'))) "
//			+ " AND (:machineId is null or (cm.machine.id LIKE ('%' || :machineId || '%'))) " 
			+ " AND (:machineId is null or cm.machine.id=:machineId) "
			+ " AND (cast(:dateFrom as timestamp) is null or (date(cm.lastCompletedDate) >= date(cast(:dateFrom as timestamp)))) "
			+ " AND (cast(:dateTo as timestamp) is null or (date(cm.lastCompletedDate) <= date(cast(:dateTo as timestamp)))) order by cm.id desc ")
	Page<PMSchedule> getAllPMScheduleDetails(@Param("taskGroup") String taskGroup,@Param("scheduleName") String scheduleName,@Param("taskCategory") String taskCategory,
			@Param("machineName") String machineName, @Param("machineId") Long machineId,
			@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
			Pageable pageable);

	@Query(value = "select cm FROM PMSchedule cm WHERE cm.machine.id in (:machineIds) ")
	List<PMSchedule> findByMachineIds(List<Long> machineIds);




	@Query(value = " SELECT cm.id as id , cm.name as scheduleName , cm.machineid as machineId ,  m.name as machineName ,  " +
			" cm.panelid as panelId , p.panel_name as panelName  , cm.taskcategory as taskCategoryId , tc.task_category_name as taskCategoryName ,  " +
			" cm.task as taskGroupId, tg.task_group_name as taskGroupName , cm.last_completed_date as lastCompleteDate  " +
			" FROM pmschedule cm  " +
			" left join task_category tc on tc.id = cm.taskcategory  " +
			" left join task_group_master tg on tg.id = cm.task  " +
			" left join machine m on m.id = cm.machineid  " +
			" left join panel p on p.id = cm.panelid  " +
			" WHERE  " +
			" (:scheduleName is null or (lower(cm.name) LIKE ( lower(:scheduleName) || '%')))  " +
			" AND (:taskCategory is null or (lower(tc.task_category_name) LIKE ( lower(:taskCategory) || '%')))  " +
			" AND (:taskGroup is null or (lower(tg.task_group_name) LIKE ( lower(:taskGroup) || '%')))  " +
			" AND (:machineName is null or (lower(m.name) LIKE ( lower(:machineName) || '%')))  " +
			" AND (:machineId is null or (cm.machineid =:machineId ))   " +
			" AND (cast(:dateFrom as timestamp) is null or (date(cm.last_completed_date) >= date(cast(:dateFrom as timestamp))))  " +
			" AND (cast(:dateTo as timestamp) is null or (date(cm.last_completed_date) <= date(cast(:dateTo as timestamp)))) order by cm.id desc ", nativeQuery = true)
	Page<PMScheduleList> getAllPMScheduleList(@Param("taskGroup") String taskGroup,
												 @Param("scheduleName") String scheduleName,
												 @Param("taskCategory") String taskCategory,
												 @Param("machineName") String machineName,
												 @Param("machineId") Long machineId,
												 @Param("dateFrom") String dateFrom,
												 @Param("dateTo") String dateTo,
												 Pageable pageable);



	@Query(value = "select * FROM pmschedule cm WHERE cm.schedule_date =:scheduleDate " ,nativeQuery = true )
	List<PMSchedule> findByScheduleDate(@Param("scheduleDate") Date scheduleDate);



}
