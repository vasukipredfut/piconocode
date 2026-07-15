package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.InterviewScore;

@Repository
public interface InterviewScoreRepository extends JpaRepository<InterviewScore, Long> {

}