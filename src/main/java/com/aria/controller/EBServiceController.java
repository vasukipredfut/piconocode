package com.aria.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.EBTableDetailsDto;
import com.aria.dto.HtaChargesDto;
import com.aria.dto.HttfgrpDto;
import com.aria.dto.HttslapDto;
import com.aria.model.HtDayTrn;
import com.aria.model.HtaCharges;
import com.aria.model.Httfgrp;
import com.aria.model.Httslap;
import com.aria.model.User;
import com.aria.service.EBService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class EBServiceController {
	@Autowired
	EBService ebservice;

	@Autowired
	UserRepository userRepository;

	@GetMapping("/getListEbService")
	public ResponseEntity<?> getListofEbService(@RequestParam(value = "shiftdate", required = false) String shiftdate,
			@RequestParam(value = "serviceid", required = false) Long serviceid,
			@RequestParam(value = "timmingid", required = false) Long timmingid,
			@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year,
			@RequestParam(value = "status", required = false) String status,
//	        @RequestParam(value="createddate",required = false)String createddate,
			Pageable pageable) {
		return ebservice.getListOfEbService(shiftdate, serviceid, timmingid, month, year, status, pageable);
	}

	@GetMapping("/gettabledetails")
	public ResponseEntity<?> getTabledetails(@RequestParam(value = "shiftdate", required = false) String shiftdate,
			@RequestParam(value = "serviceid", required = false) Long serviceid,
			@RequestParam(value = "timmingid", required = false) Long timmingid,
			@RequestParam(value = "month", required = false) Long month,
			@RequestParam(value = "year", required = false) Long year) {
		return ebservice.getTableDetails(serviceid, timmingid, shiftdate, month, year);

	}

	@PostMapping("/saveEBTable")
	public ResponseEntity<?> saveEBTableDetails(@RequestBody EBTableDetailsDto dto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return ebservice.saveEBTableDetail(dto, user.get());
	}

	@GetMapping("/getEbfromdate")
	public ResponseEntity<?> getEbfromdate(@RequestParam Long serviceid, @RequestParam Long timmingid) {
		return ebservice.getFromDate(serviceid, timmingid);
	}

	@GetMapping("/getservicename")
	public ResponseEntity<?> getservicename() {
		return ebservice.getServicename();
	}

	@GetMapping("/gettimeslap")
	public ResponseEntity<?> gettimeslap() {
		return ebservice.gettimeslap();
	}

}
