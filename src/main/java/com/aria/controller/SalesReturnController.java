package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.dto.SalesReturnDto;
import com.aria.model.User;
import com.aria.service.SalesReturnService;
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
public class SalesReturnController {


    @Autowired
    SalesReturnService salesReturnService;

    @Autowired
    UserRepository userRepository;


    // Sales Return

    @PostMapping("/saveSalesReturn")
    public ResponseEntity<?> saveSalesReturn(@RequestBody SalesReturnDto salesReturnDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesReturnService.saveSalesReturn(salesReturnDto, user.get());
    }

    @PutMapping("/updateSalesReturn")
    public ResponseEntity<?> updateSalesReturn(@RequestBody SalesReturnDto salesReturnDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesReturnService.updateSalesReturn(salesReturnDto, user.get());
    }


    @GetMapping("/getSalesReturnList")
    public ResponseEntity<?> getSalesReturnList(@RequestParam(value = "vendorName", required = false) String vendorName,
                                                @RequestParam(value = "refNo", required = false) String refNo,
                                                @RequestParam(value = "type", required = false) String type,
                                                @RequestParam(value = "productName", required = false) String productName,
                                                @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                @RequestParam(value = "dateTo", required = false) String dateTo,
                                                Authentication authentication,
                                                Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salesReturnService.getSalesReturnList(type, refNo, vendorName, dateFrom, dateFrom, pageable);
        } else {
            return new ResponseEntity<List<SalesReturnDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getSalesReturnDetails/{id}")
    public ResponseEntity<?> getSalesReturnDetails(@PathVariable String id, Authentication authentication) {
        return salesReturnService.getSalesReturnDetails(id);
    }

    @DeleteMapping("/deleteSalesReturnDetails/{id}")
    public ResponseEntity<?> deleteSalesReturnDetails(@PathVariable String id, Authentication authentication) {
        return salesReturnService.deleteSalesReturnDetails(id);
    }



    @PutMapping("/salesRejectOrVerify")
    public ResponseEntity<?> salesRejectOrVerify(@RequestParam(value = "id", required = false) Long id,
                                               @RequestParam(value = "type", required = false) String type,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesReturnService.salesRejectOrVerify(id, type, user.get());
    }


    @PutMapping("/deleteReturnProduct")
    public ResponseEntity<?> deleteReturnProduct(@RequestParam(value = "id", required = false) Long id,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesReturnService.deleteReturnProduct(id);
    }

}
