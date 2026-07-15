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
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.dto.ActionConfigurationDto;
import com.aria.dto.ActionDto;
import com.aria.model.User;
import com.aria.service.ActionService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ActionController {

	@Autowired
	ActionService actionService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/actionConfiguration")
	public ResponseEntity<?> saveActionConfig(@RequestBody ActionConfigurationDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return actionService.saveActionConfiguration(reqDto, user.get());
	}



	@GetMapping("/actionConfiguration")
	public ResponseEntity<?> getAllDetails( Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return actionService.getAllDetailsForActionConfiguration(user.get(), pageable);
		} else {
			return new ResponseEntity<List<ActionConfigurationDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}
	
	@GetMapping("/actionConfiguration/{id}")
	public ResponseEntity<?> getActionConfigDetailsById(@PathVariable String id, Authentication authentication) {
		return actionService.getActionConfigDetailsById(id);
	}

//	@GetMapping("/action/{id}")
//	public ResponseEntity<?> getDutyTransferDetails(@PathVariable Long id, Authentication authentication) {
//		return actionService.getActionDetails(id);
//	}
//
//	@DeleteMapping("/action/{id}")
//	public ResponseEntity<?> deleteShiftsDetails(@PathVariable Long id,
//			Authentication authentication) {
//		return actionService.deleteAction(id);
//	}
	
	
	//Action 
	
	@PostMapping("/action")
	public ResponseEntity<?> saveAction(@RequestBody ActionDto reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return actionService.saveAction(reqDto, user.get());
	}
	
	@GetMapping("/action")
	public ResponseEntity<?> getAllDetailsForAction( Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return actionService.getAllDetails(user.get(), pageable);
		} else {
			return new ResponseEntity<List<ActionDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}
	
	@GetMapping("/action/{id}")
	public ResponseEntity<?> getActionDetailsById(@PathVariable String id, Authentication authentication) {
		return actionService.getActionDetailsById(id);
	}
}
