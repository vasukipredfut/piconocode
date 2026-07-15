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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.GoDownRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.GoDownExternalDto;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.GodownService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GoDownController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	GodownService godownService;

	@Autowired
	GoDownRepository goDownRepository;

	@PostMapping("/saveExternalGodown")
	public ResponseEntity<?> saveExternalGodown(@RequestBody GoDownExternalDto goDown, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			return godownService.saveExternalGoDown(goDown, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

	@PostMapping("/updateExternalGodown")
	public ResponseEntity<?> updateExternalGodown(@RequestBody GoDownExternalDto goDown,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			return godownService.updateExternalGoDown(goDown, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}


	@PostMapping("/updateBlendingExternalGoDown")
	public ResponseEntity<?> updateBlendingExternalGoDown(@RequestBody GoDownExternalDto goDown,
												  Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			return godownService.updateBlendingExternalGoDown(goDown, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

	@PostMapping("/updateGodownRecipt")
	public ResponseEntity<?> updateGodownRecipt(@RequestBody GoDownExternalDto goDown, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user != null) {
			return godownService.updateGoDownRecipt(goDown, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

	@GetMapping("/getAllExternalGodown")
	public ResponseEntity<?> getAllExternalGodownList(@RequestParam(value = "branch", required = true) Long branch,
													  @RequestParam(value = "firmId", required = false) Long firmId,
													  @RequestParam(value = "status", required = true) String status,
													  @RequestParam(value = "trnNo", required = false) String trnNo,
													  @RequestParam(value = "fromDate", required = false) String fromDate,
													  @RequestParam(value = "toDate", required = false) String toDate,
			Pageable pageable, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return godownService.listAll( firmId, branch, status, trnNo, fromDate, toDate, pageable);
		} else {
			return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getExternalGoDownById")
	public ResponseEntity<?> getExternalGoDownById(@RequestParam(value = "id", required = true) Long id,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return godownService.getExternalGoDownById(id, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}

}
