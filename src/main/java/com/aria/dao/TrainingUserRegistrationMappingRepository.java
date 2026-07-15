package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.IRegisteredUserListScheduleTraining;
import com.aria.model.TrainingUserRegistrationMapping;

@Repository
public interface TrainingUserRegistrationMappingRepository
		extends JpaRepository<TrainingUserRegistrationMapping, Long> {

	@Query(value = "SELECT turm FROM TrainingUserRegistrationMapping turm "
			+ "WHERE (turm.user.id = :userId) AND (turm.scheduleTraining.id = :scheduleTrainingId)")
	Optional<TrainingUserRegistrationMapping> findByScheduleTrainingIdANDUserId(
			@Param("scheduleTrainingId") Long scheduleTrainingId, @Param("userId") Long userId);

	@Query(value = "SELECT ed.id employeeId, ed.employeename employeename, dt.id departmentId, dt.name departmentName, dn.id designationId, dn.name designationName, turm.schedule_training_id scheduleTrainingId, turm.registration_date registrationDate "
			+ "FROM training_user_registration_mapping turm "
			+ "LEFT JOIN employee_details ed ON ed.id = turm.trainee_id "
			+ "LEFT JOIN department_details dt ON dt.id = ed.dept_id "
			+ "LEFT JOIN designation dn ON dn.id = ed.desi_id "
			+ "WHERE (turm.schedule_training_id = :scheduleTrainingId) "
			+ "AND (turm.registration_status = 'Registered')", nativeQuery = true)
	List<IRegisteredUserListScheduleTraining> findByScheduleTrainingId(
			@Param("scheduleTrainingId") Long scheduleTrainingId);

	@Query(value = "SELECT COUNT(*) turm FROM training_user_registration_mapping turm "
			+ "WHERE (turm.schedule_training_id = :scheduleTrainingId)", nativeQuery = true)
	int countByScheduleTrainingId(@Param("scheduleTrainingId") Long scheduleTrainingId);
	
	@Query(value = "SELECT turm FROM TrainingUserRegistrationMapping turm "
			+ "WHERE (turm.user.id = :userId) AND (turm.scheduleTraining.trainingDate = :date) AND (turm.registrationStatus = :status)")
	Optional<TrainingUserRegistrationMapping> findByUserIdANDDateANDStatus(@Param("userId") Long userId,
			@Param("date") Date date, @Param("status") String status);
	
	@Query(value = "SELECT turm FROM TrainingUserRegistrationMapping turm "
			+ "WHERE (turm.user.id = :userId) AND (turm.scheduleTraining.trainingDate = :date) AND (turm.attendanceStatus = :attandanceStatus)")
	Optional<TrainingUserRegistrationMapping> findByUserIdANDDateANDAttandanceStatus(@Param("userId") Long userId,
			@Param("date") Date date, @Param("attandanceStatus") String attandanceStatus);
}
