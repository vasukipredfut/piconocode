package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.HTMSummary;

@Repository
public interface HtmsummaryRepository extends JpaRepository<HTMSummary, Long> {

}
