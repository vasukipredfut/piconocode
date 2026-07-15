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
import org.springframework.web.bind.annotation.*;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.LeaveDetailsService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LeaveController {

    @Autowired
    LeaveDetailsService leaveService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/leaveRequest")
    public ResponseEntity<?> leaveRequest(@RequestBody LeaveDetailsDto leaveDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return leaveService.leaveRequest(leaveDetailsDto, user.get());
    }

    @PutMapping("/updateLeaveRequest")
    public ResponseEntity<?> updateLeaveRequest(@RequestBody LeaveDetailsDto leaveDetailsDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return leaveService.updateLeaveRequest(leaveDetailsDto, user.get());
    }

    @PutMapping("/leaveRejectOrApprove")
    public ResponseEntity<?> updatePage(@RequestBody LeaveDetailsDto leaveDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return leaveService.approveOrReject(leaveDetailsDto, user.get());
    }

    @GetMapping("/getLeaveRequestList")
    public ResponseEntity<?> leaveRequestList(@RequestParam(value = "employeeName", required = false) String employeeName,
                                              @RequestParam(value = "designationName") String designationName,
                                              @RequestParam(value = "roleName") String roleName,
                                              @RequestParam(value = "leaveType", required = false) String leaveType,
                                              @RequestParam(value = "leaveStatus", required = false) String leaveStatus,
                                              @RequestParam(value = "fromDate", required = false) String fromDate,
                                              @RequestParam(value = "toDate", required = false) String toDate,
                                              Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {

            System.out.println(" designation === " + designationName);
            System.out.println(" roleName === " + roleName);

            return leaveService.listAllRequest(user.get().getDepartment().getId(), employeeName, designationName, roleName, leaveType, leaveStatus, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getLeaveRequestListByUserId")
    public ResponseEntity<?> leaveRequestListByUserId(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
        if ((userOptional.get() != null) && (userOptional.get().getId() != null)) {
            return leaveService.leaveRequestListByUserId(userOptional.get().getId(), pageable);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getLeaveRequestReport")
    public ResponseEntity<?> getLeaveRequestReport(@RequestParam(value = "userId", required = false) Long userId,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
        if (userOptional.get() != null) {
            if (userId == null) {
                userId = userOptional.get().getId();
            }
            return leaveService.leaveRequestReportByUserId(userId);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getLeaveRejectOrApproveList")
    public ResponseEntity<?> leaveRejectOrApprove(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return leaveService.listAllRejectOrApprove(pageable);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PutMapping("/verifyOrApprove")
    public ResponseEntity<?> getLeaveDetails(@RequestParam(value = "id", required = true) String id,
                                             @RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "leaveType", required = false) String leaveType,
                                             @RequestParam(value = "status", required = false) String status,
                                             @RequestParam(value = "reasonForRejection", required = false) String reasonForRejection,
                                             Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
        return leaveService.verifyOrApprove(id, type, leaveType, status, reasonForRejection, userOptional.get());
    }

    @GetMapping("/getLeaveDetails/{id}")
    public ResponseEntity<?> getLeaveDetails(@PathVariable String id, Authentication authentication) {
        return leaveService.getLeaveDetails(id);
    }

    @DeleteMapping("/deleteLeaveDetails/{id}")
    public ResponseEntity<?> deleteLeaveDetails(@PathVariable String id, Authentication authentication) {
        return leaveService.deleteLeave(id);
    }


    @GetMapping("/leaveRegisterList")
    public ResponseEntity<?> leaveRegisterList(@RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "dept", required = false) Long dept,
                                               @RequestParam(value = "employeeName", required = false) String employeeName,
                                               @RequestParam(value = "empId", required = false) Long empId,
                                               @RequestParam(value = "fromDate") String fromDate,
                                               @RequestParam(value = "toDate") String toDate,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return leaveService.leaveMasterRegList(branch, dept, employeeName, empId, fromDate, toDate);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/printLeaveRegisterList")
    public ResponseEntity<?> printLeaveRegisterList(@RequestParam(value = "branch", required = false) Long branch,
                                                    @RequestParam(value = "dept", required = false) Long dept,
                                                    @RequestParam(value = "empId", required = false) Long empId,
                                                    @RequestParam(value = "fromDate") String fromDate,
                                                    @RequestParam(value = "toDate") String toDate,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return leaveService.printLeaveRegisterList(branch, dept, empId, fromDate, toDate);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/validationLeaveRequest")
    public ResponseEntity<?> validationLeaveRequest(@RequestBody LeaveDetailsDto leaveDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return leaveService.validationLeaveRequest(leaveDetailsDto, user.get());
    }

}
