package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.MaterialRequestDto;
import com.aria.dto.MaterialReturnListDTO;
import com.aria.dto.OtherBranchIssueDto;
import com.aria.model.DepartmentDetails;
import com.aria.model.User;
import com.aria.service.MaterialIssueService;
import com.aria.service.OtherBranchIssueService;
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
public class OtherBranchIssueController {

    @Autowired
    OtherBranchIssueService otherBranchIssueService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MaterialIssueService materialIssueService;


    @PutMapping("/updateOtherBranchIssue")
    public ResponseEntity<?> updateOtherBranchIssue(@RequestBody OtherBranchIssueDto receiptDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return otherBranchIssueService.updateOtherBranchIssue(receiptDto, user.get());
    }
    
    
    @PutMapping("/updateOtherBranchIssueList")
    public ResponseEntity<?> updateOtherBranchIssue(@RequestBody List<OtherBranchIssueDto> receiptDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return otherBranchIssueService.ListupdateOtherBranchIssue(receiptDto, user.get());
    }

    @GetMapping("/getOtherBranchIssueList")
    public ResponseEntity<?> getOtherBranchIssueList(@RequestParam(value = "firmId", required = false) Long firmId,
                                                     @RequestParam(value = "branch", required = false) Long branch,
                                                     @RequestParam(value = "mrnNo", required = false) String mrnNo,
                                                     @RequestParam(value = "requestBranchName", required = false) String requestBranchName,
                                                     @RequestParam(value = "issueDeptName", required = false) String issueDeptName,
                                                     @RequestParam(value = "fromDate", required = false) String fromDate,
                                                     @RequestParam(value = "toDate", required = false) String toDate,
                                                     @RequestParam(value = "status", required = false) String status,
                                                     Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null ) {
            return otherBranchIssueService.filterListAll( firmId, branch, mrnNo, requestBranchName, issueDeptName, fromDate, toDate,status, pageable);
        } else {
            return new ResponseEntity<List<MaterialRequestDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getOtherBranchIssueDetails/{id}")
    public ResponseEntity<?> getOtherBranchIssueDetails(@PathVariable String id, Authentication authentication) {
        return otherBranchIssueService.getOtherBranchIssue(id);
    }

    @DeleteMapping("/deleteOtherBranchIssue/{id}")
    public ResponseEntity<?> deleteOtherBranchIssue(@PathVariable String id, Authentication authentication) {
        return otherBranchIssueService.deleteOtherBranchIssue(id);
    }

    @GetMapping("/getOtherBranchIssueItemDetails/{id}")
    public ResponseEntity<?> getMaterialIssue(@PathVariable String id, Authentication authentication) {
        return materialIssueService.getMaterialIssue(id);
    }

    @PostMapping("/verifyAndAcceptOtherBranch")
    public ResponseEntity<?> verifyAndAcceptOtherBranch(@RequestBody OtherBranchIssueDto issueDto, Authentication authentication) {
        return otherBranchIssueService.verifyAndAcceptAndRejectOtherBranchIssue(issueDto.getId(),"ACCEPTED");
    }

    @PostMapping("/rejectOtherBranch")
    public ResponseEntity<?> rejectOtherBranch(@RequestBody OtherBranchIssueDto issueDto, Authentication authentication) {
        return otherBranchIssueService.rejectOtherBranchIssue(issueDto.getId(),"REJECTED");
    }

    @GetMapping("/getOtherBranchReceiptDetail")
    public ResponseEntity<?> getOtherBranchReceiptDetail(@RequestParam(value = "otherBranchIssueId") Long otherBranchIssueId, Authentication authentication) {
        return otherBranchIssueService.getReceiptData(otherBranchIssueId);
    }


    @PutMapping("/amendmentGDC")
    public ResponseEntity<?> amendmentGDC(@RequestBody OtherBranchIssueDto otherBranchIssueDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return otherBranchIssueService.amendmentGDC(otherBranchIssueDto);
        } else {
            return new ResponseEntity<List<MaterialReturnListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
