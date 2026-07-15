package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.CompentencyMatrixList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.CompentencyMatrix;

@Repository
public interface CompentencyMatrixRepository extends JpaRepository<CompentencyMatrix, Long> {

	@Query(value = "select   cm.id as id , cm.date as date , de.id as designationId, de.name as designation, dt.id as deptId, dt.name as departmentName, count(*) as skillCount , string_agg (sd.skills,' , ') as skills from compentency_matrix cm LEFT JOIN skill_details sd ON sd.competency_id = cm.id LEFT JOIN designation de ON de.id = cm.desgination_id LEFT JOIN department_details dt ON dt.id = cm.dept_id  " +
			" WHERE (:designationId is null or de.id = :designationId)"
			+ "AND (:deptId is null or dt.id = :deptId)"
			+ "AND (cast(:dateFrom as timestamp) is null or (cm.date >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (cm.date <= :dateTo)) GROUP BY  cm.id, de.id, dt.id , cm.date", nativeQuery = true)
	Page<CompentencyMatrixList> compentencyMatrixfilterList(@Param("designationId") Long designationId,
															@Param("deptId") Long deptId,
															@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);


	@Query(value = "select * from compentency_matrix cm where cm.desgination_id =:designationId AND cm.dept_id =:deptId LIMIT 1", nativeQuery = true)
	List<CompentencyMatrix> compentencyMatrixFilter(@Param("designationId") Long designationId, @Param("deptId") Long deptId);

}