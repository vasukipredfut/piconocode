package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.DutyUnplanned;

@Repository
public interface DutyUnplannedRepository extends JpaRepository<DutyUnplanned, Long> {

	@Query(value = "SELECT du FROM DutyUnplanned du WHERE (:userId is null or du.user.id = :userId)"
			+ "AND (:departmentId is null or du.departmentDetails.id = :departmentId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (du.dutyFrom >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (du.dutyTo <= :dateTo))")
	Page<DutyUnplanned> dutyUnplannedfilterList(@Param("userId") Long userId, @Param("departmentId") Long deptId,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

}
