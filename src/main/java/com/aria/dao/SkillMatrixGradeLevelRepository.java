package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ILowSkillLevelUserCount;
import com.aria.model.SkillMatrixGradeLevel;

@Repository
public interface SkillMatrixGradeLevelRepository extends JpaRepository<SkillMatrixGradeLevel, Long> {

	@Query(value = "SELECT sd.id skillId, COUNT(sgl.level_number) userCount FROM skill_matrix_grade_level smgl "
			+ "LEFT JOIN skill_matrix sm ON sm.id = smgl.skill_matrix_id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details dt ON dt.id = ed.dept_id "
			+ "LEFT JOIN designation dn ON dn.id = ed.desi_id "
			+ "LEFT JOIN skill_details sd ON sd.id = smgl.skill_details_id "
			+ "LEFT JOIN skill_grade_level sgl ON sgl.id = smgl.skill_grade_level_id "
			+ "WHERE (:branchId is null or br.id = :branchId) "
			+ "AND (:departmentId is null or dt.id = :departmentId) "
			+ "AND (:designationId is null or dn.id = :designationId)"
			+ "AND (sgl.level_number = 2 OR sgl.level_number = 3 OR sgl.level_number = 4) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM skill_matrix sm LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "WHERE ((:departmentId is null or department.id = :departmentId) "
			+ "AND (:designationId is null or desig.id = :designationId)"
			+ "AND (cast(:dateTo as timestamp) is null or (sm.date <= :dateTo))) "
			+ "GROUP BY ed.id, br.id, department.id, desig.id) "
			+ "GROUP BY (br.id, dt.id, dn.id, sd.id)", nativeQuery = true)
	List<ILowSkillLevelUserCount> getLowSkillLevelUserCount(@Param("branchId") Long branchId,
			@Param("departmentId") Long departmentId, @Param("designationId") Long designationId,
			@Param("dateTo") Date dateTo);

	@Query(value = "SELECT sm.user_id userId FROM skill_matrix_grade_level smgl "
			+ "LEFT JOIN skill_matrix sm ON sm.id = smgl.skill_matrix_id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details dt ON dt.id = ed.dept_id "
			+ "LEFT JOIN designation dn ON dn.id = ed.desi_id "
			+ "LEFT JOIN skill_details sd ON sd.id = smgl.skill_details_id "
			+ "LEFT JOIN skill_grade_level sgl ON sgl.id = smgl.skill_grade_level_id " + "WHERE (br.id = :branchId) "
			+ "AND (dt.id = :departmentId) " + "AND (dn.id = :designationId) " + "AND (sd.id = :skillId) "
			+ "AND (sgl.level_number = 2 OR sgl.level_number = 3 OR sgl.level_number = 4) "
			+ "AND sm.id IN (SELECT MAX(sm.id) FROM skill_matrix sm LEFT JOIN skill_matrix_grade_level smgl ON smgl.skill_matrix_id = sm.id "
			+ "LEFT JOIN employee_details ed ON ed.id = sm.user_id " + "LEFT JOIN branch br ON br.id = ed.branch_id "
			+ "LEFT JOIN department_details department ON department.id = sm.department_id "
			+ "LEFT JOIN designation desig ON desig.id = sm.designation_id "
			+ "WHERE ((br.id = :branchId) AND (department.id = :departmentId) " + "AND (desig.id = :designationId)) "
			+ "GROUP BY ed.id, br.id, department.id, desig.id)", nativeQuery = true)
	List<Long> getLowSkillLevelUserIdList(@Param("branchId") Long branchId, @Param("departmentId") Long departmentId,
			@Param("designationId") Long designationId, @Param("skillId") Long skillId);

	@Modifying
	@Query(value = "DELETE FROM SkillMatrixGradeLevel smgl " + "WHERE (smgl.skillMatrixId IN :skillMatrixIds)")
	void deleteAllBySkillMatrixIdIn(@Param("skillMatrixIds") Set<Long> skillMatrixIds);

}
