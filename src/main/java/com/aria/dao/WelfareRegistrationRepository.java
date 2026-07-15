package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.WelfareRegistration;

@Repository
public interface WelfareRegistrationRepository extends JpaRepository<WelfareRegistration, Long> {
	
}