package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.ShiftAllotmentDto;
import com.aria.dto.ShiftPeriodDTO;
import com.aria.dto.ShiftRotationDto;
import com.aria.model.User;
import com.aria.service.ShiftRotationService;
import com.aria.service.ShitAllotmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ShiftAllotmentController {


    @Autowired
    ShitAllotmentService shitAllotmentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShiftRotationService shiftRotationService;

    @PostMapping("/saveShiftAllotment")
    public ResponseEntity<?> saveShiftAllotment(@RequestBody ShiftAllotmentDto shiftAllotmentDto, Authentication authentication) throws NoSuchAlgorithmException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shitAllotmentService.saveShift(shiftAllotmentDto, user.get());
    }

  

    @GetMapping("/getShiftAllotmentList")
    public ResponseEntity<?> getShiftAllotmentList(@RequestParam(value = "type", required = false) String type,
                                                   @RequestParam(value = "empName", required = false) String empName,
                                                   @RequestParam(value = "deptId", required = false) Long deptId,
                                                   @RequestParam(value = "shiftName", required = false) String shiftName,
                                                   Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return shitAllotmentService.listAll(type, empName, deptId, shiftName, pageable);
        } else {
            return new ResponseEntity<List<ShiftAllotmentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getShiftAllotmentDetails/{id}/{staff}/{shiftid}")
    public ResponseEntity<?> getPageDetails(@PathVariable String id, @PathVariable Boolean staff, @PathVariable String shiftid , Authentication authentication) throws UsernameNotFoundException, NoSuchAlgorithmException {

        return shitAllotmentService.getShiftDetails(id, shiftid, staff);
    }

    @DeleteMapping("/deleteShiftAllotmentDetails/{id}/{staff}/{shiftid}")
    public ResponseEntity<?> deleteShiftAllotmentDetails(@PathVariable String id, @PathVariable Boolean staff, @PathVariable String shiftid , Authentication authentication) {

        return shitAllotmentService.deleteShift(id, shiftid, staff);
    }


    // Shift Rotation

    @PostMapping("/saveShiftRotation")
    public ResponseEntity<?> saveShiftRotation(@RequestBody ShiftRotationDto shiftRotationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shiftRotationService.saveShiftRotation(shiftRotationDto, user.get());
    }

    @PutMapping("/updateShiftRotation")
    public ResponseEntity<?> updateShiftRotation(@RequestBody ShiftRotationDto shiftRotationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shiftRotationService.updateShiftRotation(shiftRotationDto, user.get());
    }

    @GetMapping("/getShiftRotationList")
    public ResponseEntity<?> getShiftRotationList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return shiftRotationService.listAll();
        } else {
            return new ResponseEntity<List<ShiftAllotmentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/filterShiftRotationList")
    public ResponseEntity<?> filterShiftRotationList(@RequestParam("branchId") Long branchId,
                                                     @RequestParam(value = "type", required = false) String type,
                                                     @RequestParam(value = "departmentId", required = false) Long departmentId,
                                                     @RequestParam(value = "employeeCategory", required = false) String employeeType,
                                                     @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                     @RequestParam(value = "dateTo", required = false) String dateTo,
                                                     Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return shiftRotationService.filterShiftRotationList(branchId, type, departmentId, employeeType, dateFrom, dateTo, pageable);

    }


    @GetMapping("/ViewShifts")
    public ResponseEntity<?> ViewShifts(@RequestParam("branchId") Long branchId,
                                        @RequestParam(value = "departmentId", required = false) Long departmentId,
                                        @RequestParam(value = "employeeCategory", required = false) String employeeCategory,
                                        @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                        @RequestParam(value = "shiftName", required = false) String shiftName,
                                        @RequestParam(value = "dateTo",required = false) String dateTo,
                                        @RequestParam(value = "emp_type", required = false) int emp_type, Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shiftRotationService.ViewShifts(employeeCategory, emp_type, branchId, departmentId, dateFrom, dateTo, shiftName);

    }

    @GetMapping("/getShiftRotationDetails/{id}")
    public ResponseEntity<?> getShiftRotationDetails(@PathVariable String id, Authentication authentication) {

        return shiftRotationService.getShiftDetails(id);
    }

    @GetMapping("/getShiftRotationViewDetails/{id}")
    public ResponseEntity<?> getShiftRotationViewDetails(@PathVariable String id, Authentication authentication) {

        return shiftRotationService.getShiftRotationViewDetails(id);
    }

    @DeleteMapping("/deleteShiftRotationDetails/{id}")
    public ResponseEntity<?> deleteShiftRotationDetails(@PathVariable String id, Authentication authentication) {

        return shiftRotationService.deleteShift(id);
    }

    @GetMapping("/filterShiftRotationUserList")
    public ResponseEntity<?> filterShiftRotationUserList(@RequestParam(value = "branchId", required = false) Long branchId,
                                                         @RequestParam(value = "departmentId", required = false) Long departmentId,
                                                         @RequestParam(value = "employeeCategory", required = false) String employeeCategory,
                                                         @RequestParam(value = "emp_type", required = false) int emp_type,
                                                         @RequestParam(value = "shiftType", required = false) String shiftType,
                                                         @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                         @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                         @RequestParam(value = "gender", required = false) String gender,
                                                         Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shiftRotationService.filterShiftRotationUserList(branchId, departmentId, employeeCategory, emp_type,  shiftType, shiftId , shiftDate, gender);

    }



    @PutMapping("/updateParticularShifts")
    public ResponseEntity<?> updateParticularShifts(@RequestBody ShiftPeriodDTO shiftPeriodDTO, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return shiftRotationService.updateParticularShifts(shiftPeriodDTO, user.get());

    }

    @RequestMapping(value = "/shiftRestrictStaffList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> shiftRestrictStaffList(@RequestParam(value = "fromDate", required = false) String fromDate,
                                                    @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                    @RequestParam(value = "empType", required = false) int empType,
                                                   Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return shiftRotationService.shiftRestrictStaffList(fromDate, shiftId , empType);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
