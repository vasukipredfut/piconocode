package com.aria.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TrainerFeedbackScore;

@Repository
public interface TrainerFeedbackScoreRepository extends JpaRepository<TrainerFeedbackScore, Long> {

	@Query(value = "SELECT * FROM trainer_feedback_score tfs "
			+ "LEFT JOIN trainer_detail td ON td.id = tfs.trainer_id "
			+ "LEFT JOIN employee_details edtr ON edtr.id = td.trainer_id "
			+ "LEFT JOIN training_user_registration_mapping turm ON turm.id = tfs.trainee_id "
			+ "LEFT JOIN employee_details edte ON edte.id = turm.trainee_id "
			+ "WHERE td.trainer_id IN (:trainerIdsList)" + "AND (edte.id = :userId)", nativeQuery = true)
	List<TrainerFeedbackScore> getTrainerFeedbackScoreListByTraineeIdtrainerDetailIn(
			@Param("trainerIdsList") List<Long> trainerIdsList, @Param("userId") Long userId);

	@Query(value = "SELECT * FROM trainer_feedback_score tfs "
			+ "LEFT JOIN trainer_detail td ON td.id = tfs.trainer_id "
			+ "LEFT JOIN employee_details edtr ON edtr.id = td.trainer_id "
			+ "LEFT JOIN training_user_registration_mapping turm ON turm.id = tfs.trainee_id "
			+ "LEFT JOIN employee_details edte ON edte.id = turm.trainee_id "
			+ "WHERE turm.id = :traineeId ", nativeQuery = true)
	List<TrainerFeedbackScore> findByTraineeId(@Param("traineeId") Long traineeId);

	@Query(value = "SELECT COUNT(*) FROM trainer_feedback_score tfs "
			+ "WHERE (tfs.schedule_training_id = :scheduleTrainingId)", nativeQuery = true)
	int countByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Modifying
	@Query(value = "DELETE FROM TrainerFeedbackScore tfs " + "WHERE (tfs.trainerDetail.id = :trainerDetailId)")
	void deleteByTrainerDetailId(@Param("trainerDetailId") Long trainerDetailId);

	@Modifying
	@Query(value = "DELETE FROM TrainerFeedbackScore tfs " + "WHERE (tfs.trainerDetail.id IN :trainerDetailIds)")
	void deleteAllByTrainerDetailIdIn(@Param("trainerDetailIds") Set<Long> trainerDetailIds);
}
