package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.FumigationDto;
import com.aria.model.User;
import com.aria.service.FumigationService;
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
public class FumigationController {

    @Autowired
    FumigationService fumigationService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveFumigation")
    public ResponseEntity<?> saveFumigation(@RequestBody FumigationDto fumigationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return fumigationService.saveFumigation(fumigationDto, user.get());
    }

    @PutMapping("/updateFumigation")
    public ResponseEntity<?> updateFumigation(@RequestBody FumigationDto fumigationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return fumigationService.updateFumigation(fumigationDto, user.get());
    }

    @GetMapping("/getFumigationList")
    public ResponseEntity<?> getFumigationList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return fumigationService.listAll(pageable);
        } else {
            return new ResponseEntity<List<FumigationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getFumigationDetails/{id}")
    public ResponseEntity<?> getFumigationDetails(@PathVariable String id, Authentication authentication) {

        return fumigationService.getFumigation(id);
    }

    @DeleteMapping("/deleteFumigationDetails/{id}")
    public ResponseEntity<?> deleteFumigationDetails(@PathVariable String id, Authentication authentication) {

        return fumigationService.deleteFumigation(id);
    }

    @GetMapping("/getFumigationSchedulerList")
    public ResponseEntity<?> getFumigationSchedulerList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return fumigationService.listAll();
        } else {
            return new ResponseEntity<List<FumigationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


}
