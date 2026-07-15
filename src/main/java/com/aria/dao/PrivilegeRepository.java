package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.PrivilegeDetails;

public interface PrivilegeRepository extends JpaRepository<PrivilegeDetails, Long> {

}
