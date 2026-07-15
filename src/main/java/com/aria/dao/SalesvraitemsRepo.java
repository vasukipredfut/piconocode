package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aria.model.Salesvraitems;

@Repository
public interface SalesvraitemsRepo extends JpaRepository<Salesvraitems, Long> {
	
	public Optional<Salesvraitems> findByTrnno(String trnno);

}
