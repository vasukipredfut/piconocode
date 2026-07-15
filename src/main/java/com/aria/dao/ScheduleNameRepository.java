package com.aria.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ScheduleName;

import java.util.List;

@Repository
public interface ScheduleNameRepository extends JpaRepository<ScheduleName, Long> {

	@Query(value = "SELECT tn.* FROM schedule_name_master tn where (:scheduleName is null or (lower(tn.name) LIKE ('%' || lower(:scheduleName) || '%')))",nativeQuery = true)
	Page<ScheduleName> findAllScheduleName(@Param("scheduleName") String scheduleName, Pageable pageable);

	@Query(value = "SELECT tn.* FROM schedule_name_master tn where (:scheduleName is null or (lower(tn.name) LIKE ('%' || lower(:scheduleName) || '%')))",nativeQuery = true)
	List<ScheduleName> findAllScheduleName(@Param("scheduleName") String scheduleName);

}
