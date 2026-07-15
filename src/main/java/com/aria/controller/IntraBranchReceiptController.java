
package com.aria.controller;

import com.aria.dao.UserRepository;

import com.aria.dto.IntraBranchReceiptDto;
import com.aria.model.DepartmentDetails;
import com.aria.model.User;

import com.aria.service.IntraBranchReceiptService;

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
public class IntraBranchReceiptController {

    @Autowired
    IntraBranchReceiptService intraBranchReceiptService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveIntraBranchReceipt")
    public ResponseEntity<?> saveIntraBranchReceipt(@RequestBody IntraBranchReceiptDto intraBranchReceiptDto,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return intraBranchReceiptService.updateIntraBranchReceipt(intraBranchReceiptDto, user.get());
    }

    @PutMapping("/updateIntraBranchReceipt")
    public ResponseEntity<?> updateIntraBranchReceipt(@RequestBody IntraBranchReceiptDto intraBranchReceiptDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return intraBranchReceiptService.updateIntraBranchReceipt(intraBranchReceiptDto, user.get());
    }

    @GetMapping("/getIntraBranchReceiptList")
    public ResponseEntity<?> getIntraBranchReceiptList(@RequestParam(value = "firmId", required = false) Long firmId,
                                                       @RequestParam(value = "branch", required = false) Long branch,
                                                       @RequestParam(value = "deptId", required = false) Long deptId,
                                                       @RequestParam(value = "hupId", required = false) String hupId,
                                                       @RequestParam(value = "dktNo", required = false) String dktNo,
                                                       @RequestParam(value = "issueBranchName", required = false) String issueBranchName,
                                                       @RequestParam(value = "dispatchOn", required = false) String dispatchOn,
                                                       @RequestParam(value = "fromDate", required = false) String fromDate,
                                                       @RequestParam(value = "toDate", required = false) String toDate,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return intraBranchReceiptService.filterListAll(firmId, branch, deptId, hupId, dktNo, issueBranchName, dispatchOn, status, pageable, fromDate, toDate);

        } else {
            return new ResponseEntity<List<IntraBranchReceiptDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getIntraBranchReceipt/{id}")
    public ResponseEntity<?> getIntraBranchReceipt(@PathVariable Long id, Authentication authentication) {
        return intraBranchReceiptService.getIntraBranchReceipt(id);
    }

    @DeleteMapping("/deleteIntraBranchReceipt/{id}")
    public ResponseEntity<?> deleteIntraBranchReceipt(@PathVariable String id, Authentication authentication) {
        return intraBranchReceiptService.deleteIntraBranchReceipt(id);
    }
}
