package com.aria.dao;

import com.aria.dto.PMScheduleList;
import com.aria.model.PMScheduleDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PMScheduleDetailsRepo extends JpaRepository<PMScheduleDetails, Long> {


    @Query(value = "SELECT * FROM pmscheduledetails pd where pd.pm_schedule_id = :pmscheduleId and pd.active_flag = 1", nativeQuery = true)
    Optional<PMScheduleDetails> findByPmscheduleId(@Param("pmscheduleId") Long pmscheduleId);





    @Query(value = " SELECT cm.id as id , cm.name as scheduleName , cm.machineid as machineId ,  m.name as machineName ,  " +
            " cm.panelid as panelId , p.panel_name as panelName , cm.taskcategory as taskCategoryId , tc.task_category_name as taskCategoryName ,  " +
            " cm.task as taskGroupId, tg.task_group_name as taskGroupName , cm.status as status , cm.schedule_date as scheduleDate " +
            " FROM pmscheduledetails cm  " +
            " left join task_category tc on tc.id = cm.taskcategory  " +
            " left join task_group_master tg on tg.id = cm.task  " +
            " left join machine m on m.id = cm.machineid  " +
            " left join panel p on p.id = cm.panelid  " +
            " WHERE  " +
            " (:scheduleName is null or (lower(cm.name) LIKE ( lower(:scheduleName) || '%')))  " +
            " AND (:status is null or (lower(cm.status) LIKE ( lower(:status) || '%')))  " +
            " AND (:taskCategory is null or (lower(tc.task_category_name) LIKE ( lower(:taskCategory) || '%')))  " +
            " AND (:taskGroup is null or (lower(tg.task_group_name) LIKE ( lower(:taskGroup) || '%')))  " +
            " AND (:machineName is null or (lower(m.name) LIKE ( lower(:machineName) || '%')))  " +
            " AND (:machineId is null or (cm.machineid =:machineId))   " +
            " AND (cast(:dateFrom as date) is null or (date(cm.schedule_date) >= date(cast(:dateFrom as date))))  " +
            " AND (cast(:dateTo as date) is null or (date(cm.schedule_date) <= date(cast(:dateTo as date)))) order by cm.id desc ", nativeQuery = true)
    Page<PMScheduleList> getAllPMScheduleDetailsList(@Param("taskGroup") String taskGroup,
                                              @Param("scheduleName") String scheduleName,
                                              @Param("taskCategory") String taskCategory,
                                              @Param("machineName") String machineName,
                                              @Param("machineId") Long machineId,
                                              @Param("dateFrom") String dateFrom,
                                              @Param("dateTo") String dateTo,
                                              @Param("status") String status,
                                              Pageable pageable);

}
