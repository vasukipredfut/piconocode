package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.TestScore;

@Repository
public interface TestScoreRepository extends JpaRepository<TestScore, Long> {

}