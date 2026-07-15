package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.TsLapDto;
import com.aria.model.User;
import com.aria.service.TsLapService;
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
public class TsLapController {

    @Autowired
    TsLapService tsLapService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveTsLap")
    public ResponseEntity<?> saveTsLap(@RequestBody TsLapDto tsLapDto,
                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return tsLapService.saveTsLap(tsLapDto, user.get());
    }

    @PutMapping("/updateTsLap")
    public ResponseEntity<?> updateTsLap(@RequestBody TsLapDto tsLapDto,
                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return tsLapService.updateTsLap(tsLapDto, user.get());
    }

    @GetMapping("/tsLapListAll")
    public ResponseEntity<?> TsLapListAll(@RequestParam (value = "tsId", required = false) Long tsId,
                                          Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return tsLapService.listAllTsLap(tsId);
        } else {
            return new ResponseEntity<List<TsLapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/tsLapListAll/{shiftId}")
    public ResponseEntity<?> TsLapListAllByShift(@PathVariable int shiftId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return tsLapService.listAllTsLapByShift(shiftId);
        } else {
            return new ResponseEntity<List<TsLapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/allTsLapDropDownlist")
    public ResponseEntity<?> TsLapListAll(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return tsLapService.listAllTsLap();
        } else {
            return new ResponseEntity<List<TsLapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/tsLapById/{id}")
    public ResponseEntity<?> tsLapById(@PathVariable(value = "id") String id,
                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return tsLapService.getTsLap(id);
        } else {
            return new ResponseEntity<List<TsLapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/deleteTsLapById/{id}")
    public ResponseEntity<?> deleteTsLapById(@PathVariable(value = "id") String id,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return tsLapService.deleteTsLap(id);
        } else {
            return new ResponseEntity<List<TsLapDto>>(HttpStatus.NOT_FOUND);
        }
    }
}
