package com.aria.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dto.TransportDriverDto;
import com.aria.model.Status;
import com.aria.service.TransportDriverService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TransportDriverController {
	@Autowired
	private TransportDriverService transportDriverService;
	
	@GetMapping("/driver/getAllDrivers")
	public ResponseEntity<?> getAllDrivers(Authentication authentication){
		return transportDriverService.getAllDrivers();
	}
	
	@GetMapping("/driver/getAllDriversBasedOnName")
	public ResponseEntity<?> getAllDriversBasedOnName(@RequestParam("driverName") String driverName,Authentication authentication){
		return transportDriverService.getAllDriversBasedOnName(driverName);
	}
	
	@PostMapping("/driver/saveDRiver")
	public ResponseEntity<?> saveDriver(@RequestBody TransportDriverDto transportDriverDto,Authentication authentication){
		try {
			return transportDriverService.saveDriver(transportDriverDto);
		}catch (Exception e) {
			System.out.println("ERROR IN SAVE DRIVER : "+e.getMessage());
			return new ResponseEntity<Object>(new Status("500","Exception Occured..."),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/driver/getAssignDriversToVehicle")
	public ResponseEntity<?> getAssignDriversToVehicle(@RequestParam("vehicleId") Long vehicleId,Authentication authentication){
		return transportDriverService.getAssignDriversToVehicle(vehicleId);
	}
}
