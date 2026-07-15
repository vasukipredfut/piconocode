package com.aria.dao;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.FinClose;

@Repository
public interface FinCloseRepository extends JpaRepository<FinClose, Long>{

	
}
