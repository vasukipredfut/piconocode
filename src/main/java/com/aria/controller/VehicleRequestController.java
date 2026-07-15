package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.TransportTripService;
import com.aria.service.VehicleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class VehicleRequestController {

    @Autowired
    VehicleRequestService vehicleRequestService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/vehicleRequest")
    public ResponseEntity<?> vehicleRequest(@RequestParam(value = "travelDate",required = false) String travelDate,@RequestParam(value = "capacity",required = false) String capacity,@RequestParam(value = "typeofVehicle",required = false) String typeofVehicle,@RequestParam(value = "tripName",required = false) String tripName,Authentication authentication, Pageable pageable) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return vehicleRequestService.getAllVehicleRequest(travelDate,capacity,typeofVehicle,tripName,pageable);
    }

    @GetMapping("/vehicleRequest/{id}")
    public ResponseEntity<?> vehicleRequest(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<VehicleRequestDto>(vehicleRequestService.getVehicleRequest(id), HttpStatus.OK);
    }

    @DeleteMapping("/vehicleRequest/{id}")
    public ResponseEntity<?> deleteVehicleRequest(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleRequestService.deleteVehicleRequest(id);
        return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
    }

    @PostMapping("/addOrEdit/vehicleRequest")
    public ResponseEntity<?> addOrEditVehicleRequest(@RequestBody VehicleRequestDto expenseDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String message = "Vehicle Request updated successfully!!";
        if (  expenseDto.getId() == null || expenseDto.getId() == 0) {
            vehicleRequestService.saveVehicleRequest(expenseDto, user.get());
            message = "Vehicle Request save successfully!!!";
        } else
            vehicleRequestService.saveVehicleRequest(expenseDto, user.get());
        return new ResponseEntity<Status>(new Status("200", message), HttpStatus.OK);
    }

    @GetMapping("/vehicleRequest/vehicles/{id}")
    public ResponseEntity<?> getVehicleRequestVehicles(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<List<VehicleRequestVehiclesDto>>(vehicleRequestService.getVehicleRequestVehicles(id), HttpStatus.OK);
    }

    @GetMapping("/vehicleRequest/shuffleVehicles/{id}")
    public ResponseEntity<?> getVehicleRequestVehiclesForShuffle(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<List<VehicleRequestVehiclesDto>>(vehicleRequestService.getVehicleRequestVehiclesShuffle(id), HttpStatus.OK);
    }


    @PostMapping("/vehicleRequest/vehiclesAssign")
    public ResponseEntity<?> assignVehicleRequest(@RequestBody VehicleAssignDto vehicleAssignDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleRequestService.assignVehicleRequest(vehicleAssignDto,user.get());
        return new ResponseEntity<Status>(new Status("200", "Vehicle Request Allotted Successfully!"), HttpStatus.OK);
    }

    @PostMapping("/vehicleRequest/assignAdditionalDriver")
    public ResponseEntity<?> assignAdditionalDriver(@RequestBody VehicleAssignAdditionalDto vehicleAssignDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleRequestService.assignAdditionalDriver(vehicleAssignDto,user.get());
        return new ResponseEntity<Status>(new Status("200", "Additional Driver Allotted Successfully!"), HttpStatus.OK);
    }

    @PostMapping("/vehicleRequest/shuffleVehicle")
    public ResponseEntity<?> shuffleVehicle(@RequestBody VehicleAssignShuffleDto vehicleAssignDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleRequestService.shuffleVehicleRequest(vehicleAssignDto,user.get());
        return new ResponseEntity<Status>(new Status("200", "Vehicle Request Shuffled Successfully!"), HttpStatus.OK);
    }

    @GetMapping("/vehicleRequest/vehiclesAssignDetails/{reqId}")
    public ResponseEntity<?> vehiclesAssignDetails(@PathVariable Long reqId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<VehicleRequestAllotmentDto>(vehicleRequestService.getVehicleRequestAllotmentDetails(reqId), HttpStatus.OK);
    }

    @PostMapping("/vehicleRequest/vehiclesReject")
    public ResponseEntity<?> vehiclesReject(@RequestBody VehicleAssignDto vehicleAssignDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleRequestService.vehicleReject(vehicleAssignDto.getVehicleRequestId(),user.get());
        return new ResponseEntity<Status>(new Status("200", "Vehicle Request Rejected Successfully!"), HttpStatus.OK);
    }




}
