package com.aria.controller;

import java.util.List;
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
import com.aria.dto.PrintViewDto;
import com.aria.dto.WindmillTimingResponseDto;
import com.aria.dto.WindmillsaveDto;
import com.aria.model.User;
import com.aria.service.WindmillService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class WindmillController {

	@Autowired
	WindmillService windmillservice;
	
	@Autowired
	UserRepository userRepository;

	@GetMapping("/getplans")
	public ResponseEntity<?> getPlans(@RequestParam String type) {
		return windmillservice.getPlan(type);
	}

	@GetMapping("/getFromDate")
	public ResponseEntity<?> getFromDate(@RequestParam Long id) {
		return windmillservice.getFromDate(id);
	}
	
	@GetMapping("/getAllWindmillDetails")
	public ResponseEntity<?> getAllTimingDetails(@RequestParam Long windmillId, @RequestParam String fromDate){
		return ResponseEntity.ok(windmillservice.getAllTimingDetails(windmillId, fromDate));
	}

	@GetMapping("/timings")
	public ResponseEntity<?> getAllTimings(@RequestParam(value="windmillid",required = false)Long windmillid,
			@RequestParam(value="fromdate",required = false)String fromdate,
			@RequestParam(value="month",required = false)Long month,
			@RequestParam(value="year",required = false)Long year) {

		return ResponseEntity.ok(windmillservice.getAllTimingData(windmillid, fromdate, month, year));
	}
	
	@GetMapping("/getList")
	public ResponseEntity<?> getList(@RequestParam(value="month",required = false)Long month,
			@RequestParam(value="year",required = false)Long year,
			@RequestParam(value="plan",required = false)Long plan,
			@RequestParam(value="sourcetype",required = false)String sourcetype,
			@RequestParam(value="createddate",required = false)String createddate,
			@RequestParam(value="workingdays",required = false)Long workingdays,
			Pageable pageable){
		return windmillservice.getListForWindmill(month,year, plan, sourcetype,createddate,workingdays,pageable);
		
	}
	
	@PostMapping("/savewindmill")
	public ResponseEntity<?> savewindmill(@RequestBody WindmillsaveDto request,Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		
		return windmillservice.updateWindmillReading(request, user.get());
		
	}
	
	@GetMapping("/getViewDetails")
	public ResponseEntity<?> getViewDetails(@RequestParam(value="windmillid",required = false)Long windmillid,
			@RequestParam(value="fromdate",required = false)String fromdate,
			@RequestParam(value="todate",required = false)String todate){
		return windmillservice.getViewDetails(windmillid,fromdate,todate);
	}
	
	@PostMapping("/getPrintDetails")
	public ResponseEntity<?> getPrintDetails(@RequestBody List<PrintViewDto> request){
		return windmillservice.getPrintDetails(request);
	}
	
	@GetMapping("/getnewtimming")
	public ResponseEntity<?> getNewTimming(@RequestParam(value="windmillid",required = false)Long windmillid,
			@RequestParam(value="fromdate",required = false)String fromdate,
			@RequestParam(value="month",required = false)Long month,
			@RequestParam(value="year",required = false)Long year){
		 return ResponseEntity.ok(windmillservice.getAllTimingData(windmillid, fromdate, month, year));
	}

}
