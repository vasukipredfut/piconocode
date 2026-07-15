package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.CExpenses;

@Repository
public interface CExpensesRepository extends JpaRepository<CExpenses, Long> {

}
