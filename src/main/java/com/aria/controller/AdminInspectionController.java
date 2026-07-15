package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.AdminInspectionDto;
import com.aria.model.User;
import com.aria.service.AdminInspectionService;

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
public class AdminInspectionController {

    @Autowired
    AdminInspectionService adminInspectionService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveAdminInspection")
    public ResponseEntity<?> saveAdminInspection(@RequestBody AdminInspectionDto adminInspectionDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return adminInspectionService.saveAdminInspection(adminInspectionDto, user.get());
    }

    @PutMapping("/updateAdminInspection")
    public ResponseEntity<?> updateAdminInspection(@RequestBody AdminInspectionDto adminInspectionDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return adminInspectionService.updateAdminInspection(adminInspectionDto, user.get());
    }

    @GetMapping("/getAdminInspectionList")
    public ResponseEntity<?> getAdminInspection(
            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return adminInspectionService.listAll(pageable);
        } else {
            return new ResponseEntity<List<AdminInspectionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getAdminInspection/{id}")
    public ResponseEntity<?> getAdminInspection(@PathVariable String id, Authentication authentication) {
        return adminInspectionService.getAdminInspection(id);
    }

    @DeleteMapping("/deleteAdminInspection/{id}")
    public ResponseEntity<?> deleteAdminInspection(@PathVariable String id, Authentication authentication) {
        return adminInspectionService.deleteAdminInspection(id);
    }
}
