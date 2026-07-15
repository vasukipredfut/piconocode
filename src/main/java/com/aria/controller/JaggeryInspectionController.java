package com.aria.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.aria.dao.UserRepository;
import com.aria.dto.JaggeryAttributesDto;
import com.aria.dto.JaggeryInspectionReqDto;
import com.aria.dto.JaggeryInspectionSubmitReqDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.JaggeryInspectionService;
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
public class JaggeryInspectionController {

    @Autowired
    JaggeryInspectionService jaggeryInspectionService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/getJaggeryAttributes")
    public ResponseEntity<?> getJaggeryAttributes(@RequestBody JaggeryInspectionSubmitReqDto submitReqDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return jaggeryInspectionService.getJaggeryAttributes(submitReqDto);

    }

    @PostMapping("/saveJaggeryInspection")
    public ResponseEntity<?> saveJaggeryInspection(@RequestBody JaggeryInspectionReqDto jaggeryInspectionReqDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return jaggeryInspectionService.saveJaggeryInspection(jaggeryInspectionReqDto, user.get());
        } else {
            return new ResponseEntity<List<JaggeryAttributesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/jaggeryInspectionList")
    public ResponseEntity<?> jaggeryInspectionList(@RequestParam(value = "vendorId", required = false) Long vendorId,
                                                   @RequestParam(value = "vendorName", required = false) String vendorName,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "trnNo", required = false) String trnNo,
                                                   @RequestParam(value = "billNo", required = false) String billNo,
                                                   @RequestParam(value = "billDate", required = false) String billDate,
                                                   @RequestParam(value = "jagDate", required = false) String jagDate, Pageable pageable,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return jaggeryInspectionService.findAll( vendorId, vendorName, billNo, billDate, jagDate, status , trnNo , pageable);
        } else {
            return new ResponseEntity<List<JaggeryAttributesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/jaggeryInspection/{id}")
    public ResponseEntity<?> findAll(@PathVariable(value = "id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return jaggeryInspectionService.findBy(id);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/approveAndRejectJaggeryInspection/{id}")
    public ResponseEntity<?> approveAndRejectJaggeryInspection(@PathVariable(value = "id") Long id,
                                                               @RequestParam String action, Authentication authentication) throws JsonProcessingException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return jaggeryInspectionService.approveAndRejectJaggeryInspection(id, action, user);
        } else {
            return new ResponseEntity<Status>(new Status("200", "Jaggery inspection updated successfully!!!"),
                    HttpStatus.OK);
        }
    }

    @PutMapping("/updateJaggeryInspection")
    public ResponseEntity<?> updateJaggeryInspection(@RequestBody JaggeryInspectionReqDto jaggeryInspectionReqDto,
                                                     Authentication authentication) throws JsonProcessingException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return jaggeryInspectionService.updateJaggeryInspectionStatus(jaggeryInspectionReqDto, authentication,
                    user);
        } else {
            return new ResponseEntity<Status>(new Status("200", "Jaggery inspection updated successfully!!!"),
                    HttpStatus.OK);
        }
    }

    @PostMapping("/getAttributesList")
    public ResponseEntity<?> getAttributesList(@RequestBody JaggeryInspectionSubmitReqDto submitReqDto,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return jaggeryInspectionService.getAttributesList(submitReqDto);

    }

}
