package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.RodentMonitorDto;
import com.aria.model.User;
import com.aria.service.RodentMonitorService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RodentMonitorController {

	@Autowired
	RodentMonitorService rodentMonitorService;

	@Autowired
	UserRepository userRepository;
	

	@PostMapping("/rodentMonitor")
	public ResponseEntity<?> saveRodentMonitor(@RequestBody RodentMonitorDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return rodentMonitorService.saveRodentMonitor(reqDto, user.get());
	}

	@PutMapping("/rodentMonitor")
	public ResponseEntity<?> updateRodentMonitor(@RequestBody RodentMonitorDto reqDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return rodentMonitorService.updateRodentMonitor(reqDto, user.get());
	}

	@GetMapping("/rodentMonitor")
	public ResponseEntity<?> getAllDetails(Pageable pageable, Authentication authentication,
			@RequestParam(value = "goDown", required = false) String goDown,
			@RequestParam(value = "reason", required = false) String reason) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return rodentMonitorService.listAllRodentMonitorDetails(user.get(), pageable, goDown, reason);
		} else {
			return new ResponseEntity<List<RodentMonitorDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/rodentMonitor/{id}")
	public ResponseEntity<?> getRodentMonitorDetails(@PathVariable String id, Authentication authentication) {
		return rodentMonitorService.getRodentMonitorDetails(id);
	}

	@DeleteMapping("/rodentMonitor/{id}")
	public ResponseEntity<?> deleteRodentMonitorDetails(@PathVariable Long id,
			Authentication authentication) {
		return rodentMonitorService.deleteRodentMonitor(id);
	}
	
}
