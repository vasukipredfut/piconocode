

package com.aria.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.aria.dao.UserRepository;
import com.aria.dto.MaterialIssueDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.MaterialIssueService;
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
public class MaterialIssueController {

    @Autowired
    MaterialIssueService materialIssueService;

    @Autowired
    UserRepository userRepository;


    @PutMapping("/updateMaterialIssue")
    public ResponseEntity<?> updateMaterialIssue(@RequestBody MaterialIssueDto materialIssueDto, Authentication authentication) throws JsonProcessingException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return materialIssueService.updateMaterialIssue(materialIssueDto, user.get());
    }

    @PutMapping("/issuePendingRequest")
    public ResponseEntity<?> issuePendingRequest(@RequestBody MaterialIssueDto materialIssueDto, Authentication authentication) throws JsonProcessingException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return materialIssueService.issuePendingRequest(materialIssueDto, user.get());
    }

    @PutMapping("/rejectMaterialIssue")
    public ResponseEntity<?> rejectMaterialIssue(@RequestBody MaterialIssueDto materialIssueDto, Authentication authentication) throws JsonProcessingException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        materialIssueService.rejectMaterialIssue(materialIssueDto.getId());

        return new ResponseEntity<Status>(new Status("200", "material issue  rejected  successfully!!!"), HttpStatus.OK);
    }

    @GetMapping("/getMaterialIssueList")
    public ResponseEntity<?> getMaterialIssueList(@RequestParam(value = "firmId", required = false) Long firmId,
                                                  @RequestParam(value = "branch", required = false) Long branch,
                                                  @RequestParam(value = "deptId", required = false) Long deptId,
                                                  @RequestParam(value = "requestId", required = false) Long requestId,
                                                  @RequestParam(value = "itemName", required = false) String itemName,
                                                  @RequestParam(value = "branchName", required = false) String branchName,
                                                  @RequestParam(value = "deptName", required = false) String deptName,
                                                  @RequestParam(value = "fromDate", required = false) String fromDate,
                                                  @RequestParam(value = "toDate", required = false) String toDate,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialIssueService.filterListAll(firmId , branch, deptId, requestId, itemName, branchName, deptName, fromDate, toDate, status, pageable);
        } else {
            return new ResponseEntity<List<MaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getMaterialIssue/{id}")
    public ResponseEntity<?> getMaterialIssue(@PathVariable String id, Authentication authentication) {
        return materialIssueService.getMaterialIssue(id);
    }

    @DeleteMapping("/deleteMaterialIssue/{id}")
    public ResponseEntity<?> deleteMaterialIssue(@PathVariable String id, Authentication authentication) {
        return materialIssueService.deleteMaterialIssue(id);
    }

    @GetMapping("/mrnPendingList")
    public ResponseEntity<?> mrnPendingList(@RequestParam(value = "requestBranchId", required = false) Long requestBranchId,
                                            @RequestParam(value = "requestBranchName", required = false) String requestBranchName,
                                            @RequestParam(value = "issueBranchId", required = false) Long issueBranchId,
                                            @RequestParam(value = "issueBranchName", required = false) String issueBranchName,
                                            @RequestParam(value = "trnDate", required = false) String trnDate,
                                            @RequestParam(value = "trnNo", required = false) String trnNo,
                                            @RequestParam(value = "productName", required = false) String productName,
                                            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialIssueService.mrnPendingList(requestBranchId, requestBranchName, issueBranchId, issueBranchName, trnDate , trnNo, productName, pageable);
        } else {
            return new ResponseEntity<List<MaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/validateDayEndCloseReceiveBranch")
    public ResponseEntity<?> validateDayEndCloseReceiveBranch(@RequestParam(value = "requestBranchId", required = false) Long requestBranchId,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return materialIssueService.validateDayEndCloseReceiveBranch(requestBranchId, user.get());
        } else {
            return new ResponseEntity<List<MaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
