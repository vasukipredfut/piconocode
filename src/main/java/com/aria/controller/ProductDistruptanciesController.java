package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductDistruptanciesController {

    @Autowired
    ProductDistruptanciesService productDistruptanciesService;

    @Autowired
    UserRepository userRepository;


    // Excess/shortage

    @PostMapping("/saveProductExcessStorage")
    public ResponseEntity<?> saveProductExcessStorage(@RequestBody ProductExcessStorageDto productExcessStorageDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productDistruptanciesService.saveProductExcessStorage(productExcessStorageDto, user.get());
    }

    @PutMapping("/updateProductExcessStorage")
    public ResponseEntity<?> updateProductExcessStorage(@RequestBody ProductExcessStorageDto productExcessStorageDto,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productDistruptanciesService.updateProductExcessStorage(productExcessStorageDto, user.get());
    }

    @GetMapping("/getProductExcessStorageList")
    public ResponseEntity<?> getProductExcessStorageList(@RequestParam(value = "branchId", required = false) Long branchId,
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
            if (branchId != null) {
                branchId = user.get().getBranch_id();
            }
            return productDistruptanciesService.listAllProductExcessStorage(branchId, vendorName, billNo, type, productName, dateFrom, dateTo, status, pageable);

        } else {
            return new ResponseEntity<List<ProductExcessStorageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductExcessStorageDetails/{id}")
    public ResponseEntity<?> getProductExcessStorage(@PathVariable String id, Authentication authentication) {
        return productDistruptanciesService.getProductExcessStorageDetails(id);
    }

    @DeleteMapping("/deleteProductExcessStorageDetails/{id}")
    public ResponseEntity<?> deleteProductExcessStorage(@PathVariable String id, Authentication authentication) {
        return productDistruptanciesService.deleteProductExcessStorage(id);
    }

    // Purchase Return

    @PostMapping("/savePurchaseReturn")
    public ResponseEntity<?> savePurchaseReturn(@RequestBody PurchaseReturnDto purchaseReturnDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productDistruptanciesService.savePurchaseReturn(purchaseReturnDto, user.get());
    }

    @PutMapping("/updatePurchaseReturn")
    public ResponseEntity<?> updatePurchaseReturn(@RequestBody PurchaseReturnDto purchaseReturnDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productDistruptanciesService.updatePurchaseReturn(purchaseReturnDto, user.get());
    }

    @GetMapping("/getPurchaseReturnList")
    public ResponseEntity<?> getPurchaseReturnList(@RequestParam(value = "branchId", required = false) Long branchId,
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
            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            return productDistruptanciesService.listAllPurchaseReturn(branchId, vendorName, billNo, type, productName, dateFrom,
                    dateTo, status, pageable);
        } else {
            return new ResponseEntity<List<ProductExcessStorageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getPurchaseReturnDetails/{id}")
    public ResponseEntity<?> getPurchaseReturn(@PathVariable String id, Authentication authentication) {
        return productDistruptanciesService.getPurchaseReturn(id);
    }

    @DeleteMapping("/deletePurchaseReturnDetails/{id}")
    public ResponseEntity<?> deletePurchaseReturn(@PathVariable String id, Authentication authentication) {
        return productDistruptanciesService.deletePurchaseReturn(id);
    }

    @GetMapping("/getPurchaseReceipt/{id}")
    public ResponseEntity<?> getPurchaseReceipt(@PathVariable String id, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return productDistruptanciesService.getPurchaseReceipt(id, user.get());
    }

    @GetMapping("/checkValidBill")
    public ResponseEntity<?> checkValidBill(@RequestParam(value = "type", required = false) String type,
                                            @RequestParam(value = "vendorId", required = false) Long vendorId,
                                            @RequestParam(value = "billNo", required = false) String billNo,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productDistruptanciesService.checkValidBill(type, vendorId, billNo);
    }


}
