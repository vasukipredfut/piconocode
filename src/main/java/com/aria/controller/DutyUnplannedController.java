package com.aria.controller;
import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.User;
import com.aria.service.DutyUnplannedService;
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
public class DutyUnplannedController {


    @Autowired
    DutyUnplannedService dutyUnplannedService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveDutyUnplanned")
    public ResponseEntity<?> saveDutyUnplanned(@RequestBody DutyUnplannedDto dutyUnplannedDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyUnplannedService.saveDutyUnplanned(dutyUnplannedDto, user.get());
    }

    @PutMapping("/updateDutyUnplanned")
    public ResponseEntity<?> updateDutyUnplanned(@RequestBody DutyUnplannedDto dutyUnplannedDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyUnplannedService.updateDutyUnplanned(dutyUnplannedDto, user.get());
    }

    @GetMapping("/getDutyUnplannedList")
    public ResponseEntity<?> getDutyUnplannedList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dutyUnplannedService.listAllDutyUnplanned(pageable);
        } else {
            return new ResponseEntity<List<DutyUnplannedDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getDutyUnplannedDetails/{id}")
    public ResponseEntity<?> getDutyUnplannedDetails(@PathVariable String id, Authentication authentication) {

        return dutyUnplannedService.getDutyUnplannedDetails(id);
    }

    @DeleteMapping("/deleteDutyUnplannedDetails/{id}")
    public ResponseEntity<?> deleteDutyUnplannedDetails(@PathVariable String id, Authentication authentication) {

        return dutyUnplannedService.deleteDutyUnplanned(id);
    }
    @GetMapping("/getdutyUnplannedfilterList")
	public ResponseEntity<?> dutyUnplannedList(
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "departmentId", required = false) Long departmentId,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
	     	@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return dutyUnplannedService.dutyUnplannedfilterList(userId,departmentId,dateFrom,dateTo, pageable);
		} else {
			return new ResponseEntity<List<DutyUnplannedDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	} 
}
