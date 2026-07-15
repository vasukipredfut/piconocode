package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.dto.FormulaAttributesList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Attributes;

@Repository
public interface AttributesRepository extends JpaRepository<Attributes,Long> {

	@Query("select a from Attributes a where a.taskNameDet.id=:taskNameId and a.taskCategory.id=:taskCategoryId ")
	Page<Attributes> getAttributesStepsDetails(@Param(value = "taskNameId") Long taskNameId, 
			@Param(value = "taskCategoryId") Long taskCategoryId, Pageable pageable);

	@Query("select a from Attributes a where a.taskNameDet.id in (:taskNameIds) ")
	List<Attributes> getAttributesStepsDetailsForTaskName(@Param(value = "taskNameIds") List<Long> taskNameIds);
	
	@Query("select a from Attributes a where a.id in :ids ")
	Page<Attributes> getAttributesIdsDetails(@Param(value = "ids") List<Long> ids,  Pageable pageable);

	@Query(value = "select * from attributes a where a.task_name_id =:taskNameId and a.task_category_id=:taskCategoryId "
	 +" AND (:attName is null or (lower(a.name) LIKE ('%' || lower(:attName) || '%'))) ", nativeQuery = true)
	List<Attributes> getAttributes(@Param(value = "taskNameId") Long taskNameId,
											   @Param(value = "taskCategoryId") Long taskCategoryId , @Param(value = "attName") String attName);


	@Query(value = "select * from attributes a where a.task_name_id =:taskNameId and a.task_category_id=:taskCategoryId "
			+" AND a.id=:attId ", nativeQuery = true)
	Optional<Attributes> getAttributes(@Param(value = "taskNameId") Long taskNameId,
									   @Param(value = "taskCategoryId") Long taskCategoryId , @Param(value = "attId") Long attId);

	@Query(value = "select a.* from attributes a, task_group tg where tg.id =:tgId and a.task_name_id = tg.task and a.task_category_id=tg.taskcategory "
			+" AND (:attName is null or (lower(a.name) LIKE ('%' || lower(:attName) || '%'))) ", nativeQuery = true)
	List<Attributes> getAttributes(@Param(value = "tgId") Long tgId, @Param(value = "attName") String attName);

	@Query(value = "select * from attributes a where a.task_category_id >0 and a.task_name_id =:taskNameId and a.task_category_id=:taskCategoryId  ", nativeQuery = true)
	List<Attributes> getAllAttributes(@Param(value = "taskNameId") Long taskNameId, @Param(value = "taskCategoryId") Long taskCategoryId );




	@Query(value = "select a.id as attId , a.name as attName   from attributes a where "
			+" (:attName is null or (lower(a.name) LIKE ('%' || lower(:attName) || '%'))) ", nativeQuery = true)
	List<FormulaAttributesList> getAttributes(@Param(value = "attName") String attName);

}
