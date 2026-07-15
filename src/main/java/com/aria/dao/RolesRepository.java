package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.Roles;
import org.springframework.data.jpa.repository.Query;

public interface RolesRepository extends JpaRepository<Roles, Long> {

	Optional<Roles> findByNameIgnoreCase(String name);
	@Query(value = "SELECT u.ids FROM roles u WHERE  " +
			" u.role_name In ( :name )" , nativeQuery = true)
	List<Long> findByIds(List<String> name);

}
