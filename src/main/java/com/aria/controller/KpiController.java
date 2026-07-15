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
import com.aria.dto.EBDto;
import com.aria.dto.GeneratorDto;
import com.aria.model.User;
import com.aria.service.EBGeneratorService;
import com.aria.service.KpiService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class KpiController {

	@Autowired
	KpiService kpiService;

	@Autowired
	UserRepository userRepository;
	
	//Kpi Name

	/*@PostMapping("/kpi")
	public ResponseEntity<?> saveEB(@RequestBody ArrayList<EBDto> reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return kpiService.saveEB(reqDto, user.get());
	}

	@PutMapping("/kpi")
	public ResponseEntity<?> updateEB(@RequestBody EBDto shiftsDtos,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return kpiService.updateEB(shiftsDtos, user.get());
	}
	
	@GetMapping("/kpi/{id}")
	public ResponseEntity<?> getEBDetails(@PathVariable String id, Authentication authentication) {
		return kpiService.getEBDetails(id);
	}

	@DeleteMapping("/kpi/{id}")
	public ResponseEntity<?> deleteEBDetails(@PathVariable Long id,
			Authentication authentication) {
		return kpiService.deleteEB(id);
	}*/

	@GetMapping("/kpi")
	public ResponseEntity<?> getAllDetails(
			 @RequestParam(value = "modeOfEntry", required = false) String modeOfEntry,
			Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return kpiService.listAllKpiDetails(modeOfEntry, user.get(), pageable);
		} else {
			return new ResponseEntity<List<EBDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	
}
