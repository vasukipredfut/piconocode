package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.IntraBranchReceiptDto;
import com.aria.dto.WithInBranchDistributionDto;
import com.aria.model.DepartmentDetails;
import com.aria.model.User;
import com.aria.service.WithInBranchDistributionService;
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
public class WithInBranchDistributionController {

    @Autowired
    WithInBranchDistributionService withInBranchDistributionService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveWithInBranchDistribution")
    public ResponseEntity<?> saveWithInBranchDistribution(@RequestBody WithInBranchDistributionDto withInBranchDistributionDto,
                                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return withInBranchDistributionService.updateWithInBranchDistribution(withInBranchDistributionDto, user.get());
    }

    @PutMapping("/updateWithInBranchDistribution")
    public ResponseEntity<?> updateWithInBranchDistribution(@RequestBody WithInBranchDistributionDto withInBranchDistributionDto,
                                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return withInBranchDistributionService.updateWithInBranchDistribution(withInBranchDistributionDto, user.get());
    }

    @GetMapping("/getWithInBranchDistributionList")
    public ResponseEntity<?> getWithInBranchDistributionList(@RequestParam(value = "firmId", required = false) Long firmId,
                                                             @RequestParam(value = "branch", required = false) Long branch,
                                                             @RequestParam(value = "dept", required = false) Long dept,
                                                             @RequestParam(value = "hupId", required = false) String hupId,
                                                             @RequestParam(value = "dktNo", required = false) String dktNo,
                                                             @RequestParam(value = "requestDeptName", required = false) String requestDeptName,
                                                             @RequestParam(value = "issueBranchName", required = false) String issueBranchName,
                                                             @RequestParam(value = "fromDate", required = false) String fromDate,
                                                             @RequestParam(value = "toDate", required = false) String toDate,
                                                             @RequestParam(value = "status", required = false) String status,
                                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {

            return withInBranchDistributionService.filterListAll(firmId, branch, dept , hupId, dktNo, requestDeptName, issueBranchName, fromDate, toDate, status, pageable);

        } else {
            return new ResponseEntity<List<IntraBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getWithInBranchDistribution/{id}")
    public ResponseEntity<?> getWithInBranchDistribution(@PathVariable String id, Authentication authentication) {
        return withInBranchDistributionService.getWithInBranchDistribution(id);
    }

    @DeleteMapping("/deleteWithInBranchDistribution/{id}")
    public ResponseEntity<?> deleteWithInBranchDistribution(@PathVariable String id, Authentication authentication) {
        return withInBranchDistributionService.deleteWithInBranchDistribution(id);
    }
}