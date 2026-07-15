package com.aria.dao;

import com.aria.model.WelfareEmployee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WelfareEmployeeRepository extends JpaRepository<WelfareEmployee, Long> {
	
}