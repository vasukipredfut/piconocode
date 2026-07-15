package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.InterviewerAssignDetails;

@Repository
public interface InterviewerAssignDetailsRepository extends JpaRepository<InterviewerAssignDetails, Long> {

}