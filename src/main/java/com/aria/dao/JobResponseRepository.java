package com.aria.dao;

import com.aria.model.JobResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobResponseRepository extends JpaRepository<JobResponse, Long> {

}
