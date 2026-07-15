package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.LeaveDetailsDto;
import com.aria.dto.MedicalCheckupDto;
import com.aria.dto.PageDataDto;
import com.aria.model.User;
import com.aria.service.MedicalCheckupService;
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
public class MedicalCheckupController {


    @Autowired
    MedicalCheckupService medicalCheckupService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveMedicalCheckup")
    public ResponseEntity<?> saveMedicalCheckup(@RequestBody MedicalCheckupDto medicalCheckupDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return medicalCheckupService.saveMedicalCheckup(medicalCheckupDto, user.get());
    }

    @PutMapping("/updateMedicalCheckup")
    public ResponseEntity<?> updateMedicalCheckup(@RequestBody MedicalCheckupDto medicalCheckupDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return medicalCheckupService.updateMedicalCheckup(medicalCheckupDto, user.get());
    }

    @GetMapping("/getMedicalCheckupList")
    public ResponseEntity<?> getMedicalCheckupList(@RequestParam(value = "employeeName", required = false) String employeeName,
                                                   @RequestParam(value = "firm", required = false) String firm,
                                                   @RequestParam(value = "branch", required = false) String branch,
                                                   @RequestParam(value = "deptName", required = false) String deptName,
                                                   @RequestParam(value = "category", required = false) String category,
                                                   @RequestParam(value = "fromDate", required = false) String fromDate,
                                                   @RequestParam(value = "toDate", required = false) String toDate,
            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return medicalCheckupService.listAllMedicalCheckup(employeeName, firm , branch , deptName, category , fromDate , toDate ,pageable);
        } else {
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getMedicalCheckupDetails/{id}")
    public ResponseEntity<?> getMedicalCheckupDetails(@PathVariable String id, Authentication authentication) {

        return medicalCheckupService.getMedicalCheckupDetails(id);
    }

    @DeleteMapping("/deleteMedicalCheckupDetails/{id}")
    public ResponseEntity<?> deleteMedicalCheckupDetails(@PathVariable String id, Authentication authentication) {

        return medicalCheckupService.deleteMedicalCheckup(id);
    }

}
