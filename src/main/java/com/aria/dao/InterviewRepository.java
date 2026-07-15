package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Interview;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
	@Query(value = "SELECT distinct status FROM interview", nativeQuery = true)
	List<String> findAllByStatus();


	@Query(value = "SELECT *  FROM interview i WHERE i.aadhar_number =:aadhar ", nativeQuery = true)
	Optional<Interview> findByAadhar(@Param("aadhar") String aadhar);

	@Query(value = "SELECT distinct status FROM interview WHERE status LIKE :status", nativeQuery = true)
	List<String> findAllByStatus(@Param("status") String status);

	@Query(value = "SELECT i FROM Interview i WHERE (:candidateName is null or (lower(i.candidateName) LIKE ('%' || lower(:candidateName) || '%'))) "
			+ "AND (:employeeType is null or lower(i.employeeType) = lower(:employeeType)) "
			+ "AND (:interviewPosition is null or lower(i.interviewPosition) = lower(:interviewPosition)) "
			+ "AND (:adhaar is null or (i.aadharNumber LIKE ('%' || :adhaar || '%')) )"
			+ "AND (:status is null or (i.status LIKE ('%' || :status || '%')) )"
			+ "AND (:place is null or i.branch.branchName = :place) "
			+ "AND (:branchId is null or i.branch.id = :branchId) "
			+ "AND (cast(:dateFrom as timestamp) is null or (i.interviewDate >= :dateFrom)) "
			+ "AND (cast(:dateTo as timestamp) is null or (i.interviewDate <= :dateTo)) ORDER BY i.id DESC")
	Page<Interview> interviewfilterList(@Param("candidateName") String candidateName,
			@Param("employeeType") String employeeType, @Param("interviewPosition") String interviewPosition,
			@Param("branchId") Long branchId, @Param("status") String status,
			@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, @Param("adhaar") String adhaar,
			@Param("place") String place, Pageable pageable);

}
