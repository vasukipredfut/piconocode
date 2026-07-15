package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.AssignDutyDto;
import com.aria.dto.DutyAllotmentDto;
import com.aria.dto.DutyTransferDto;
import com.aria.model.User;
import com.aria.service.DutyAllotmentService;
import com.aria.service.DutyTransferService;
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
public class DutyController {

    @Autowired
    DutyAllotmentService dutyAllotmentService;

    @Autowired
    DutyTransferService dutyTransferService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveAssignDuty")
    public ResponseEntity<?> saveAssignDuty(@RequestBody DutyAllotmentDto allotmentDto,
                                            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyAllotmentService.saveDutyAllotment(allotmentDto, user.get());
    }

    @PutMapping("/updateAssignDuty")
    public ResponseEntity<?> updateAssignDuty(@RequestBody DutyAllotmentDto allotmentDto,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyAllotmentService.updateDutyAllotment(allotmentDto, user.get());
    }


    @GetMapping("/getAssignDuty/{id}")
    public ResponseEntity<?> updateAssignDuty(@PathVariable("id") Long allotmentId,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyAllotmentService.getAssignDuty(allotmentId, user.get());
    }

    @GetMapping("/getAssignDutyFilterList")
    public ResponseEntity<?> getAssignDutyFilterList(@RequestParam(value = "type", required = false) String type,
                                                     @RequestParam(value = "employeeName", required = false) String employeeName,
                                                     @RequestParam(value = "branchId", required = false) Long branchId,
                                                     @RequestParam(value = "deptId", required = false) Long deptId,
                                                     @RequestParam(value = "shiftName", required = false) String shiftName,
                                                     @RequestParam(value = "fromDate", required = false) String fromDate,
                                                     @RequestParam(value = "toDate", required = false) String toDate,
                                                     Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (user.get() != null) {

            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            if (deptId == null) {
                deptId = user.get().getDepartment().getId();
            }

            System.out.println(" branchId =  " + branchId + "   deptId ==  " + deptId);

            return dutyAllotmentService.getAssignDutyFilterList(type, user.get(), branchId, deptId, employeeName, shiftName, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<AssignDutyDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @DeleteMapping("/deleteAssignDuty/{id}/{staffId}")
    public ResponseEntity<?> deleteAssignDuty(@PathVariable Long id, @PathVariable Long staffId, Authentication authentication) {

        return dutyAllotmentService.deleteAssignDuty(id, staffId);
    }

    // Duty Transfer


    @PostMapping("/saveDutyTransfer")
    public ResponseEntity<?> saveDutyTransfer(@RequestBody DutyTransferDto dutyTransferDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyTransferService.saveDuty(dutyTransferDto, user.get());
    }

    @PutMapping("/updateDutyTransfer")
    public ResponseEntity<?> updateDutyAllotment(@RequestBody DutyTransferDto dutyTransferDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dutyTransferService.updateDuty(dutyTransferDto, user.get());
    }

    @GetMapping("/getDutyTransferAssignDutyFilterList")
    public ResponseEntity<?> getDutyTransferAssignDutyFilterList(@RequestParam(value = "type", required = false) String type,
                                                                 @RequestParam(value = "employeeName", required = false) String employeeName,
                                                                 @RequestParam(value = "assignedBranchName", required = false) String assignedBranchName,
                                                                 @RequestParam(value = "fromDepartmentName", required = false) String fromDepartmentName,
                                                                 @RequestParam(value = "toDepartmentName", required = false) String toDepartmentName,
                                                                 @RequestParam(value = "fromDate", required = false) String fromDate,
                                                                 @RequestParam(value = "toDate", required = false) String toDate,
                                                                 @RequestParam(value = "branchId", required = false) Long branchId,
                                                                 @RequestParam(value = "deptId", required = false) Long deptId,
                                                                 Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            if (deptId == null) {
                deptId = user.get().getDepartment().getId();
            }
            return dutyTransferService.getDutyTransferAssignDutyFilterList( branchId , deptId , type, employeeName, assignedBranchName, fromDepartmentName, toDepartmentName, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<DutyTransferDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getDutyTransferDetails/{id}")
    public ResponseEntity<?> getDutyTransferDetails(@PathVariable String id, Authentication authentication) {

        return dutyTransferService.getDutyDetails(id);
    }

    @GetMapping("/getDutyTransferFilterDetails/{dutyTransferId}/{toBranchId}/{toDepartmentId}")
    public ResponseEntity<?> getDutyTransferFilterDetails(@PathVariable Long dutyTransferId,
                                                          @PathVariable Long toBranchId, @PathVariable Long toDepartmentId, Pageable pageable, Authentication authentication) {
        return dutyTransferService.getDutyTransferFilterDetails(dutyTransferId, toBranchId, toDepartmentId);
    }

    @DeleteMapping("/deleteDutyTransferDetails/{id}")
    public ResponseEntity<?> deleteDutyTransferDetails(@PathVariable String id, @RequestParam("staffId") String staffId, Authentication authentication) {
        return dutyTransferService.deleteDuty(id, staffId);
    }

    @GetMapping("/getDutyAllotmentTransferFilterDetails/{allotmentId}/{toBranchId}/{toDepartmentId}")
    public ResponseEntity<?> getDutyAllotmentTransferFilterDetails(@PathVariable Long allotmentId,
                                                                   @PathVariable Long toBranchId, @PathVariable Long toDepartmentId, Authentication authentication) {
        return dutyTransferService.getDutyAllotmentTransferFilterDetails(allotmentId, toBranchId, toDepartmentId);
    }


    @GetMapping("/validationDutyAllotOrTransferList")
    public ResponseEntity<?> validationDutyAllotOrTransferList(@RequestParam("shiftDate") String shiftDate,
                                                               @RequestParam("staffType") String staffType,
                                                               Authentication authentication) {
        return dutyTransferService.validationDutyAllotOrTransferList(shiftDate, staffType);
    }
}
