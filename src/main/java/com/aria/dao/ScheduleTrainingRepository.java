package com.aria.dao;

import java.util.Date;

import com.aria.dto.ScheduleTrainingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.IScheduleTrainingFilter;
import com.aria.model.ScheduleTraining;

@Repository
public interface ScheduleTrainingRepository extends JpaRepository<ScheduleTraining, Long> {

	@Query(value = "SELECT st.id as id, st.training_type as trainingTypeName, "
			+ "dt.id as departmentId, dt.name as departmentName, dn.id as designationId, dn.name as  designationName, "
			+ "sk.id as skillId, sk.name as skillName, st.total_registered_employees as totalRegisteredEmployees, "
			+ "st.total_seats as totalSeats, st.status as status, " + "st.training_topic as trainingTopic, "
			+ "st.training_date as trainingDate, st.last_registration_date as lastRegistrationDate "
			+ "FROM schedule_training st  "
			+ "LEFT JOIN department_details dt ON dt.id = st.department_id "
			+ "LEFT JOIN designation dn ON dn.id = st.designation_id "
			+ "LEFT JOIN skills sk ON sk.id = st.skill_id "
			+ "WHERE (:branchId is null or st.training_branch_id =:branchId) "
			+ "AND (:trainingType is null or (lower(st.training_type) LIKE ('%' || lower(:trainingType) || '%'))) "
			+ "AND (:departmentId is null or st.department_id =:departmentId) "
			+ "AND (:designationId is null or st.designation_id =:designationId) "
			+ "AND (:trainingTopic is null OR ((lower(sk.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(dn.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(st.training_topic) LIKE ('%' || lower(:trainingTopic) || '%')) )) "
			+ "AND (cast(:dateFrom as timestamp) is null or (st.training_date >=:dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (st.training_date <=:dateTo)) ", nativeQuery = true)
	Page<IScheduleTrainingFilter> getFilterListScheduleTraining(@Param("branchId") Long branchId,
			@Param("trainingType") String trainingType, @Param("departmentId") Long departmentId,
			@Param("designationId") Long designationId, @Param("trainingTopic") String trainingTopic,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);


	@Query(value = "select  st.id as id , st.training_type as trainingType , st.training_topic as trainingTopic, st.last_registration_date as lastRegistrationDate, st.status as status, st.total_Seats as totalSeats, st.remaining_seats as remainingSeats   "
			+"from schedule_training st LEFT JOIN skills sk ON sk.id = st.skill_id LEFT JOIN designation de ON de.id = st.designation_id LEFT JOIN department_details dt ON dt.id = st.department_id LEFT JOIN branch br ON br.id = st.training_branch_id "
			+"LEFT JOIN training_user_registration_mapping turm ON turm.schedule_training_id = st.id LEFT JOIN employee_details ed ON ed.id = turm.trainee_id LEFT JOIN trainer_detail td ON td.schedule_training_id = st.id  "
			+" WHERE (br.id = :branchId) "
			+ "AND (:trainingType is null or (lower(st.training_type) LIKE ('%' || lower(:trainingType) || '%'))) "
			+ "AND (:trainingTopic is null or (((lower(sk.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(de.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(st.training_topic) LIKE ('%' || lower(:trainingTopic) || '%'))))) "
			+ "AND (cast(:dateFrom as timestamp) is null or (st.training_date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (st.training_date <= :dateTo)) "
			+ "GROUP BY st.id ", nativeQuery = true)
	Page<ScheduleTrainingList> getFilterListScheduleTrainingForRegistration(@Param("branchId") Long branchId, @Param("trainingType") String trainingType,
																			@Param("trainingTopic") String trainingTopic, @Param("dateFrom") Date dateFrom,
																			@Param("dateTo") Date dateTo, Pageable pageable);

	@Query(value = "SELECT st.id as id, st.training_type as trainingTypeName, dt.id as departmentId, dt.name as departmentName, dn.id as designationId, dn.name as designationName, "
			+ "sd.id as skillId, sd.name as skillName, st.total_registered_employees as totalRegisteredEmployees, st.training_topic as trainingTopic, st.total_seats as totalSeats, st.status as status, "
			+ "st.training_date trainingDate, st.last_registration_date lastRegistrationDate "
			+ "FROM schedule_training st LEFT JOIN trainer_detail td ON td.schedule_training_id = st.id "
			+ "LEFT JOIN employee_details ed ON ed.id = td.trainer_id "
			+ "LEFT JOIN department_details dt ON dt.id = st.department_id "
			+ "LEFT JOIN designation dn ON dn.id = st.designation_id "
			+ "LEFT JOIN skills sd ON sd.id = st.skill_id "
			+ "WHERE (:branchId is null or st.training_branch_id = :branchId) "
			+ "AND (:trainingType is null or (lower(st.training_type) LIKE ('%' || lower(:trainingType) || '%'))) "
			+ "AND (:departmentId is null or st.department_id = :departmentId) "
			+ "AND (:designationId is null or st.designation_id = :designationId) "
			+ "AND (:trainingTopic is null or ((lower(sd.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(dn.name) LIKE ('%' || lower(:trainingTopic) || '%')) or (lower(st.training_topic) LIKE ('%' || lower(:trainingTopic) || '%')))) "
			+ "AND (cast(:dateFrom as timestamp) is null or (st.training_date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (st.training_date <= :dateTo))", nativeQuery = true)
	Page<IScheduleTrainingFilter> getFilterListScheduleTrainingForEvaluation(
			@Param("branchId") Long branchId, @Param("departmentId") Long departmentId, @Param("designationId") Long designationId, @Param("trainingType") String trainingType,
			@Param("trainingTopic") String trainingTopic, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);
}
