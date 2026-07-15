package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.BankDTO;
import com.aria.dto.BankDetailsDTO;
import com.aria.dto.CreditNoteBillDto;
import com.aria.dto.CreditNoteItemsDto;
import com.aria.model.User;
import com.aria.service.BankDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BankDetailsController {

    @Autowired
    BankDetailsService bankDetailsService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveBank")
    public ResponseEntity<?> saveBank(@RequestBody BankDTO bankDetails,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.saveBank(bankDetails, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");
    }


    @PostMapping("/saveBankDetails")
    public ResponseEntity<?> saveBankDetails(@RequestBody BankDetailsDTO bankDetails,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.saveBankDetails(bankDetails, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");
    }

    @PutMapping("/updateBankDetails")
    public ResponseEntity<?> updateBankDetails(@RequestBody BankDetailsDTO bankDetails,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.updateBankDetails(bankDetails, user.get());
        else
            return ResponseEntity.badRequest().body("User not found");
    }

    @GetMapping("/bankDetailsList")
    public ResponseEntity<?> bankDetailsList(@RequestParam(value = "bankName", required = false) String bankName,
                                               @RequestParam(value = "branchName", required = false) String branchName,
                                               @RequestParam(value = "ifscCode", required = false) String ifscCode,
                                               Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.listAll(bankName, branchName, ifscCode, pageable);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/bankDetails/{id}")
    public ResponseEntity<?> bankDetails(@PathVariable("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.bankDetails(id);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @DeleteMapping("/deleteBankDetails/{id}")
    public ResponseEntity<?>deleteBankDetails(@PathVariable("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.deleteBankDetails(id);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

    @GetMapping("/bankList")
    public ResponseEntity<?> bankList(@RequestParam(value = "bankName", required = false) String bankName,
                                               @RequestParam(value = "branchName", required = false) String branchName,
                                               @RequestParam(value = "ifscCode", required = false) String ifscCode,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null)
            return bankDetailsService.bankList(bankName, branchName, ifscCode);
        else
            return ResponseEntity.badRequest().body("User not found");

    }

}
