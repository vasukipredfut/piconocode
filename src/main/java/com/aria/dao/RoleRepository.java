package com.aria.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.aria.model.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	 Optional<Role> findByName(String name);
	 
	 Optional<Role> findById(long id);

}