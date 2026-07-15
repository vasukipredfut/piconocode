package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.SkillGradeLevelDto;
import com.aria.model.User;
import com.aria.service.SkillGradeLevelService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SkillGradeLevelController {

	@Autowired
	SkillGradeLevelService skillGradeLevelService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveSkillGradeLevel")
	public ResponseEntity<?> saveSkillGradeLevel(@RequestBody List<SkillGradeLevelDto> skillGradeLevelDtos,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return skillGradeLevelService.saveSkillGradeLevel(skillGradeLevelDtos, user.get());
	}

	@PutMapping("/updateSkillGradeLevel")
	public ResponseEntity<?> updateSkillGradeLevel(@RequestBody SkillGradeLevelDto skillGradeLevelDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return skillGradeLevelService.updateSkillGradeLevel(skillGradeLevelDto, user.get());
	}

	@GetMapping("/listAllSkillGradeLevel")
	public ResponseEntity<?> listAllSkillGradeLevel(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return skillGradeLevelService.listAllSkillGradeLevel();
		} else {
			return new ResponseEntity<List<SkillGradeLevelDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/skillGradeLevel/{id}")
	public ResponseEntity<?> getSkillGradeLevel(@PathVariable String id, Authentication authentication) {
		return skillGradeLevelService.getSkillGradeLevel(id);
	}

	@DeleteMapping("/skillGradeLevel/{id}")
	public ResponseEntity<?> deleteskillGradeLevel(@PathVariable String id, Authentication authentication) {
		return skillGradeLevelService.deleteskillGradeLevel(id);
	}
}
