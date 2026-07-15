
package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.WelfareRegistrationDto;
import com.aria.model.User;
import com.aria.service.WelfareRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
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
public class WelfareRegistrationController {

    @Autowired
    WelfareRegistrationService welfareRegistrationService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveWelfareRegistration")
    public ResponseEntity<?> saveWelfareRegistration(@RequestBody WelfareRegistrationDto welfareRegistrationDto,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return welfareRegistrationService.saveWelfareRegistration(welfareRegistrationDto, user.get());
    }

    @PutMapping("/updateWelfareRegistration")
    public ResponseEntity<?> updateWelfareRegistration(@RequestBody WelfareRegistrationDto welfareRegistrationDto,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return welfareRegistrationService.updateWelfareRegistration(welfareRegistrationDto, user.get());
    }

    @GetMapping("/getWelfareRegistrationList")
    public ResponseEntity<?> getWelfareRegistrationList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return welfareRegistrationService.listAllwelfareRegistration();
        } else {
            return new ResponseEntity<List<WelfareRegistrationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getWelfareRegistrationDetails/{id}")
    public ResponseEntity<?> getPageDetails(@PathVariable String id, Authentication authentication) {

        return welfareRegistrationService.getWelfareRegistrationDetails(id);
    }

    @DeleteMapping("/deleteWelfareRegistrationDetails/{id}")
    public ResponseEntity<?> deleteWelfareRegistrationDetails(@PathVariable String id, Authentication authentication) {

        return welfareRegistrationService.deleteWelfareRegistration(id);
    }
}
