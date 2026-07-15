package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ITrainerLastTrainingDetailReport;
import com.aria.model.ITrainerTrainingHistory;
import com.aria.model.TrainerDetail;

@Repository
public interface TrainerDetailRepository extends JpaRepository<TrainerDetail, Long> {

	@Query(value = "SELECT COUNT(td.trainer_id) count FROM trainer_detail td "
			+ "LEFT JOIN schedule_training st ON st.id = td.schedule_training_id "
			+ "WHERE (:userId is null or td.trainer_id = :userId) " + "GROUP BY (td.trainer_id)", nativeQuery = true)
	Integer getTrainingCount(@Param("userId") Long userId);

	@Query(value = "SELECT td.id trainerDetailId, st.id lastScheduleTrainingId, "
			+ "st.training_date lastTrainingDate, td.average_score lastTrainingScore FROM trainer_detail td "
			+ "LEFT JOIN schedule_training st ON st.id = td.schedule_training_id "
			+ "WHERE td.id IN (SELECT MAX(td.id) FROM trainer_detail td "
			+ "LEFT JOIN schedule_training st ON st.id = td.schedule_training_id "
			+ "WHERE (:userId is null or td.trainer_id = :userId) " + "GROUP BY (td.trainer_id))", nativeQuery = true)
	ITrainerLastTrainingDetailReport getLastTrainingDetail(@Param("userId") Long userId);

	@Query(value = "SELECT td.id as trainerDetailId, dt.id as departmentId, dt.name as departmentName, dn.id as designationId, dn.name as designationName, sd.id as skillId, sd.name as skillName, "
			+ "st.training_date as trainingDate, td.average_score as averageScore FROM trainer_detail td "
			+ "LEFT JOIN schedule_training st ON st.id = td.schedule_training_id "
			+ "LEFT JOIN skills sd ON sd.id = st.skill_id "
			+ "LEFT JOIN department_details dt ON dt.id = st.department_id "
			+ "LEFT JOIN designation dn ON dn.id = st.designation_id "
			+ "WHERE (td.trainer_id = :userId)", nativeQuery = true)
	List<ITrainerTrainingHistory> getTrainerTrainingHistory(@Param("userId") Long userId);

	@Query(value = "SELECT * FROM trainer_detail td " + "WHERE (td.last_schedule_training_id = :scheduleTrainingId) "
			+ "AND (td.training_feedback_id = :trainerFeedbackId)", nativeQuery = true)
	List<TrainerDetail> findAllByLastTrainingIdANDTrainerFeedbackId(
			@Param("scheduleTrainingId") Long scheduleTrainingId, @Param("trainerFeedbackId") Long trainerFeedbackId);

	@Query(value = "SELECT * FROM trainer_detail td " + "WHERE (td.last_schedule_training_id = :scheduleTrainingId) "
			+ "AND (td.training_feedback_id = :trainerFeedbackId)", nativeQuery = true)
	Optional<TrainerDetail> findByLastTrainingIdANDTrainerFeedbackId(
			@Param("scheduleTrainingId") Long scheduleTrainingId, @Param("trainerFeedbackId") Long trainerFeedbackId);

	@Query(value = "SELECT COUNT(*) FROM trainer_detail td "
			+ "WHERE (td.schedule_training_id = :scheduleTrainingId)", nativeQuery = true)
	int countByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Query(value = "SELECT * FROM trainer_detail td " + "WHERE (td.schedule_training_id = :scheduleTrainingId) "
			+ "AND (td.trainer_id = :userId)", nativeQuery = true)
	Optional<TrainerDetail> findByScheduleTrainingIdUserId(@Param("scheduleTrainingId") Long scheduleTrainingId,
			@Param("userId") Long userId);

}
