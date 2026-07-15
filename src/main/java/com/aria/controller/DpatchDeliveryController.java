package com.aria.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.aria.dao.UserRepository;
import com.aria.dto.DpatchDeliveryRequestDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.DpatchDeliveryService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class DpatchDeliveryController {
	@Autowired
	private DpatchDeliveryService dpatchDeliveryService;
	
	@Autowired
	private UserRepository userRepository;
	
	@PostMapping("/delivery/createDelivery")
	public ResponseEntity<?> createDelivery(@RequestBody DpatchDeliveryRequestDto dpatchDeliveryRequestDto,Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        try {
        	return dpatchDeliveryService.createDelivery(dpatchDeliveryRequestDto,user.get());
        }catch(Exception e) {
        	return new ResponseEntity<Object>(new Status("500","Something Went Wrong..."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping("/delivery/getAllVehicalNumber")
	public ResponseEntity<?> getAllVehicalNumber(@RequestParam() Long vehicleType,Authentication authentication){
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return dpatchDeliveryService.getAllVehicalNumber(vehicleType);
	}
}
