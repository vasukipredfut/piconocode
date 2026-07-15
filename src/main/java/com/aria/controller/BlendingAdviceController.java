package com.aria.controller;

import com.aria.dto.*;
import com.aria.model.*;
import com.aria.dao.*;
import com.aria.service.*;
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
public class BlendingAdviceController {

    @Autowired
    BlendingAdviceService blendingAdviceService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/checkBlending")
    public ResponseEntity<?> checkBlending(@RequestParam("journalId") Long journalId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.checkBlending(journalId, user.get());
    }

    @PostMapping("/saveBlendingAdvice")
    public ResponseEntity<?> saveBlendingAdvice(@RequestBody BlendingAdviceDto blendingAdviceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.saveBlendingAdvice(blendingAdviceDto, user.get());
    }

    @PutMapping("/updateBlendingAdvice")
    public ResponseEntity<?> updateBlendingAdvice(@RequestBody BlendingAdviceDto binDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.updateBlendingAdvice(binDetailsDto, user.get());
    }

    @DeleteMapping("/deleteBlending/{id}")
    public ResponseEntity<?> deleteBlending(@PathVariable("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.deleteBlending(id);
    }


    @GetMapping("/blendingLockANDStockDetails")
    public ResponseEntity<?> blendingLockANDStockDetails(@RequestParam(value = "productId", required = false) Long productId,
                                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.getBlendingStockDetails(productId, user.get());
    }

    @GetMapping("/blendingAllStockDetails")
    public ResponseEntity<?> blendingAllStockDetails(@RequestParam(value = "productId", required = false) Long productId,
                                                     @RequestParam(value = "branchId", required = false) Long branchId,
                                                     @RequestParam(value = "category", required = false) String category,
                                                     @RequestParam(value = "ffa", required = false) Double ffa,
                                                     @RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "lockType", required = false) String lockType,
                                                     @RequestParam(value = "blendingAdviceId", required = false) Long blendingAdviceId,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.blendingAllStockDetails(productId, branchId, category, ffa, status, blendingAdviceId, lockType);
        } else {
            return new ResponseEntity<List<BlendingStockDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/saveBlendingAdviceDetails")
    public ResponseEntity<?> saveBlendingAdviceDetails(@RequestBody List<BlendingAdviceDetailsDto> blendingAdviceDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.saveBlendingAdviceDetails(blendingAdviceDetailsDto, user.get());
    }

    @PutMapping("/updateBlendingAdviceDetails")
    public ResponseEntity<?> updateBlendingAdviceDetails(@RequestBody BlendingAdviceDetailsDto binDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return blendingAdviceService.updateBlendingAdviceDetails(binDetailsDto, user.get());
    }


    @GetMapping("/blendingAdviceDetailsList")
    public ResponseEntity<?> allBlendingAdviceDetailsList(Pageable pageable,
                                                          Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.blendingList(user.get().getBranch_id(), pageable);
        } else {
            return new ResponseEntity<List<BlendingAdviceDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/blendingAdviceDetail/{id}")
    public ResponseEntity<?> blendingAdviceDetailsById(@PathVariable("id") Long id, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.blendingAdviceDetailsById(id);
        } else {
            return new ResponseEntity<List<BlendingAdviceDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/printBlendingAdviceDetailsList")
    public ResponseEntity<?> printAllBlendingAdviceDetailsList(@RequestParam(value = "journalId", required = false) Long journalId,
                                                               @RequestParam(value = "kalasalWeek", required = false) String kalasalWeek,
                                                               @RequestParam(value = "status", required = false) String status,
                                                               @RequestParam(value = "branch", required = false) Long branch,
                                                               Pageable pageable,
                                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //if (branch != null) {
            return blendingAdviceService.printBlendingList(journalId, kalasalWeek, status, branch, pageable);
            //} else {
            // return blendingAdviceService.printBlendingList(journalId, kalasalWeek, status, user.get().getBranch_id(), pageable);
            // }
        } else {
            return new ResponseEntity<List<BlendingAdviceDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PutMapping("/activateBlendingAdvice")
    public ResponseEntity<?> activateBlendingAdviceById(@RequestParam(value = "journalId") Long journalId,
                                                        @RequestParam(value = "adviceId", required = false) Long adviceId,
                                                        @RequestParam(value = "branchId", required = false) Long branchId,
                                                        Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.activateBlendingAdvice(adviceId, journalId, branchId, user.get());
        } else {
            return new ResponseEntity<List<BlendingAdviceDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/printBlendingStockDetails")
    public ResponseEntity<?> printBlendingStockDetails(@RequestBody PrintBlendingStock printBlendingStock,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.printBlendingStockDetails(printBlendingStock);
        } else {
            return new ResponseEntity<List<BlendingAdviceDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getCasBinWiseLotANDStock")
    public ResponseEntity<?> getCasBinWiseLotANDStock(@RequestParam(value = "blendingStockId", required = false) Long blendingStockId,
                                                      @RequestParam(value = "productId", required = false) Long productId,
                                                      @RequestParam(value = "branch", required = false) Long branch,
                                                      @RequestParam(value = "casgroup", required = false) String casgroup,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.getCasBinWiseLotANDStock(blendingStockId, productId, branch, casgroup);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/categoryList")
    public ResponseEntity<?> categoryList(@RequestParam(value = "category", required = false) String category,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return blendingAdviceService.categoryList(category);
        } else {
            return new ResponseEntity<List<String>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
