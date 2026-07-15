package com.aria.controller;

import com.aria.dao.UserRepository;

import com.aria.service.BillService;
import com.aria.service.CheckMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class MigrationController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CheckMigrationService migrationService;
    @Autowired
    BillService billService;

    @GetMapping("/updateMigrationHsnCategory")
    public ResponseEntity<?> updateMigrationHsnCategory(Authentication authentication) {
         migrationService.updateMigrationHsnCategory();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/updateMigrationHsnItem")
    public ResponseEntity<?> updateMigrationHsnItem(Authentication authentication) {
         migrationService.updateMigrationHsnItem();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/updateClosePurchaseOrder")
    public ResponseEntity<?> updateClosePurchaseOrder(Authentication authentication) {
        billService.closePurchaseOrder();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/updateDpProduct")
    public ResponseEntity<?> updateDpProduct(Authentication authentication) {
        billService.updateDpProduct();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
