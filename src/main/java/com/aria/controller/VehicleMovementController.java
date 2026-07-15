package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.VehicleMovementService;
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
@RequestMapping("/api/vehicleMovement")
@CrossOrigin
public class VehicleMovementController {


    @Autowired
    UserRepository userRepository;

    @Autowired
    VehicleMovementService vehicleMovementService;


    @GetMapping("")
    public ResponseEntity<?> getVehicleMovementList(Pageable pageable, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return vehicleMovementService.getAllVehicleMovement(pageable);
    }

    @PostMapping("/begun")
    public ResponseEntity<?> begunVehicleMovement(@RequestBody VehicleMovementBegunDto vehicleMovementBegunDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleMovementService.begunVehicleMovement(vehicleMovementBegunDto,user.get());
        return new ResponseEntity<Status>(new Status("200", ""), HttpStatus.OK);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endVehicleMovement(@RequestBody VehicleMovementEndDto vehicleMovementEndDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleMovementService.endVehicleMovement(vehicleMovementEndDto,user.get());
        return new ResponseEntity<Status>(new Status("200", ""), HttpStatus.OK);
    }

    @PostMapping("/upKm")
    public ResponseEntity<?> upKmVehicleMovement(@RequestBody VehicleMovementUpKmDto vehicleMovementBegunDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        vehicleMovementService.vehicleMovementUpKm(vehicleMovementBegunDto,user.get());
        return new ResponseEntity<Status>(new Status("200", ""), HttpStatus.OK);
    }

    @GetMapping("/vanBoys/{allotmentId}")
    public ResponseEntity<?> vanBoys(@PathVariable Long allotmentId, Authentication authentication)  {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<List<VehicleVanBoyDto>>(vehicleMovementService.getVanBoys(allotmentId), HttpStatus.OK);
    }

    @GetMapping("/refType/{allotmentId}")
    public ResponseEntity<?> refType(@PathVariable Long allotmentId, Authentication authentication)  {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<List<VehicleRefTyeDto>>(vehicleMovementService.getRefType(allotmentId), HttpStatus.OK);
    }

}
