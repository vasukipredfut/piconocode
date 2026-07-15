package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.DailyUserDependency;

public interface DailyUserDependencyRepository extends JpaRepository<DailyUserDependency,Long>{
	public List<DailyUserDependency> findByUserid(Long id);
}
