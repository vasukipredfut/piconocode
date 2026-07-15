package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aria.model.ModulePrivilegeMapping;

public interface ModulePrivilegeMappingRepository extends JpaRepository<ModulePrivilegeMapping, Long> {

	@Query(value = "SELECT * FROM module_privilege_mapping WHERE role_id = :roleId AND module_id = :moduleId AND submodule_id = :subModuleId AND (:deptId IS NULL OR dept_id = :deptId) AND active = 1", nativeQuery = true)
	Optional<ModulePrivilegeMapping> findByRoleIdAndModuleIdAndSubModuleIdAndDeptId(@Param("roleId") Long roleId,
			@Param("moduleId") Long moduleId, @Param("subModuleId") Long subModuleId, @Param("deptId") Long deptId);

	@Query(value = "SELECT * FROM module_privilege_mapping WHERE (:roleId IS NULL OR role_id = :roleId) AND (:moduleId IS NULL OR module_id = :moduleId) AND (:subModuleId IS NULL OR submodule_id = :subModuleId) AND (:deptId IS NULL OR dept_id = :deptId OR dept_id = -1) AND active = 1", nativeQuery = true)
	List<ModulePrivilegeMapping> findByFilter(@Param("roleId") Long roleId, @Param("moduleId") Long moduleId,
			@Param("subModuleId") Long subModuleId, @Param("deptId") Long deptId);

//	@Query(value = "SELECT * FROM module_privilege_mapping WHERE role_id = :roleId AND (dept_id = :deptId OR dept_id IS NULL)", nativeQuery = true)
//	List<ModulePrivilegeMapping> findByRoleIdAndDeptId(@Param("roleId") Long roleId, @Param("deptId") Long deptId);



	@Query(value = "SELECT * FROM module_privilege_mapping WHERE module_id = :moduleId AND  submodule_id = :subModuleId ", nativeQuery = true)
	List<ModulePrivilegeMapping> findBy(@Param("moduleId") Long moduleId,
											  @Param("subModuleId") Long subModuleId);
}
