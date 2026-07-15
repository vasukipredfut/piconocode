package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.User;
import com.aria.service.ASRSProductionService;
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
public class ASRSController {


    @Autowired
    ASRSProductionService asrsProductionService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/getASRSProductionEntryFilterList")
    public ResponseEntity<?> getASRSProductionEntryFilterList(@RequestParam(value = "journalName", required = false) String journalName,
                                                              @RequestParam(value = "productName", required = false) String productName,
                                                              @RequestParam(value = "journalId", required = false) Long journalId,
                                                              @RequestParam(value = "firmId", required = false) Long firmId,
                                                              @RequestParam(value = "branch", required = false) Long branch,
                                                              @RequestParam(value = "dept", required = false) Long dept,
                                                              @RequestParam(value = "status", required = false) String status,
                                                              @RequestParam(value = "trnNo", required = false) String trnNo,
                                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                              Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return asrsProductionService.getASRSProductionEntryFilterList(branch, dept, journalId, journalName, productName, status, trnNo, shiftDate, pageable);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/saveASRSProduction")
    public ResponseEntity<?> saveASRSProduction(@RequestBody ASRSProductionDTO productionDetails, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return asrsProductionService.saveASRSProduction(productionDetails, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PutMapping("/updateASRSProduction")
    public ResponseEntity<?> updateASRSProduction(@RequestBody ASRSProductionDTO productionDetails, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return asrsProductionService.updateASRSProduction(productionDetails, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getASRSProduction")
    public ResponseEntity<?> getASRSProduction(@RequestParam("asrsId") Long asrsId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return asrsProductionService.getASRSProduction(asrsId, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // 26-08-2025 created by dhivya (ASRSAmendment save API ) discussed by alaguraja sir
    @PostMapping("/saveAmendmentASRSProduction")
    public ResponseEntity<?> saveAmendmentASRSProduction(@RequestBody ASRSProductionDTO amendmentRequest, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return asrsProductionService.saveAmendmentASRSProduction(amendmentRequest, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
   

}
