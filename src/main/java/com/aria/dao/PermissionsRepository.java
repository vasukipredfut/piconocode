package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.Permissions;

@Repository
public interface PermissionsRepository extends JpaRepository<Permissions, Long> {

	List<Permissions> findByRoleId(int id);

}
