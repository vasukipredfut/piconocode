package com.aria.dao;

import java.util.Date;
import java.util.Optional;

import com.aria.model.ScoreAssessmentList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ScoreAssessment;

@Repository
public interface ScoreAssessmentRepository extends JpaRepository<ScoreAssessment, Long> {



    /*@Query(value = "SELECT sa.id as scoreId , iv.candidate_name as candidateName,  iv.interview_date as interviewDate, "
    		+ " iv.phone_number as phoneNumber, sa.industrial_score as industrialScore, sa.food_saftey_score as foodSafteyScore, sa.interview_score as interviewScore ,"
    		+ " sa.overall_score as overallScore FROM score_assessment sa LEFT JOIN interviewer_assign ia"
    		+ " ON ia.id = sa.interview_assign_id LEFT JOIN interview  iv ON iv.id = ia.interview_id"
            + " WHERE "
            + " sa.interview_assign_id is not null "
            + " AND ia.examiner_id is not null and ia.interviewer_id is not null and "
            + " (:candidateName is null or (lower(iv.candidate_name) LIKE ('%' || lower(:candidateName) || '%')) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (iv.interview_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (iv.interview_date <= :dateTo)) ORDER BY sa.id DESC", nativeQuery = true)
    Page<ScoreAssessmentList> scoreAssessmentFilterList(@Param("candidateName") String candidateName,
														@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
														Pageable pageable);*/
	@Query(value = "SELECT sa.id as scoreId , iv.candidate_name as candidateName,  iv.interview_date as interviewDate, "
    		+ " iv.phone_number as phoneNumber, sa.industrial_score as industrialScore, sa.food_saftey_score as foodSafteyScore, sa.interview_score as interviewScore ,"
    		+ " sa.overall_score as overallScore FROM score_assessment sa "
    		+ " LEFT JOIN interview  iv ON iv.id = sa.interview_id"
            + " WHERE "
            + " (:candidateName is null or (lower(iv.candidate_name) LIKE ('%' || lower(:candidateName) || '%')) ) "
            + " AND (cast(:dateFrom as timestamp) is null or (iv.interview_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (iv.interview_date <= :dateTo)) ORDER BY sa.id DESC", nativeQuery = true)
    Page<ScoreAssessmentList> scoreAssessmentFilterList(@Param("candidateName") String candidateName,
														@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
														Pageable pageable);

    @Query(value = "select sa from ScoreAssessment sa where sa.interview.id=:id")
    Optional<ScoreAssessment> findByInterviewId(@Param("id") Long id);


} 
