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

@RestController
@RequestMapping("/api")
@CrossOrigin
public class EbGeneratorController {

	@Autowired
	EBGeneratorService ebGenService;

	@Autowired
	UserRepository userRepository;
	
	//EB

	@PostMapping("/eb")
	public ResponseEntity<?> saveEB(@RequestBody ArrayList<EBDto> reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return ebGenService.saveEB(reqDto, user.get());
	}

	@PutMapping("/eb")
	public ResponseEntity<?> updateEB(@RequestBody EBDto shiftsDtos,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return ebGenService.updateEB(shiftsDtos, user.get());
	}

	@GetMapping("/eb")
	public ResponseEntity<?> getAllDetails(
			@RequestParam(value = "machineId", required = false) String machineId,
			@RequestParam(value = "serviceNo", required = false) String serviceNo,
			@RequestParam(value = "serviceName", required = false) String serviceName,
			@RequestParam(value = "serviceCategory", required = false) String serviceCategory,
			@RequestParam(value = "status", required = false) String status,
			Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return ebGenService.listAllEBDetails(serviceNo, serviceName, serviceCategory, status, user.get(), machineId, pageable);
		} else {
			return new ResponseEntity<List<EBDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/allEb")
	public ResponseEntity<?> getAllDetails(
			@RequestParam(value = "machineId", required = false) String machineId,
			@RequestParam(value = "serviceNo", required = false) String serviceNo,
			@RequestParam(value = "serviceName", required = false) String serviceName,
			@RequestParam(value = "serviceCategory", required = false) String serviceCategory,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "paging", required = false,defaultValue = "true") boolean paging,
			Pageable pageable,
			 Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			if(paging){
				return ebGenService.listAllEBDetails(serviceNo, serviceName, serviceCategory, status, user.get(),machineId,pageable);
			}
			return ebGenService.listAllEBDetails(serviceNo, serviceName, serviceCategory, status,machineId, user.get());
		} else {
			return new ResponseEntity<List<EBDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/eb/{id}")
	public ResponseEntity<?> getEBDetails(@PathVariable String id, Authentication authentication) {
		return ebGenService.getEBDetails(id);
	}

	@DeleteMapping("/eb/{id}")
	public ResponseEntity<?> deleteEBDetails(@PathVariable Long id,
			Authentication authentication) {
		return ebGenService.deleteEB(id);
	}
	
	@GetMapping("/eb/getNames")
	public ResponseEntity<?> getEBNames(Authentication authentication) {
		return ebGenService.getEBNames();
	}
	
	//Generator
	
	@PostMapping("/generator")
	public ResponseEntity<?> saveGen(@RequestBody ArrayList<GeneratorDto> reqDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return ebGenService.saveGenerator(reqDto, user.get());
	}

	@PutMapping("/generator")
	public ResponseEntity<?> updateGen(@RequestBody GeneratorDto reqDtos,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return ebGenService.updateGenerator(reqDtos, user.get());
	}

	@GetMapping("/generator")
	public ResponseEntity<?> getAllGenDetails(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "machineId", required = false) String machineId,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo,
		Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return ebGenService.listAllGeneratorDetails(name, status, machineId, dateFrom, dateTo, user.get(), pageable);
		} else {
			return new ResponseEntity<List<GeneratorDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/generator/{id}")
	public ResponseEntity<?> getGenDetails(@PathVariable String id, Authentication authentication) {
		return ebGenService.getGeneratorDetails(id);
	}

	@DeleteMapping("/generator/{id}")
	public ResponseEntity<?> deleteGenDetails(@PathVariable Long id,
			Authentication authentication) {
		return ebGenService.deleteGenerator(id);
	}
	
	@GetMapping("/generator/getNames")
	public ResponseEntity<?> getGeneratorNames(Authentication authentication) {
		return ebGenService.getGeneratorNames();
	}
}
