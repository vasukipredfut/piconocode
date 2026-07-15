package com.aria.dao;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TraineeExamScore;

@Repository
public interface TraineeExamScoreRepository extends JpaRepository<TraineeExamScore, Long> {

//	@Query(value = "SELECT * FROM TraineeExamScore tes LEFT JOIN TrainingUserRegistrationMapping turm "
//			+ "WHERE turm.trainingUserRegistrationMapping.user.id = scheduleTrainingId")
//	List<TraineeExamScore> getTraineeExamScoreListByScheduleTrainingId(
//			@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Query(value = "SELECT DISTINCT tes FROM TraineeExamScore tes LEFT JOIN FETCH tes.trainingUserRegistrationMapping turm "
			+ "LEFT JOIN FETCH tes.traineeAnswerMultipleChoiceQuestions tamcq "
			+ "LEFT JOIN FETCH tes.traineeAnswerOpenEndedQuestions taoeq " + "LEFT JOIN FETCH turm.user u "
			+ "LEFT JOIN FETCH tes.trainerDetail td " + "LEFT JOIN FETCH td.trainer tr "
			+ "WHERE (tes.scheduleTrainingId = :scheduleTrainingId) " + "AND (:userId is null OR u.id = :userId)")
	LinkedHashSet<TraineeExamScore> getTraineeExamScoreListByScheduleTrainingId(
			@Param("scheduleTrainingId") Long scheduleTrainingId, @Param("userId") Long userId);

	@Query(value = "SELECT tes FROM TraineeExamScore tes " + "WHERE (tes.scheduleTrainingId = :scheduleTrainingId) "
			+ "AND (tes.evaluationStatus = 'Evaluation Pending')", countQuery = "SELECT COUNT(tes) FROM TraineeExamScore tes "
					+ "WHERE (tes.scheduleTrainingId = :scheduleTrainingId) "
					+ "AND (tes.evaluationStatus = 'Evaluation Pending')")
	List<TraineeExamScore> findAllTrainingExamScoreWithEvaluationPendingStatus(
			@Param("scheduleTrainingId") Long scheduleTrainingId, Pageable pageable);

	@Modifying
	@Query(value = "DELETE FROM TraineeExamScore tes " + "WHERE (tes.trainerDetail.id = :trainerDetailId)")
	void deleteByTrainerDetailId(@Param("trainerDetailId") Long trainerDetailId);

	@Query(value = "SELECT * FROM trainee_exam_score tes WHERE tes.schedule_training_id = :scheduleTrainingId", nativeQuery = true)
	List<TraineeExamScore> findByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Query(value = "SELECT tes.id FROM trainee_exam_score tes WHERE tes.schedule_training_id = :scheduleTrainingId", nativeQuery = true)
	LinkedHashSet<Long> findAllByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Modifying
	@Query(value = "DELETE FROM TraineeExamScore tes " + "WHERE (tes.trainerDetail.id IN :trainerDetailIds)")
	void deleteAllByTrainerDetailIdIn(@Param("trainerDetailIds") Set<Long> trainerDetailIds);
}
