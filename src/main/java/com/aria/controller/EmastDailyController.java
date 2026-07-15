package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.dto.EmastSingleUserResponse;
import com.aria.dto.EpayMasterAndDepayMasterDto;
import com.aria.model.EmastDaily;
import com.aria.model.User;
import com.aria.service.EmastDailyService;

@RestController
@CrossOrigin
public class EmastDailyController {

    @Autowired
    private EmastDailyService emastDailyService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    /*@GetMapping("/api/salaryStructureList")
    public ResponseEntity<?> salaryStructureList(@RequestParam("employeeType") String employeeType, Pageable pageable, Authentication authentication) {
        EpayMasterAndDepayMasterDto epayMasterAndDepayMasterDto = emastDailyService.getAllSalaryEMployeeStructureList(employeeType, pageable);
        return new ResponseEntity<EpayMasterAndDepayMasterDto>(epayMasterAndDepayMasterDto, HttpStatus.OK);
    }*/

    @PostMapping("/api/emastCreateUser")
    public ResponseEntity<?> createUser(@RequestBody EmastDaily userdto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return emastDailyService.createUser(userdto);
    }

    @GetMapping("/api/allEmastEmployee")
    public ResponseEntity<?> allDepayMasterEmployee(Pageable page, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            try {
                return emastDailyService.allEmastEmployee(page);
            } catch (Exception e) {
                return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else
            return new ResponseEntity<Object>("Invalid User...", HttpStatus.OK);
    }

    @GetMapping("/api/getEmastEmployee/{empId}")
    public ResponseEntity<?> allDepayMasterEmployee(@PathVariable("empId") Long employeeId,
                                                    Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        try {
            EmastSingleUserResponse employee = emastDailyService.singleEmastEmployee(employeeId);
            if (employee != null)
                return new ResponseEntity<Object>(employee, HttpStatus.OK);
            return new ResponseEntity<Object>("user Not Found..", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/api/deleteEmast")
    public ResponseEntity<?> deleteEmast(@RequestParam("empId") Long employeeId,
                                              Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        try {
            if (emastDailyService.deleteEmast(employeeId))
                return new ResponseEntity<Object>("Employee Deleted successfully...", HttpStatus.OK);
            return new ResponseEntity<Object>("Employee Not Deleted ...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }	

    @PutMapping("/api/updateEmast")
    public ResponseEntity<?> updateUser(@RequestBody EmastDaily userdto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return emastDailyService.updateUser(userdto);
    }

    @RequestMapping(value = "/api/emastFilterList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> filterListUser(@RequestParam(value = "employeeId", required = false) Long employeeId,
                                            @RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "departmentId", required = false) Long departmentId,
                                            @RequestParam(value = "employeeName", required = false) String employeeName,
                                            @RequestParam(value = "employeeCategory", required = false) String employeeType,
                                            @RequestParam(value = "designationId", required = false) Long designationId,
                                            @RequestParam(value = "designation", required = false) String designation,
                                            @RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate, Pageable pageable, Authentication authentication) {

        if (pageable == null) {
            pageable = PageRequest.of(0, 500);
        }
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return emastDailyService.filterListUser(employeeId, branchId, departmentId, employeeName,  status, designationId, designation, startDate, endDate,  pageable);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/commonEmastStaffFilter", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> commonEmastStaffFilter(@RequestParam(value = "branchId") Long branchId,
                                                   @RequestParam(value = "employeeId", required = false) Long employeeId,
                                                   @RequestParam(value = "dataCatId", required = false) Long dataCatId,
                                                   @RequestParam(value = "departmentId", required = false) Long departmentId,
                                                   @RequestParam(value = "employeeName", required = false) String employeeName,
                                                   @RequestParam(value = "employeeType", required = false) String employeeType,
                                                   @RequestParam(value = "weekOff", required = false) String weekOff,
                                                   Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return emastDailyService.commonEmastStaffFilter(employeeId, branchId, departmentId, employeeName, employeeType, weekOff, dataCatId);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/api/allContractLabour")
    public ResponseEntity<?> allContractLabour(@RequestParam(value = "name", required = false) String name, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
                return emastDailyService.allContractLabour(name);
        } else
            return new ResponseEntity<Object>("Invalid User...", HttpStatus.OK);
    }


    @RequestMapping(value = "/api/emastQualificationList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> emastQualificationList(Authentication authentication,
                                               @RequestParam(value = "qualification", required = false) String qualification) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return emastDailyService.findAllByQualification(qualification);
    }
}
