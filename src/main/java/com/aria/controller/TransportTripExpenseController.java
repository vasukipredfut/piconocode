package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.dto.TransportTripExpenseDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.TransportTripExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TransportTripExpenseController {

	@Autowired
	TransportTripExpenseService transportTripService;

	@Autowired
	UserRepository userRepository;


	@GetMapping("/transportTripExpense")
	public ResponseEntity<?> transportTrip(@RequestParam(value = "tripName",required = false) String tripName, @RequestParam(value = "distance",required = false) String distance, @RequestParam(value = "tripId",required = false) Long tripId, Authentication authentication, Pageable pageable) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return transportTripService.getAllTransportTripExpense(tripName,distance,tripId,pageable);
	}



    @DeleteMapping("/transportTripExpense/{id}")
    public ResponseEntity<?> deleteTransportTrip(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        transportTripService.deleteTransportExpense(id);
        return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
    }

	@GetMapping("/transportTripExpense/{id}")
	public ResponseEntity<?> getTransportTrip(@PathVariable Long id,Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return  transportTripService.getTransportExpense(id);
	}

	@PostMapping("/addOrEdit/transportTripExpense")
	public ResponseEntity<?> addOrEditTransportTrip(@RequestBody TransportTripExpenseDto expenseDto, Authentication authentication) throws ParseException {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		String message = "Transport Trip Expenses added successfully!!";
		transportTripService.mapTransportWithExpense(expenseDto);
		return new ResponseEntity<Status>(new Status("200", message), HttpStatus.OK);
	}


}
