package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.DispatchService;
import com.aria.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DispatchController {


    @Autowired
    DispatchService dispatchService;

    @Autowired
    UserRepository userRepository;

    //DispatchAgainstOrder
    @PostMapping("/saveDispatchAgainstOrder")
    public ResponseEntity<?> saveDispatchAgainstOrder(@RequestBody DispatchAgainstOrderDto dispatchAgainstOrderDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.saveDispatchAgainstOrder(dispatchAgainstOrderDto, user.get());
    }

    @PutMapping("/updateDispatchAgainstOrder")
    public ResponseEntity<?> updateDispatchAgainstOrder(@RequestBody DispatchAgainstOrderDto dispatchAgainstOrderDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.updateDispatchAgainstOrder(dispatchAgainstOrderDto, user.get());
    }

    @GetMapping("/getDispatchAgainstOrder")
    public ResponseEntity<?> getDispatchAgainstOrder(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dispatchService.listAllDispatchAgainstOrder();
        } else {
            return new ResponseEntity<List<DispatchAgainstOrderDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getDispatchAgainstOrder/{id}")
    public ResponseEntity<?> getDispatchAgainstOrder(@PathVariable String id, Authentication authentication) {

        return dispatchService.getDispatchAgainstOrder(id);
    }

    @DeleteMapping("/deleteDispatchAgainstOrder/{id}")
    public ResponseEntity<?> deleteDispatchAgainstOrder(@PathVariable String id, Authentication authentication) {

        return dispatchService.deleteDispatchAgainstOrder(id);
    }

//DispatchOrderWithPrice


    @PostMapping("/saveDispatchOrderWithPrice")
    public ResponseEntity<?> saveDispatchOrderWithPrice(@RequestBody DispatchOrderWithPriceDto dispatchOrderWithPriceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.saveDispatchOrderWithPrice(dispatchOrderWithPriceDto, user.get());
    }

    @PutMapping("/updateDispatchOrderWithPrice")
    public ResponseEntity<?> updateDispatchOrderWithPrice(@RequestBody DispatchOrderWithPriceDto dispatchOrderWithPriceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.updateDispatchOrderWithPrice(dispatchOrderWithPriceDto, user.get());
    }

    @GetMapping("/getDispatchOrderWithPrice")
    public ResponseEntity<?> getDispatchOrderWithPrice(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dispatchService.listAllDispatchOrderWithPrice();
        } else {
            return new ResponseEntity<List<DispatchOrderWithPriceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getDispatchOrderWithPrice/{id}")
    public ResponseEntity<?> getDispatchOrderWithPrice(@PathVariable String id, Authentication authentication) {

        return dispatchService.getDispatchOrderWithPrice(id);
    }

    @DeleteMapping("/deleteDispatchOrderWithPrice/{id}")
    public ResponseEntity<?> deleteDispatchOrderWithPrice(@PathVariable String id, Authentication authentication) {

        return dispatchService.deleteDispatchOrderWithPrice(id);
    }


//DispatchAgainstBill


    @PostMapping("/saveDispatchAgainstBill")
    public ResponseEntity<?> saveDispatchAgainstBill(@RequestBody DispatchAgainstBillDto dispatchAgainstBillDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.saveDispatchAgainstBill(dispatchAgainstBillDto, user.get());
    }

    @PutMapping("/updateDispatchAgainstBill")
    public ResponseEntity<?> updateDispatchAgainstBill(@RequestBody DispatchAgainstBillDto dispatchAgainstBillDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return dispatchService.updateDispatchAgainstBill(dispatchAgainstBillDto, user.get());
    }

    @GetMapping("/getDispatchAgainstBill")
    public ResponseEntity<?> getDispatchAgainstBill(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dispatchService.listAllDispatchAgainstBill();
        } else {
            return new ResponseEntity<List<DispatchAgainstBillDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getDispatchAgainstBill/{id}")
    public ResponseEntity<?> getDispatchAgainstBill(@PathVariable String id, Authentication authentication) {

        return dispatchService.getDispatchAgainstBill(id);
    }

    @DeleteMapping("/deleteDispatchAgainstBill/{id}")
    public ResponseEntity<?> deleteDispatchAgainstBill(@PathVariable String id, Authentication authentication) {

        return dispatchService.deleteDispatchAgainstBill(id);
    }

}