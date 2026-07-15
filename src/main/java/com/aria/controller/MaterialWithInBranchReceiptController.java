package com.aria.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.aria.dao.UserRepository;
import com.aria.dto.MaterialRequestDto;
import com.aria.dto.MaterialWithInBranchReceiptDto;
import com.aria.service.MaterialWithInBranchReceiptService;
import com.aria.model.User;
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
public class MaterialWithInBranchReceiptController {

    @Autowired
    MaterialWithInBranchReceiptService branchReceiptService;

    @Autowired
    UserRepository userRepository;


    @PutMapping("/updateMaterialWithInBranchReceipt")
    public ResponseEntity<?> updateMaterialWithInBranchReceipt(@RequestBody MaterialWithInBranchReceiptDto receiptDto, Authentication authentication) throws JsonProcessingException {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return branchReceiptService.updateMaterialWithInBranchReceipt(receiptDto, user.get());
    }

    @GetMapping("/getMaterialWithInBranchReceiptList")
    public ResponseEntity<?> getMaterialWithInBranchReceiptList(@RequestParam(value = "mrnNo", required = false) String mrnNo,
                                                                @RequestParam(value = "itemName", required = false) String itemName,
                                                                @RequestParam(value = "requestBranchName", required = false) String requestBranchName,
                                                                @RequestParam(value = "issueBranchName", required = false) String issueBranchName,
                                                                @RequestParam(value = "fromDate", required = false) String fromDate,
                                                                @RequestParam(value = "toDate", required = false) String toDate,
            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return branchReceiptService.filterListAll(mrnNo, itemName, requestBranchName, issueBranchName, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<MaterialRequestDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getMaterialWithInBranchReceiptDetails/{id}")
    public ResponseEntity<?> getMaterialWithInBranchReceiptDetails(@PathVariable String id, Authentication authentication) {
        return branchReceiptService.getMaterialWithInBranchReceipt(id);
    }

    @DeleteMapping("/deleteMaterialWithInBranchReceipt/{id}")
    public ResponseEntity<?> deleteMaterialWithInBranchReceipt(@PathVariable String id, Authentication authentication) {
        return branchReceiptService.deleteWithInPlantReceipt(id);
    }

    @GetMapping("/getWithInBranchReceiptMRNIdByDetails/{id}")
    public ResponseEntity<?> getMRNIdByDetails(@PathVariable String id, Authentication authentication) {
        List<String> ids = Arrays.asList(id.split("-"));
        return branchReceiptService.getMRNDetails(ids.get(1));
    }
}
