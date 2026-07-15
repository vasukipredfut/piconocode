package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.SalaryRules;

@Repository
public interface LinkSalaryAllRepository extends JpaRepository<SalaryRules, Long> {


}
