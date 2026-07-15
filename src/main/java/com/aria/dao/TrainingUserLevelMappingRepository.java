package com.aria.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TrainingUserLevelMapping;

@Repository
public interface TrainingUserLevelMappingRepository extends JpaRepository<TrainingUserLevelMapping, Long> {

	@Query(value = "SELECT tulm.id FROM training_user_level_mapping tulm WHERE tulm.schedule_training_id = :scheduleTrainingId", nativeQuery = true)
	Set<Long> findAllByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Modifying
	@Query(value = "DELETE FROM TrainingUserLevelMapping tulm " + "WHERE (tulm.id IN :deletedIds)")
	void deleteAllByIdsIn(@Param("deletedIds") Set<Long> deletedIds);
}
