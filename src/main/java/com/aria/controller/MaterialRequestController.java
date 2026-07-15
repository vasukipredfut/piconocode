package com.aria.controller;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;

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
public class MaterialRequestController {

    @Autowired
    MaterialRequestService materialRequestService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveMaterialRequest")
    public ResponseEntity<?> saveMaterialRequest(@RequestBody MaterialRequestDto materialRequestDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return materialRequestService.saveMaterialRequest(materialRequestDto, user.get());
    }

    @PutMapping("/updateMaterialRequest")
    public ResponseEntity<?> updateMaterialRequest(@RequestBody MaterialRequestDto materialRequestDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return materialRequestService.updateMaterialRequest(materialRequestDto, user.get());
    }

    @GetMapping("/getMaterialRequestList")
    public ResponseEntity<?> getMaterialRequestList(@RequestParam(value = "requestId", required = false) String requestId,
                                                    @RequestParam(value = "firmId", required = false) Long firmId,
                                                    @RequestParam(value = "branch", required = false) Long branch,
                                                    @RequestParam(value = "deptId", required = false) Long deptId,
                                                    @RequestParam(value = "description", required = false) String description,
                                                    @RequestParam(value = "branchName", required = false) String branchName,
                                                    @RequestParam(value = "deptName", required = false) String deptName,
                                                    @RequestParam(value = "requestDate", required = false) String requestDate,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    Pageable pageable , Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialRequestService.listAllMaterialRequest(firmId , branch, deptId, requestId, description, branchName, deptName, requestDate, status, pageable);
        } else {
            return new ResponseEntity<List<MaterialRequestDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getMaterialRequestDetails/{id}")
    public ResponseEntity<?> getMaterialRequestDetails(@PathVariable String id, Authentication authentication) {
        return materialRequestService.getMaterialRequest(id);
    }

    @DeleteMapping("/deleteMaterialRequest/{id}")
    public ResponseEntity<?> deleteMaterialRequest(@PathVariable String id, Authentication authentication) {
        return materialRequestService.deleteMaterial(id);
    }
}
