package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.OpenEndedQuestion;

@Repository
public interface OpenEndedQuestionRepository extends JpaRepository<OpenEndedQuestion, Long> {

}
