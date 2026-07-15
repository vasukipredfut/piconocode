package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.BinStockLockService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
@AllArgsConstructor
public class BinStockController {

    private final BinStockLockService binStockLockService;

    private final UserRepository userRepository;

    @GetMapping("/binstock/binLock")
    public ResponseEntity<?> getBinLocks(@RequestParam(value = "branchId", required = false) Long branchId,
                                         @RequestParam(value = "lockType", required = false) String lockType,
                                         @RequestParam(value = "binNumber", required = false) Long binNumber,
                                         @RequestParam(value = "productId", required = false) Long productId,
                                         @RequestParam(value = "paging", required = false, defaultValue = "true") boolean paging, Pageable pageable,
                                         Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {

            if (productId == null) {
                productId = 773L;
            }

            if (paging)
                return binStockLockService.fetchAllBinLock(branchId, lockType, binNumber, productId, pageable);
            else {
                return binStockLockService.fetchAllBinLock(branchId, lockType, binNumber, productId);
            }
        } else {
            return new ResponseEntity<>(new Status("401", "Not Authorized to access"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/binLock/binDropdown")
    public ResponseEntity<?> getBinDropdown(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "lockType", required = false) String lockType,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return binStockLockService.getBinDropdown(branchId, lockType);
        } else {
            return new ResponseEntity<>(new Status("401", "Not Authorized to access"), HttpStatus.UNAUTHORIZED);
        }
    }

}
