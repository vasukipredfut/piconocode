package com.aria.dao;

import com.aria.model.CostExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CostExpenseRepository extends JpaRepository<CostExpense, Long> {


}
