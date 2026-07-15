package com.aria.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TrainingEmployeeCategoryMapping;

@Repository
public interface TrainingEmployeeCategoryMappingRepository
		extends JpaRepository<TrainingEmployeeCategoryMapping, Long> {

	@Query(value = "SELECT tecm.id FROM training_employee_category_mapping tecm WHERE tecm.schedule_training_id = :scheduleTrainingId", nativeQuery = true)
	Set<Long> findAllByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Modifying
	@Query(value = "DELETE FROM TrainingEmployeeCategoryMapping tecm " + "WHERE (tecm.id IN :deletedIds)")
	void deleteAllByIdsIn(@Param("deletedIds") Set<Long> deletedIds);
}
