package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.GoDownSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class GoDownSummaryController {

    @Autowired
    GoDownSummaryService goDownSummaryService;


    @Autowired
    UserRepository userRepository;

    @GetMapping("/goDownSummaryProductList")
    public ResponseEntity<?> goDownSummaryProductList(@RequestParam(value = "name",required = false) String name,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return goDownSummaryService.goDownSummaryProductList(name);
    }


    @GetMapping("/goDownSummary")
    public ResponseEntity<?> goDownSummary(@RequestParam (value = "branch",required = true) Long branch,
                                           @RequestParam (value = "itemId", required = true) Long itemId,
                                           @RequestParam (value = "godownId",required = false) Long godownId,
                                           @RequestParam ("fromDate") String fromDate,
                                           @RequestParam ("toDate") String toDate,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return goDownSummaryService.goDownSummary(user.get(), branch, itemId, godownId , fromDate , toDate);
    }





}
