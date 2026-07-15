package com.aria.controller;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class VerifyController {


    @Autowired
    VerifyService verifyService;

    @Autowired
    UserRepository userRepository;


    @PutMapping("/plantManagerVerify")
    public ResponseEntity<?> plantManagerVerify(@RequestBody SearchDto searchDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return verifyService.updatePlantVerify(searchDto, user.get());
    }

    @PutMapping("/dgmVerify")
    public ResponseEntity<?> dgmVerify(@RequestBody SearchDto searchDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return verifyService.updateDGMVerify(searchDto, user.get());
    }

    @PutMapping("/mdVerify")
    public ResponseEntity<?> mdVerify(@RequestBody SearchDto searchDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return verifyService.updateMDVerify(searchDto, user.get());
    }



}
