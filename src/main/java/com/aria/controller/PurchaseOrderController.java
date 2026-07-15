package com.aria.controller;


import java.util.*;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.PurchaseOrderService;
import com.aria.utils.DateUtil;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PurchaseOrderController {

    @Autowired
    PurchaseOrderService purchaseOrderService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/savePurchaseOrder")
    public ResponseEntity<?> savePurchaseOrder(@RequestBody PurchaseOrderDto purchaseOrderDto,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return purchaseOrderService.savePurchaseOrder(purchaseOrderDto, user.get());
    }

    @PutMapping("/updatePurchaseOrder")
    public ResponseEntity<?> updatePurchaseOrder(@RequestBody PurchaseOrderDto purchaseOrderDto,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return purchaseOrderService.updatePurchaseOrder(purchaseOrderDto, user.get());
    }

    @GetMapping("/purchaseOrderListAll")
    public ResponseEntity<?> purchaseOrderListAll(@RequestParam(value = "poNumber", required = false) String poNumber,
                                                  @RequestParam(value = "firmId", required = false) Long firmId,
                                                  @RequestParam(value = "branch", required = false) Long branch,
                                                  @RequestParam(value = "dept", required = false) Long dept,
                                                  @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                  @RequestParam(value = "vendorName", required = false) String vendorName,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "proposalid", required = false) Long proposalid, Pageable pageable,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return purchaseOrderService.purchaseOrderListAll(poNumber, firmId, vendorId, vendorName, proposalid, branch,
                    dept, status, pageable);
        } else {
            return new ResponseEntity<List<PurchaseOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/purchaseOrder/{id}")
    public ResponseEntity<?> purchaseOrderById(@PathVariable(value = "id") String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return purchaseOrderService.purchaseOrderById(id);
        } else {
            return new ResponseEntity<List<PurchaseOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/viewPurchaseOrder/{id}")
    public ResponseEntity<?> viewPurchaseOrder(@PathVariable(value = "id") String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return purchaseOrderService.viewPurchaseOrder(id);
        } else {
            return new ResponseEntity<List<PurchaseOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/purchaseOrderFilterListByVendorId/{vendorId}")
    public ResponseEntity<?> purchaseOrderFilterListByVendorId(@PathVariable(value = "vendorId") Long vendorId,
                                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return purchaseOrderService.getPurchaseOrderFilterListByVendorId(vendorId);
        } else {
            return new ResponseEntity<List<PurchaseOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/purchaseOrderFilterBySisterConcernMapVendor/{vendorId}")
    public ResponseEntity<?> purchaseOrderFilterBySisterConcernMapVendor(@PathVariable(value = "vendorId") Long vendorId,
                                                                         @RequestParam(value = "firmId") Long firmId,
                                                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return purchaseOrderService.getPurchaseOrderFilterListBySisterConcernVendorId(vendorId, firmId);
        } else {
            return new ResponseEntity<List<PurchaseOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/deletePurchaseOrder")
    public ResponseEntity<?> deletePurchaseOrder(@RequestParam("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return purchaseOrderService.deletePurchaseOrder(id);
    }

    @GetMapping("/pendingSupplierQty")
    public ResponseEntity<?> pendingSupplierQty(@RequestParam(value = "poNumber", required = false) String poNumber,
                                                @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                @RequestParam(value = "proposalid", required = false) Long proposalid,
                                                @RequestParam(value = "plantid", required = false) Long plantid, Pageable pageable,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return purchaseOrderService.getSupplierQty(poNumber, proposalid, vendorId, plantid, pageable);
    }

    @PostMapping("/auth/All")
    public ResponseEntity<?> All() {
        return purchaseOrderService.all();
    }

    @PostMapping("/changePOStatus")
    public ResponseEntity<?> changePOStatus(@RequestParam(value = "status", required = true) String status,
                                            @RequestParam(value = "purchaseOrderId", required = true) Long poId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user != null) {
            if (status.equalsIgnoreCase("Reject")) {
                return purchaseOrderService.poReject(poId, user.get());
            } else {
                return purchaseOrderService.changePOStatus(poId, user, status);
            }
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PostMapping("/POStatusAuthorized")
    public ResponseEntity<?> POStatusAuthorized(@RequestBody PurchaseOrderDto dto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user != null)
            return purchaseOrderService.POStatusAuthorized(dto, user);
        else
            return ResponseEntity.badRequest().body("User not found");
    }


    @GetMapping("/auth/checkDateTime")
    public String checkDateTime() {

        DateUtil dateUtil = new DateUtil();

        System.out.println(" Date " + dateUtil.getISTDate());

        return "Success";
    }

    @DeleteMapping("/inactivePurchaseOrderItem")
    public ResponseEntity<?> inactivePurchaseOrderItem(@RequestParam("poId") Long poId,
                                                       @RequestParam("poItemId") String poItemId,
                                                       @RequestParam("active") boolean active,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return purchaseOrderService.inactivePurchaseOrderItem(poId, poItemId, active);
    }


}
