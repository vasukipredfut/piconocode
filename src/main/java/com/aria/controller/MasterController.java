package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.MasterComponentDto;
import com.aria.model.User;
import com.aria.service.MasterService;
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
public class MasterController {


    @Autowired
    MasterService masterService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveComponent")
    public ResponseEntity<?> saveComponent(@RequestBody MasterComponentDto componentDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return masterService.saveComponent(componentDto, user.get());
    }

    @PutMapping("/updateComponent")
    public ResponseEntity<?> updateComponent(@RequestBody MasterComponentDto componentDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return masterService.updateComponent(componentDto, user.get());
    }

    @GetMapping("/getComponentList")
    public ResponseEntity<?> listComponent(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return masterService.listAllComponent(pageable);
        } else {
            return new ResponseEntity<List<MasterComponentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getComponentDetails/{id}")
    public ResponseEntity<?> UserProfile(@PathVariable String id, Authentication authentication) {

        return masterService.getMasterComponentDetails(id);
    }

    @DeleteMapping("/deleteComponentDetails/{id}")
    public ResponseEntity<?> deleteComponentDetails(@PathVariable String id, Authentication authentication) {

        return masterService.deleteComponent(id);
    }

}
