package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.VendorDto;
import com.aria.dto.VendorSisterConcernMapDto;
import com.aria.model.User;
import com.aria.service.VendorSisterConcernService;
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
public class VendorSisterConcernController {


    @Autowired
    VendorSisterConcernService sisterConcernService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveVendorSisterConcern")
    public ResponseEntity<?> saveVendorSisterConcern(@RequestBody VendorSisterConcernMapDto vendorDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return sisterConcernService.saveVendorSisterConcern(vendorDto, user.get());
    }

    @PutMapping("/updateVendorSisterConcern")
    public ResponseEntity<?> updateVendor(@RequestBody VendorSisterConcernMapDto vendorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return sisterConcernService.updateVendorSisterConcern(vendorDto, user.get());
    }


    @GetMapping("/listAllVendorSisterConcern")
    public ResponseEntity<?> listAllVendorSisterConcern(Authentication authentication, Pageable pageable) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return sisterConcernService.listAllVendor(user.get(),pageable);
        } else {
            return new ResponseEntity<List<VendorSisterConcernMapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
