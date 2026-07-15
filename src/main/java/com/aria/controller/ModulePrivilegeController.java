package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.ModulePrivilegeMappingDto;
import com.aria.dto.ModulesDto;
import com.aria.dto.RolesDto;
import com.aria.dto.SubModulesDto;
import com.aria.model.User;
import com.aria.service.ModulePrivilegeService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ModulePrivilegeController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	private ModulePrivilegeService modulePrivilegeService;

	/* Module Privilege Mapping */
	@PostMapping("/saveModulePrivilegeMapping")
	public ResponseEntity<?> saveModulePrivilegeMapping(@RequestBody List<ModulePrivilegeMappingDto> dtos,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.saveModulePrivilegeMapping(dtos);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getModulePrivilegesByFilter")
	public ResponseEntity<?> getModulePrivilegesByFilter(@RequestParam(value = "roleId", required = false) Long roleId,
			@RequestParam(value = "moduleId", required = false) Long moduleId,
			@RequestParam(value = "subModuleId", required = false) Long subModuleId,
			@RequestParam(value = "deptId", required = false) Long deptId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.getModulePrivilegesByFilter(roleId, moduleId, subModuleId, deptId);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getModulePrivilegesById/{id}")
	public ResponseEntity<?> getModulePrivilegesById(@PathVariable Long id, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.getModulePrivilegesById(id);
		else
			return ResponseEntity.badRequest().body("User not found");
	}

	/* Roles */
	@PostMapping("/saveRole")
	public ResponseEntity<?> saveRole(@RequestBody RolesDto dto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.saveRole(dto);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getAllRoles")
	public ResponseEntity<?> getAllRoles(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.getAllRoles();

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/deleteRole")
	public ResponseEntity<?> deleteRole(@RequestParam("roleId") Long roleId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.deleteRole(roleId);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	/* Modules */
	@PostMapping("/saveModule")
	public ResponseEntity<?> saveModule(@RequestBody ModulesDto dto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.saveModule(dto);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getAllModules")
	public ResponseEntity<?> getAllModules(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.getAllModules();

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/deleteModule")
	public ResponseEntity<?> deleteModule(@RequestParam("moduleId") Long moduleId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.deleteModule(moduleId);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	/* SubModules */
	@PostMapping("/saveSubModule")
	public ResponseEntity<?> saveSubModule(@RequestBody SubModulesDto dto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.saveSubModule(dto);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@GetMapping("/getSubModules")
	public ResponseEntity<?> getSubModulesByModuleId(@RequestParam("moduleId") Long moduleId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)
			return modulePrivilegeService.getSubModulesByModuleId(moduleId);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

	@DeleteMapping("/deleteSubModule")
	public ResponseEntity<?> deleteSubModule(@RequestParam("subModuleId") Long subModuleId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null)

			return modulePrivilegeService.deleteSubModule(subModuleId);

		else
			return ResponseEntity.badRequest().body("User not found");
	}

}
