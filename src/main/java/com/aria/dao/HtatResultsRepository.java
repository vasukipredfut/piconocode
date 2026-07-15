package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.HtatResults;

@Repository
public interface HtatResultsRepository extends JpaRepository<HtatResults, Long> {

}
