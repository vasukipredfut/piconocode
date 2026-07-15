package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.aria.dto.CompentencyMatrixList;
import com.aria.dto.SkillMatrixList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ISkillMatrixFilter;
import com.aria.model.SkillMatrix;

@Repository
public interface SkillMatrixRepository extends JpaRepository<SkillMatrix, Long> {

/*	@Query(value = "SELECT sm.id as id, sm.date as date, ed.id as userId, ed.employeename as employeename, br.id as branchId, br.name as branchName, "
			+ "department.id as departmentId, department.name as departmentName, "
			+ "desig.id as designationId, desig.name as designationName, count(smgl.skill_details_id) as skillsCount, "
			+ "sm.updated_date as updatedDate "
			+ "FROM skill_matrix sm LEFT JOIN employee_details ed ON ed.id = sm.user_id "
			+ "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "WHERE (:userId is null or sm.user_id = :userId) " + "AND (:branchId is null or br.id = :branchId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (sm.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (sm.date <= :dateTo)) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM skill_matrix sm LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "GROUP BY ed.id, br.id, department.id, desig.id) "
			+ "GROUP BY (sm.id, ed.id, br.id, department.id, desig.id)", nativeQuery = true)
	List<ISkillMatrixFilter> filterListSkillMatrix(@Param("userId") Long userId, @Param("branchId") Long branchId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);*/


	@Query(value = "select cm.id as id , cm.date as date , de.id as designationId, de.name as designation, dt.id as deptId, dt.name as departmentName, count(*) as skillCount , string_agg (sd.skills,' , ') as skills , sm.updated_date as updatedDate from compentency_matrix cm LEFT JOIN skill_details sd ON sd.competency_id = cm.id LEFT JOIN designation de ON de.id = cm.desgination_id LEFT JOIN department_details dt ON dt.id = cm.dept_id LEFT JOIN skill_matrix sm On sm.designation_id = de.id  "
			+" WHERE sm.department_id = dt.id "
			+" AND (:designationId is null or de.id = :designationId) "
			+ "AND (:userId is null or sm.user_id = :userId) "
			+ "AND (:deptId is null or dt.id = :deptId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (cm.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (cm.date <= :dateTo)) GROUP BY cm.id, de.id, dt.id, sm.updated_date,cm.date ", nativeQuery = true)
	Page<SkillMatrixList> skillMatrixfilterList(@Param("userId") Long userId, @Param("designationId") Long designationId,
													  @Param("deptId") Long deptId,
													  @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);



