package com.aria.dao;

import com.aria.model.UserDependency;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDependencyRepository extends JpaRepository<UserDependency, Long> {
	
	
	@Query(value = "SELECT * FROM employee_dependency ud where userid = :userID", nativeQuery=true)
	List<UserDependency> findAllByUserId(@Param("userID") Long userID);
}
