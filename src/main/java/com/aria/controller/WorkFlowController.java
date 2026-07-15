package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.FlowDto;
import com.aria.dto.WorkFlowDto;
import com.aria.model.User;
import com.aria.service.WorkFlowService;
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
public class WorkFlowController {

    @Autowired
    WorkFlowService workFlowService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveWorkFlow")
    public ResponseEntity<?> saveWorkFlow(@RequestBody WorkFlowDto workFlowDto,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return workFlowService.saveWorkFlow(workFlowDto, user.get());
    }

    @PutMapping("/updateFlow")
    public ResponseEntity<?> updateFlow(@RequestBody FlowDto flowDto,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return workFlowService.upateFlow(flowDto, user.get());
    }


    @PutMapping("/updateWorkFlow")
    public ResponseEntity<?> updateWorkFlow(@RequestBody WorkFlowDto welfareRegistrationDto,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return workFlowService.updateWorkFlow(welfareRegistrationDto, user.get());
    }


    @GetMapping("/getWorkFlowList")
    public ResponseEntity<?> listAllWorkFlow(Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return workFlowService.listAllWorkFlow(pageable);
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    @GetMapping("/getWorkFlowDetails/{id}")
    public ResponseEntity<?> getWorkFlowDetails(@PathVariable String id, Authentication authentication) {

        return workFlowService.getWorkFlowDetails(id);
    }

    @DeleteMapping("/deleteWorkFlowDetails/{id}")
    public ResponseEntity<?> deleteWorkFlowDetails(@PathVariable String id, Authentication authentication) {

        return workFlowService.deleteWorkFlowDetails(id);
    }


    @GetMapping("/getWorkFlowDetailsList")
    public ResponseEntity<?> getWorkFlowDetailsList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return workFlowService.listAllWorkFlowDetailsMap();
        } else {
            return new ResponseEntity<List<WorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
