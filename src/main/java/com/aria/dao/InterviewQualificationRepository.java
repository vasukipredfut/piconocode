package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.InterviewQualification;

@Repository
public interface InterviewQualificationRepository extends JpaRepository<InterviewQualification, Long> {
	
@Query(value = "SELECT * FROM interview_qualification WHERE interview_id =:interviewId", nativeQuery = true)
List<InterviewQualification> findAllByInterviewId(@Param("interviewId") Long interviewId);

@Query(value = "SELECT * FROM interview_qualification WHERE interview_id =:interviewId", nativeQuery = true)
Optional<InterviewQualification> findByInterviewId(@Param("interviewId") Long interviewId);

}