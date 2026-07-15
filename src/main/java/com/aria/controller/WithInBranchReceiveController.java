package com.aria.controller;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.WithInBranchReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class WithInBranchReceiveController {

    @Autowired
    WithInBranchReceiveService withInBranchReceiveService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveWithInBranchReceive")
    public ResponseEntity<?> saveWithInBranchReceive(@RequestBody WithInBranchReceiveDto withInBranchReceiveDto,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return withInBranchReceiveService.updateWithInBranchReceive(withInBranchReceiveDto, user.get());
    }

    @PutMapping("/updateWithInBranchReceive")
    public ResponseEntity<?> updateWithInBranchReceive(@RequestBody WithInBranchReceiveDto withInBranchReceiveDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return withInBranchReceiveService.updateWithInBranchReceive(withInBranchReceiveDto, user.get());
    }

    @GetMapping("/getWithInBranchReceiveList")
    public ResponseEntity<?> getWithInBranchReceiveList(@RequestParam(value = "firmId", required = false) Long firmId,
                                                        @RequestParam(value = "branch", required = false) Long branch,
                                                        @RequestParam(value = "deptId", required = false) Long deptId,
                                                        @RequestParam(value = "mrnNo", required = false) String mrnNo,
                                                        @RequestParam(value = "itemName", required = false) String itemName,
                                                        @RequestParam(value = "requestDeptName", required = false) String requestDeptName,
                                                        @RequestParam(value = "issueDeptName", required = false) String issueDeptName,
                                                        @RequestParam(value = "fromDate", required = false) String fromDate,
                                                        @RequestParam(value = "toDate", required = false) String toDate,
                                                        @RequestParam(value = "status", required = false) String status,
            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return withInBranchReceiveService.filterListAll(firmId, branch , deptId , mrnNo, itemName, requestDeptName, issueDeptName, fromDate, toDate, status, pageable);
        } else {
            return new ResponseEntity<List<WithInBranchReceiveDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getWithInBranchReceive/{id}")
    public ResponseEntity<?> getWithInBranchReceive(@PathVariable String id, Authentication authentication) {
        return withInBranchReceiveService.getWithInBranchReceive(id);
    }

    @DeleteMapping("/deleteWithInBranchReceive/{id}")
    public ResponseEntity<?> deleteWithInBranchReceive(@PathVariable String id, Authentication authentication) {
        return withInBranchReceiveService.deleteWithInBranchReceive(id);
    }

    @GetMapping("/getWithInBranchReceiveMRNIdByDetails/{id}")
    public ResponseEntity<?> getMRNIdByDetails(@PathVariable String id, Authentication authentication) {
        List<String> ids = Arrays.asList(id.split("-"));
        return withInBranchReceiveService.getMRNDetails(ids.get(1));
    }
}
