package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.dto.ShiftsDto;
import com.aria.model.User;
import com.aria.service.ShiftsService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ShiftsController {

	@Autowired
	ShiftsService shiftsService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/createShifts")
	public ResponseEntity<?> saveShifts(@RequestBody ShiftsDto shiftsDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		 shiftsService.createShifts(shiftsDto, user.get());
		return new ResponseEntity<Status>(new Status("200","shift created successfully"), HttpStatus.OK);
	}

	@PostMapping("/saveShifts")
	public ResponseEntity<?> saveShifts(@RequestBody ArrayList<ShiftsDto> shiftsDtos, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return shiftsService.saveShifts(shiftsDtos, user.get());
	}

	@PutMapping("/updateShifts")
	public ResponseEntity<?> updateShifts(@RequestBody ShiftsDto shiftsDtos, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return shiftsService.updateShifts(shiftsDtos, user.get());
	}

	@GetMapping("/listAllShifts")
	public ResponseEntity<?> listAllShifts(@RequestParam(value = "shiftType", required = false) String shiftType, Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return shiftsService.listAllShifts(shiftType, user.get(), pageable);
		} else {
			return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}
	
	@GetMapping("/listAllShiftswopagination")
	public ResponseEntity<?> listAllShifts(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return shiftsService.listAllShiftswopage();
		} else {
			return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/listAllShiftsTypes")
	public ResponseEntity<?> listAllShiftsTypes(@RequestParam(value = "shiftType", required = false) String shiftType, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return shiftsService.findAllShiftCategory(shiftType);
		} else {
			return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/listAllShiftsByTypes")
	public ResponseEntity<?> listAllShifts(@RequestParam(value = "shiftType", required = false) String shiftType, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return shiftsService.listAllShifts(shiftType);
		} else {
			return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getShiftsDetails/{id}")
	public ResponseEntity<?> getShiftDetails(@PathVariable String id, Authentication authentication) {
		return shiftsService.getShiftDetails(id);
	}

	@DeleteMapping("/deleteShiftsDetails/{id}")
	public ResponseEntity<?> deleteShiftsDetails(@PathVariable Long id, Authentication authentication) {
		return shiftsService.deleteShifts(id);
	}
}
