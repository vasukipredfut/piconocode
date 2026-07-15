package com.aria.controller;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
public class InterviewController {

	@Autowired
	InterviewService interviewService;

	@Autowired
	UserRepository userRepository;

	// candidate interview details

	@PostMapping("/saveInterview")
	public ResponseEntity<?> saveInterview(@RequestBody InterviewDto interviewDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.saveInterview(interviewDto, user.get());
	}

	@PutMapping("/updateInterview")
	public ResponseEntity<?> updateInterview(@RequestBody InterviewDto interviewDto, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.updateInterview(interviewDto, user.get());
	}

	@GetMapping("/getInterviewList")
	public ResponseEntity<?> getInterviewDetails(Pageable pageable, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			return interviewService.listAllInterview(pageable);
		} else {
			return new ResponseEntity<List<InterviewDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getInterviewFilterList")
	public ResponseEntity<?> interviewList(
			@RequestParam(value = "candidateName", required = false) String candidateName,
			@RequestParam(value = "employeeType", required = false) String employeeType,
			@RequestParam(value = "interviewPosition", required = false) String interviewPosition,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "adhaar", required = false) String adhaar,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "branchId", required = false) Long branchId,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Authentication authentication,
			Pageable pageable) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			return interviewService.interviewfilterList(candidateName, employeeType, interviewPosition, status,
					branchId, /* interviewTime, */dateFrom, dateTo, adhaar, place, pageable);
		} else {
			return new ResponseEntity<List<InterviewDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getInterviewDetails/{id}")
	public ResponseEntity<?> getInterviewDetails(@PathVariable String id, Authentication authentication) {
		return interviewService.getInterviewDetails(id);
	}

	@DeleteMapping("/deleteInterviewDetails/{id}")
	public ResponseEntity<?> deleteInterviewDetails(@PathVariable String id, Authentication authentication) {
		return interviewService.deleteInterviewDetails(id);
	}



	// interviewer assign

	@PostMapping("/saveInterviewerAssign")
	public ResponseEntity<?> saveInterviewerAssign(@RequestBody List<InterviewerAssignDto> interviewerAssignDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.saveInterviewerAssign(interviewerAssignDto, user.get());
	}

	@PutMapping("/interviewerAssign")
	public ResponseEntity<?> updateInterviewerAssign(@RequestBody List<InterviewerAssignDto> interviewerAssignDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.updateInterviewerAssign(interviewerAssignDto, user.get());
	}

	@GetMapping("/getInterviewerAssignList")
	public ResponseEntity<?> getInterviewerAssignDetails(Pageable pageable, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			return interviewService.listAllInterviewerAssign(pageable);
		} else {
			return new ResponseEntity<List<InterviewerAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/interviewerAssign")
	public ResponseEntity<?> interviewerAssignList(@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "candidateName", required = false) String candidateName,
			@RequestParam(value = "interviewerName", required = false) String interviewerName,
			@RequestParam(value = "examinerName", required = false) String examinerName,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			return interviewService.interviewerAssignFilterList(branch, candidateName, interviewerName, examinerName, dateFrom,
					dateTo, pageable);
		} else {
			return new ResponseEntity<List<InterviewerAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/interviewerAssign/{id}")
	public ResponseEntity<?> getInterviewerAssignDetails(@PathVariable String id, Authentication authentication) {
		return interviewService.getInterviewerAssignDetails(id);
	}

	@DeleteMapping("/interviewerAssign/{id}")
	public ResponseEntity<?> deleteInterviewerAssignDetails(@PathVariable String id, Authentication authentication) {
		return interviewService.deleteInterviewerAssignDetails(id);
	}

	// Score assessment

	@PostMapping("/saveScoreAssessment")
	public ResponseEntity<?> saveScoreAssessment(@RequestBody ScoreAssessmentDto scoreAssessmentDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.saveScoreAssessment(scoreAssessmentDto, user.get());
	}

	@PutMapping("/scoreAssessment")
	public ResponseEntity<?> updateScoreAssessment(@RequestBody List<ScoreAssessmentDto> scoreAssessmentDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return interviewService.updateScoreAssessment(scoreAssessmentDto, user.get());
	}

	/*
	 * @GetMapping("/getScoreAssessmentList") public ResponseEntity<?>
	 * getScoreAssessmentList(Authentication authentication, Pageable pageable) {
	 * 
	 * UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
	 * Optional<User> user =
	 * userRepository.findByUsername(userPrincipal.getUsername()); if
	 * (user.isPresent()) { return
	 * interviewService.listAllScoreAssessment(pageable); } else { return new
	 * ResponseEntity<List<ScoreAssessmentDto>>(new ArrayList<>(), HttpStatus.OK); }
	 * }
	 */

	@GetMapping("/scoreAssessment")
	public ResponseEntity<?> scoreAssessmentFilterList(
			@RequestParam(value = "candidateName", required = false) String candidateName,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.isPresent()) {
			return interviewService.scoreAssessmentFilterList(candidateName, dateFrom, dateTo, pageable);
		} else {
			return new ResponseEntity<List<ScoreAssessmentDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/scoreAssessment/{id}")
	public ResponseEntity<?> getScoreAssessmentDetails(@PathVariable String id, Authentication authentication) {

		return interviewService.getscoreAssessmentDetails(id);
	}

	@DeleteMapping("/scoreAssessment/{id}")
	public ResponseEntity<?> deleteScoreAssessmentDetails(@PathVariable String id, Authentication authentication) {

		return interviewService.deleteScoreAssessment(id);
	}

}
