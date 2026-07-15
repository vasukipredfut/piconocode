package com.aria.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SalesController {


    @Autowired
    SalesService salesService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveProduct")
    public ResponseEntity<?> saveProduct(@RequestBody ProductDto productDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesService.saveProduct(productDto, user.get());
    }

    @PutMapping("/updateProduct")
    public ResponseEntity<?> updateProduct(@RequestBody ProductDto productDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesService.updateProduct(productDto, user.get());
    }


    @PutMapping("/updatePriceUpdate")
    public ResponseEntity<?> updatepriceUpdate(@RequestBody SalesProductDto salesProductDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salesService.priceUpdate(salesProductDto, user.get());
    }


    @GetMapping("/getProductList")
    public ResponseEntity<?> listProduct(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salesService.listAllProduct(pageable);
        } else {
            return new ResponseEntity<List<ProductDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getPriceUpdateList")
    public ResponseEntity<?> PriceUpdate(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salesService.listAllpriceUpdate();
        } else {
            return new ResponseEntity<List<SalesProductDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getProductDetails/{id}")
    public ResponseEntity<?> getProduct(@PathVariable String id, Authentication authentication) {
        return salesService.getProduct(id);
    }


    @DeleteMapping("/deleteProductDetails/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, Authentication authentication) {

        return salesService.deleteProduct(id);
    }


}