	@Query(value = "SELECT sm.id as id, sm.date as date, ed.id as userId, ed.employeename as employeename, br.id as branchId, br.name as branchName, "
			+ "department.id as departmentId, department.name as departmentName, "
			+ "desig.id as designationId, desig.name as designationName, count(smgl.skill_details_id) as skillsCount, "
			+ "sm.updated_date as updatedDate "
			+ "FROM skill_matrix sm LEFT JOIN employee_details ed ON ed.id = sm.user_id "
			+ "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "WHERE (:userId is null or sm.user_id = :userId) " + "AND (:branchId is null or br.id = :branchId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (sm.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (sm.date <= :dateTo)) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM skill_matrix sm LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "GROUP BY ed.id, br.id, department.id, desig.id) "
			+ "GROUP BY (sm.id, ed.id, br.id, department.id, desig.id)", nativeQuery = true)
	List<ISkillMatrixFilter> filterListSkillMatrix(@Param("userId") Long userId, @Param("branchId") Long branchId,
												   @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

	@Query(value = "SELECT sm.id as id, sm.date as date, br.id as branchId, br.name as branchName, "
			+ "department.id as departmentId, department.name as departmentName, "
			+ "desig.id as designationId, desig.name as designationName, count(smgl.skill_details_id) as skillsCount, "
			+ "sm.updated_date as updatedDate "
			+ "FROM skill_matrix sm LEFT JOIN employee_details ed ON ed.id = sm.user_id "
			+ "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "WHERE (:userId is null or sm.user_id = :userId) " + "AND (:branchId is null or br.id = :branchId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (sm.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (sm.date <= :dateTo)) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM skill_matrix sm LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "WHERE ((:departmentId is null or department.id = :departmentId) "
			+ "AND (:designationId is null or desig.id = :designationId)) "
			+ "GROUP BY br.id, department.id, desig.id) "
			+ "GROUP BY (sm.id, br.id, department.id, desig.id)", nativeQuery = true)
	List<ISkillMatrixFilter> filterListSkillMatrixAllUser(@Param("userId") Long userId,
			@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
			@Param("designationId") Long designationId, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

	@Query(value = "SELECT DISTINCT sm FROM SkillMatrix sm LEFT JOIN FETCH sm.user u " 
			+ "LEFT JOIN FETCH sm.department dt " + "LEFT JOIN FETCH sm.designation dn "
			+ "LEFT JOIN FETCH sm.skillMatrixGradeLevels " + "WHERE (:branchId is null or u.branch_id = :branchId) "
			+ "AND (:userId is null or u.id = :userId) " + "AND (:departmentId is null or dt.id = :departmentId) "
			+ "AND (:designationId is null or dn.id = :designationId)"
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM SkillMatrix sm " + "LEFT JOIN sm.user u " 
			+ "LEFT JOIN sm.department department " + "LEFT JOIN sm.designation dn "
			+ "WHERE (cast(:dateFilter as timestamp) is null or sm.date <= :dateFilter) "
			+ "GROUP BY (u.id, u.branch_id, department.id, dn.id))")
	List<SkillMatrix> getSkillMatrixList(@Param("userId") Long userId, @Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("designationId") Long designationId,
			@Param("dateFilter") Date dateFilter);

	@Query(value = "SELECT DISTINCT sm FROM SkillMatrix sm LEFT JOIN sm.user u " 
			+ "LEFT JOIN sm.department dt " + "LEFT JOIN sm.designation dn "
			+ "WHERE ((:userId is null or u.id = :userId) " + "AND (:branchId is null or u.branch_id = :branchId) "
			+ "AND (:departmentId is null or dt.id = :departmentId) "
			+ "AND (:designationId is null or dn.id = :designationId))", countQuery = "SELECT COUNT(DISTINCT sm) FROM SkillMatrix sm LEFT JOIN sm.user u "
					+ "LEFT JOIN sm.department dt " + "LEFT JOIN sm.designation dn "
					+ "WHERE ((:userId is null or u.id = :userId) " + "AND (:branchId is null or u.branch_id = :branchId) "
					+ "AND (:departmentId is null or dt.id = :departmentId) "
					+ "AND (:designationId is null or dn.id = :designationId))")
	List<SkillMatrix> findByUserIdANDBranchIdANDDepartmentIdANDDesignationId(@Param("userId") Long userId,
			@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
			@Param("designationId") Long designationId, Pageable pageable);

	@Query(value = "SELECT DISTINCT sm FROM SkillMatrix sm LEFT JOIN FETCH sm.user u " 
			+ "LEFT JOIN FETCH sm.department dt " + "LEFT JOIN FETCH sm.designation dn "
			+ "LEFT JOIN FETCH sm.skillMatrixGradeLevels smgl " + "LEFT JOIN smgl.skills sd "
			+ "WHERE (:branchId is null or u.branch_id = :branchId) "
			+ "AND (:departmentId is null or dt.id = :departmentId) "
			+ "AND (:designationId is null or dn.id = :designationId) " + "AND (:skillId is null or sd.id = :skillId) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM SkillMatrix sm " + "LEFT JOIN sm.user u "
			+ "LEFT JOIN sm.department department " + "LEFT JOIN sm.designation dn "
			+ "GROUP BY (u.id, u.branch_id, department.id, dn.id))")
	List<SkillMatrix> getSkillMatrixListWithSingleSkill(@Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("designationId") Long designationId,
			@Param("skillId") Long skillId);

	@Modifying
	@Query(value = "DELETE FROM SkillMatrix sm " + "WHERE (sm.traineeExamScore.id = :traineeExamScoreId)")
	void deleteByTraineeExamScoreId(@Param("traineeExamScoreId") Long traineeExamScoreId);

	@Query(value = "SELECT sm.id FROM SkillMatrix sm " + "WHERE (sm.traineeExamScore.id = :traineeExamScoreId)")
	List<Long> findByTraineeExamScoreId(@Param("traineeExamScoreId") Long traineeExamScoreId);

	@Query(value = "SELECT sm.id FROM SkillMatrix sm " + "WHERE (sm.traineeExamScore.id IN :traineeExamScoreIds)")
	Set<Long> findAllByTraineeExamScoreIdIn(@Param("traineeExamScoreIds") Set<Long> traineeExamScoreIds);

	@Modifying
	@Query(value = "DELETE FROM SkillMatrix sm " + "WHERE (sm.id IN :skillMatrixIds)")
	void deleteAllByIdIn(@Param("skillMatrixIds") Set<Long> skillMatrixIds);
}
