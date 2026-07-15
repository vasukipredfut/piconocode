
package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.InternalMaterialIssueDto;
import com.aria.model.User;
import com.aria.service.InternalMaterialIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class InternalMaterialIssueController {

	@Autowired
	InternalMaterialIssueService internalMaterialIssueService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveInternalMaterialIssue")
	public ResponseEntity<?> saveInternalMaterialIssue(@RequestBody InternalMaterialIssueDto internalMaterialIssueDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return internalMaterialIssueService.saveInternalMaterialIssue(internalMaterialIssueDto, user.get());
	}

	@PutMapping("/updateInternalMaterialIssue")
	public ResponseEntity<?> updateInternalMaterialIssue(@RequestBody InternalMaterialIssueDto internalMaterialIssueDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return internalMaterialIssueService.updateInternalMaterialIssue(internalMaterialIssueDto, user.get());
	}

	@GetMapping("/getInternalMaterialIssue")
	public ResponseEntity<?> getInternalMaterialIssue(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return internalMaterialIssueService.listAll();
		} else {
			return new ResponseEntity<List<InternalMaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getInternalMaterialIssue/{id}")
	public ResponseEntity<?> getInternalMaterialIssue(@PathVariable String id, Authentication authentication) {
		return internalMaterialIssueService.getInternalMaterialIssue(id);
	}

	@DeleteMapping("/deleteInternalMaterialIssue/{id}")
	public ResponseEntity<?> deleteInternalMaterialIssue(@PathVariable String id, Authentication authentication) {
		return internalMaterialIssueService.deleteInternalMaterialIssue(id);
	}
}
