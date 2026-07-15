package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.dto.ProductExcessStorageDto;
import com.aria.model.User;
import com.aria.service.HoSalesService;
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
public class HoSalesController {

    @Autowired
    HoSalesService hoSalesService;

    @Autowired
    UserRepository userRepository;



    // Ho Sales invoice

    @GetMapping("/hoSalesInvoiceList")
    public ResponseEntity<?> getHoSalesInvoiceList(
            @RequestParam(value = "vendorName", required = false) String vendorName,
            @RequestParam(value = "billNo", required = false) String billNo,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            @RequestParam(value = "status", required = false) String status, Authentication authentication,
            Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoSalesService.getHoSalesInvoiceList(vendorName, billNo, type, productName, dateFrom, dateTo, status, pageable);
        } else {
            return new ResponseEntity<List<ProductExcessStorageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/hoSalesInvoice/{id}")
    public ResponseEntity<?> hoSalesInvoice(@PathVariable String id, Authentication authentication) {
        return hoSalesService.hoSalesInvoice(id);
    }







    // Ho Sales order

    @GetMapping("/hoSalesOrderList")
    public ResponseEntity<?> hoSalesOrderList(
            @RequestParam(value = "vendorName", required = false) String vendorName,
            @RequestParam(value = "billNo", required = false) String billNo,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            @RequestParam(value = "status", required = false) String status, Authentication authentication,
            Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hoSalesService.hoSalesOrderList(vendorName, billNo, type, productName, dateFrom, dateTo, status, pageable);
        } else {
            return new ResponseEntity<List<ProductExcessStorageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/hoSalesOrder/{id}")
    public ResponseEntity<?> hoSalesOrder(@PathVariable String id, Authentication authentication) {
        return hoSalesService.hoSalesOrder(id);
    }
}
