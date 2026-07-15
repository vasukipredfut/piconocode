package com.aria.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.RoleRepository;
import com.aria.model.Role;

@Service
public class RoleService {

	@Autowired
	RoleRepository roleRepository;

	public ResponseEntity<?> getAllRoles() {
		Iterable<Role> roles = roleRepository.findAll();
		return new ResponseEntity<List<Role>>(
				StreamSupport.stream(roles.spliterator(), false).collect(Collectors.toList()), HttpStatus.OK);
	}

	public void save(String role) {
		Role role1 = new Role();
		role1.setName(role);
		roleRepository.save(role1);
	}

}
