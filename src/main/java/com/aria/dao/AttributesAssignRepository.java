package com.aria.dao;

import java.util.Date;

import jakarta.persistence.Tuple;

import com.aria.model.AttributesAssign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributesAssignRepository extends JpaRepository<AttributesAssign,Long> {
	
	@Query(value = "SELECT cm FROM AttributesAssign cm "
			+ " WHERE "
			+ " (cast(:dateFrom as timestamp) is null or (cm.created_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (cm.created_date <= :dateTo))")
	Page<AttributesAssign> getAllAttributesDetails1(
			@Param("dateFrom") Date dateFrom, @Param("dateTo")  Date dateTo, Pageable pageable);
	
	@Query(value="select att.* from attributes_assignments att left join attributes_steps attsteps on att.id=attsteps.attibutes_assign_id "
			+ "left join task_group tg on attsteps.task_group_id=tg.taskgroup  "
			+ "left join task_name tc on tg.task=tc.id "
			+ "left join task_category tcat on tg.taskcategory=tcat.id "
			+ "left join attributes attfields on attfields.attributes_steps_id=attsteps.id"
			+ " WHERE "
			+ " (cast(:dateFrom as timestamp) is null or (att.created_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (att.created_date <= :dateTo))", nativeQuery=true)
	Page<AttributesAssign> getAllAttributesDetails2(
			@Param("dateFrom") Date dateFrom, @Param("dateTo")  Date dateTo, Pageable pageable);
	
	@Query(value="SELECT * "
			+ "FROM ATTRIBUTES_ASSIGNMENTS ATT "
//			+ "LEFT JOIN ATTRIBUTES_STEPS ATTSTEPS ON ATT.ID = ATTSTEPS.ATTIBUTES_ASSIGN_ID "
//			+ "LEFT JOIN ATTRIBUTES ATTFIELDS ON ATTFIELDS.TASK_CATEGORY_ID = ATTSTEPS.TASK_CATEGORY_ID "
//			+ "AND ATTFIELDS.TASK_NAME_ID = ATTSTEPS.TASK_NAME_ID  "
//			+ "LEFT JOIN TASK_GROUP TG ON ATTSTEPS.TASK_GROUP_ID = TG.TASKGROUP "
//			+ "LEFT JOIN TASK_NAME TC ON TG.TASK = TC.ID "
//			+ "LEFT JOIN TASK_CATEGORY TCAT ON TG.TASKCATEGORY = TCAT.ID "
			+ " WHERE "
//			+ " att.id!=10 "
			+ " (cast(:dateFrom as timestamp) is null or (att.created_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (att.created_date <= :dateTo))", nativeQuery=true)
	Page<AttributesAssign> getAllAttributesDetails(
			@Param("dateFrom") Date dateFrom, @Param("dateTo")  Date dateTo, Pageable pageable);

	@Query(value = "SELECT cm FROM AttributesAssign cm ")
	Page<AttributesAssign> getAllAssignAttributesDetailsAlone(Pageable pageable);

	@Query(value = "select tg.id, tg.taskgroup as taskGroupId, tg.task_group_name taskGroupName,  "
			+ "tn.id as taskNameId, tn.task_name taskName,tc.id as taskCategoryId, tc.task_category_name taskCategoryName  "
			+ "from task_group tg, task_name tn, task_category tc  "
			+ "where tg.task=tn.id and tg.taskcategory=tc.id and  "
			+ "taskgroup not in ( "
			+ "	select distinct(tgid) from proreportdb where kpi >0)", nativeQuery=true)
	Page<Tuple> getMaintenanceDetails(Pageable pageable);
}
