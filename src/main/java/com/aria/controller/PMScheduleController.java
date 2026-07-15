package com.aria.controller;

import java.text.ParseException;
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
import com.aria.dto.PMScheduleDto;
import com.aria.model.User;
import com.aria.service.PMScheduleService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PMScheduleController {

	@Autowired
	PMScheduleService panelService;

	@Autowired
	UserRepository userRepository;
	

	@PostMapping("/pmSchedule")
	public ResponseEntity<?> savePMSchedule(@RequestBody PMScheduleDto reqDto, Authentication authentication) throws ParseException {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return panelService.savePMSchedule(reqDto, user.get());
	}

	@PutMapping("/pmSchedule")
	public ResponseEntity<?> updatePMSchedule(@RequestBody PMScheduleDto reqDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return panelService.updatePMSchedule(reqDto, user.get());
	}

	@GetMapping("/pmSchedule")
	public ResponseEntity<?> getAllDetails(
			@RequestParam(value = "scheduleName", required = false) String scheduleName,
			@RequestParam(value = "taskCategory", required = false) String taskCategory,
			@RequestParam(value = "machineName", required = false) String machineName,
			@RequestParam(value = "machineId", required = false) Long machineId,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo,
			@RequestParam(value = "taskGroup", required = false) String taskGroup,
			Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return panelService.listAllPMScheduleDetails(scheduleName, taskCategory, machineName, machineId, dateFrom, dateTo, taskGroup,user.get(), pageable);
		} else {
			return new ResponseEntity<List<PMScheduleDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/pmSchedule/{id}")
	public ResponseEntity<?> getPMScheduleDetails(@PathVariable String id, Authentication authentication) {
		return panelService.getPMScheduleDetails(id);
	}

	@DeleteMapping("/pmSchedule/{id}")
	public ResponseEntity<?> deletePMScheduleDetails(@PathVariable Long id,
			Authentication authentication) {
		return panelService.deletePMSchedule(id);
	}



	@GetMapping("/pmScheduleDetailsList")
	public ResponseEntity<?> pmScheduleDetailsList(
			@RequestParam(value = "scheduleName", required = false) String scheduleName,
			@RequestParam(value = "taskCategory", required = false) String taskCategory,
			@RequestParam(value = "machineName", required = false) String machineName,
			@RequestParam(value = "machineId", required = false) Long machineId,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo,
			@RequestParam(value = "taskGroup", required = false) String taskGroup,
			@RequestParam(value = "status", required = false) String status,
			Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return panelService.listAllPMScheduleDetailsList(scheduleName, taskCategory, machineName, machineId, dateFrom, dateTo, taskGroup, status ,  pageable);
		} else {
			return new ResponseEntity<List<PMScheduleDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PutMapping("/amendmentScheduleStatus/{id}")
	public ResponseEntity<?> amendmentScheduleStatus(@PathVariable Long id,
													 Authentication authentication) {
		return panelService.amendmentScheduleStatus(id);
	}


}
