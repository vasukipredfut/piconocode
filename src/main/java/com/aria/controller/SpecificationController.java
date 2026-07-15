package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import com.aria.dao.UserRepository;
import com.aria.dto.SpecificationDetailsDto;
import com.aria.dto.SpecificationDto;
import com.aria.dto.SpecificationParameterDTO;
import com.aria.model.User;
import com.aria.service.SpecificationService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SpecificationController {

    @Autowired
    SpecificationService specService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/specification")
    public ResponseEntity<?> saveSpecification(@RequestBody SpecificationDto reqDto,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return specService.saveSpecification(reqDto, user.get());
    }

    @GetMapping("/specification")
    public ResponseEntity<?> getAllSpecificationDetails(Pageable pageable, Authentication authentication,
                                                        @RequestParam(value = "name", required = false) String name,
                                                        @RequestParam(value = "specType", required = false) Long specType,
                                                        @RequestParam(value = "fromDate", required = false) String fromDate,
                                                        @RequestParam(value = "toDate", required = false) String toDate ) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return specService.listAllSpecificationDetails(user.get(), pageable, name, specType, fromDate, toDate);
        } else {
            return new ResponseEntity<List<SpecificationDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/listspecification")
    public ResponseEntity<?> getlistspecification(Pageable pageable, Authentication authentication,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "specType", required = false) Long specType,
                                                  @RequestParam(value = "fromDate", required = false) String fromDate,
                                                  @RequestParam(value = "toDate", required = false) String toDate,
                                                  @RequestParam(value = "standingOrder", required = false) String standingOrder,
                                                  @RequestParam(value = "standingOrderAuth", required = false) String standingOrderAuth) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return specService.listAllSpecificationDetails(user.get(), name, specType, fromDate, toDate, standingOrder, standingOrderAuth );
        } else {
            return new ResponseEntity<List<SpecificationDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getSpecparameters")
    public ResponseEntity<?> getAllSpecificationParameters(Authentication authentication,
                                                           @RequestParam(value = "specType", required = false) Long specType) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return specService.listAllSpecificationParameters(user.get(), specType);
        } else {
            return new ResponseEntity<List<SpecificationParameterDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/specificationbyid")
    public ResponseEntity<?> getSpecificationDetails(@RequestParam(value = "specType", required = false) Long specType, @RequestParam(value = "id", required = false) Long id, Authentication authentication) {
        return specService.getSpecificationDetails(id, specType);
    }

    @DeleteMapping("/specification/{id}")
    public ResponseEntity<?> deleteSpecificationDetails(@PathVariable Long id, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return specService.deleteSpecification(user.get(), id);
    }


    @GetMapping("/specificationListWithType")
    public ResponseEntity<?> specificationListWithType(Pageable pageable, Authentication authentication, @RequestParam(value = "page", required = false) Boolean pageFlag,
                                                       @RequestParam(value = "search", required = false) String search, @RequestParam(value = "specType") Long specType) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return specService.specificationListWithType(user.get(), pageFlag, search, specType, pageable);
        } else {
            return new ResponseEntity<List<SpecificationDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
