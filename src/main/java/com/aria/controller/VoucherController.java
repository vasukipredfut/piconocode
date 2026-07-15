package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.User;
import com.aria.service.VoucherService;
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
public class VoucherController {


    @Autowired
    VoucherService voucherService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveSalaryVoucher")
    public ResponseEntity<?> saveSalaryVoucher(@RequestBody SalaryVoucherDto salaryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return voucherService.saveSalaryVoucher(salaryDto, user.get());
    }


    @PostMapping("/saveLeaveSalaryVoucher")
    public ResponseEntity<?> saveLeaveSalaryVoucher(@RequestBody LeaveSalaryVoucherDto salaryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return voucherService.saveLeaveSalaryVoucher(salaryDto, user.get());
    }


    // EPF VOUCHER


    @GetMapping("/epfSalaryList")
    public ResponseEntity<?> epfSalaryList(@RequestParam(value = "firmId") Long firmId,
                                           @RequestParam(value = "year") String year,
                                           @RequestParam(value = "month") Long month,
                                           @RequestParam(value = "empType", required = false) int empType,
                                           @RequestParam(value = "epfType") String epfType,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return voucherService.epfSalaryList(firmId, year, month, empType, epfType);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PostMapping("/saveEpfVoucher")
    public ResponseEntity<?> saveEpfVoucher(@RequestBody EpfVoucherDto salaryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return voucherService.saveEpfVoucher(salaryDto, user.get());
    }


    // ESI VOUCHER


    @GetMapping("/esiSalaryList")
    public ResponseEntity<?> esiSalaryList(@RequestParam(value = "firmId") Long firmId,
                                           @RequestParam(value = "year") String year,
                                           @RequestParam(value = "month") Long month,
                                           @RequestParam(value = "empType", required = false) int empType,
                                           @RequestParam(value = "esiType") String esiType,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
                return voucherService.esiSalaryList(firmId, year, month, empType, esiType);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PostMapping("/saveEsiVoucher")
    public ResponseEntity<?> saveEsiVoucher(@RequestBody EsiVoucherDto salaryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return voucherService.saveEsiVoucher(salaryDto, user.get());
    }


    @PostMapping("/saveSalaryVoucher1")
    public ResponseEntity<?> saveSalary(@RequestBody SalaryVoucherDto salaryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return voucherService.saveSalaryVoucher(salaryDto, user.get());
    }
}
