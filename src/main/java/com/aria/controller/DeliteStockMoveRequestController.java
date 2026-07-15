package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.DeliteStockMoveRequestDto;
import com.aria.dto.MachineDto;
import com.aria.model.User;
import com.aria.service.DeliteStockMoveRequestService;
import com.aria.service.MachineService;
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
public class DeliteStockMoveRequestController {

    @Autowired
    DeliteStockMoveRequestService deliteStockMoveRequestService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/deliteStockMoveRequest")
    public ResponseEntity<?> saveDeliteStockMoveRequest(@RequestBody DeliteStockMoveRequestDto deliteStockMoveRequestDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return deliteStockMoveRequestService.saveDeliteStockMoveRequest(deliteStockMoveRequestDto, user.get());
        } else {
            return new ResponseEntity<List<DeliteStockMoveRequestDto>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/deliteStockMoveRequest")
    public ResponseEntity<?> fetchAllDeliteStockMoveRequest(@RequestParam(value = "fromBranchId", required = false) Long fromBranchId,
                                                            @RequestParam(value = "toBranchId", required = false) Long toBranchId,
                                                            @RequestParam(value = "status", required = false) String status,
                                                            Pageable pageable,
                                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return deliteStockMoveRequestService.fetchAllDeliteStockMoveRequest(fromBranchId,toBranchId,status,pageable);
        } else {
            return new ResponseEntity<List<DeliteStockMoveRequestDto>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/deliteStockMoveRequest/{id}")
    public ResponseEntity<?> fetchDeliteStockMoveRequestById(@PathVariable("id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return deliteStockMoveRequestService.fetchDeliteStockMoveRequestById(id);
        } else {
            return new ResponseEntity<List<DeliteStockMoveRequestDto>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        }

    }
}
