package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.PrimaryExpenses;
//created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir
@Repository
public interface PrimaryExpensesRepository  extends JpaRepository<PrimaryExpenses, Long>{

	boolean existsByName(String name);
	
	Optional<PrimaryExpenses>findByName(String name);

}
