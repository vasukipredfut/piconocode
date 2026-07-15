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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.VendorDto;
import com.aria.model.User;
import com.aria.service.VendorService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class VendorController {

    @Autowired
    VendorService vendorService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/vendor")
    public ResponseEntity<?> saveVendor(@RequestBody VendorDto vendorDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return vendorService.saveVendor(vendorDto, user.get());
    }

    @PutMapping("/vendor")
    public ResponseEntity<?> updateVendor(@RequestBody VendorDto vendorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return vendorService.updateVendor(vendorDto, user.get());
    }

    @GetMapping("/listAllVendor")
    public ResponseEntity<?> listAllVendor(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return vendorService.listAllVendor(user.get());
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/allVendorList")
    public ResponseEntity<?> listAllVendor(@RequestParam(value = "vendorName", required = false) String vendorName,
                                           @RequestParam(value = "groupId", required = false) Long groupId,
                                           @RequestParam(value = "gstNo", required = false) String gstNo,
                                           @RequestParam(value = "mobileNo", required = false) String mobileNo,
                                           Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return vendorService.listAllVendor(vendorName, groupId, gstNo, mobileNo, pageable, user.get());
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/vendor/{id}")
    public ResponseEntity<?> getVendorDetails(@PathVariable String id, Authentication authentication) {
        return vendorService.getVendorDetails(id);
    }

    @DeleteMapping("/vendor/{id}")
    public ResponseEntity<?> deleteVendorDetails(@PathVariable Long id, Authentication authentication) {
        return vendorService.deleteVendor(id);
    }

    @GetMapping("/searchVendor")
    public ResponseEntity<?> searchVendor(Authentication authentication, @RequestParam(value = "name", required = false) String name) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return vendorService.searchVendor(name);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    @GetMapping("/vendorDropDownList")
    public ResponseEntity<?> vendorDropDownList(Authentication authentication, @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "gst", required = false) String gst) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return vendorService.vendorDropDown(name, gst);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/listAllSearchVendor")
    public ResponseEntity<?> listAllSearchVendor(@RequestParam(value = "vendorName", required = false) String vendorName,
                                           Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return vendorService.listAllSearchVendor(vendorName,  user.get(), pageable);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/vendorCountry/{id}")
    public ResponseEntity<?> vendorCountry(@PathVariable String id, Authentication authentication) {
        return vendorService.vendorCountry(id);
    }

    @PostMapping("/auth/vendor")
    public ResponseEntity<?> saveAuthVendor(@RequestBody VendorDto vendorDto, HttpServletRequest request) {
        return vendorService.saveVendor(vendorDto,request);
    }
}
