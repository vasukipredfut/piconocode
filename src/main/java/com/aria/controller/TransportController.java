package com.aria.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.TransportService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TransportController {
	@Autowired
	private TransportService transportService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/transport/creteTransport")
	public ResponseEntity<?> creteTransport(@RequestBody TransportDto transportDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return transportService.creteTransport(transportDto);
	}

	@GetMapping("/transport/getSingleVehicleDetail/{transportId}")
	public ResponseEntity<?> getSingleVehicleDetail(@PathVariable("transportId") Long transportId,Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return transportService.getSingleVehicleDetail(transportId);
	}
	
	@GetMapping("/transport/getAllvehiclesDetails")
	public ResponseEntity<?> getAllvehiclesDetails(Authentication authentication,Pageable pageable) {
		return transportService.getAllvehiclesDetails(pageable);
	}
	
	@GetMapping("/transport/getTransportVehicleDetails")
	public ResponseEntity<?> getTransportVehicleDetails(@RequestParam(value = "vehicleName",required = false) String vehicleName,@RequestParam(value = "vehicleType",required = false) String vehicleType,@RequestParam(value = "capacity",required = false) String capacity, Authentication authentication, Pageable pageable) {
		return transportService.getTransportVehicleDetails(vehicleName,vehicleType,capacity,pageable);
	}
	
	//When 2 comes means it's contractal vehicle and 1 means own vehicle
	@GetMapping("/transport/getVehicleTypeBasedOnVehicleOwner")
	public ResponseEntity<?> getVehicleTypeBasedOnVehicleOwner(@RequestParam("transportMode") Long transportMode,Authentication authentication,Pageable pageable){
		return transportService.getVehicleTypeBasedOnVehicleOwner(transportMode);
	}
	
	@GetMapping("/transport/getVehicleTypeBasedOnVehicleName")
	public ResponseEntity<?> getVehicleTypeBasedOnVehicleName(@RequestParam("transportMode") Long transportMode, @RequestParam(value = "vehicleType",required = false) String vehicleType, @RequestParam(value = "vehicleName",required = false) String vehicleName, Authentication authentication){
		return transportService.getVehicleTypeBasedOnVehicleName(transportMode, vehicleType, vehicleName);
	}
	
	@GetMapping("/transport/getVehicleNumberBasedOnVehicleType")
	public ResponseEntity<?> getVehicleNumberBasedOnVehicleType(@RequestParam("vehicleType") Long vehicleType,Authentication authentication,Pageable pageable){
		return transportService.getVehicleNumberBasedOnVehicleType(vehicleType);
	}
	
	@PostMapping("/transport/updateTransport")
	public ResponseEntity<?> updateTransport(@RequestParam("transportId") Long transportId,@RequestBody TransportDto transportDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return transportService.updateTransport(transportDto,transportId);
	}
	
	@DeleteMapping("/transport/deleteTransport")
	public ResponseEntity<?> deleteTransport(@RequestParam("transportId") Long transportId,Authentication authentication){
		return transportService.deleteTransport(transportId);
	}
}
