package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.ProcessReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProcessReportController {

    @Autowired
    ProcessReportService processReportService;


    @Autowired
    UserRepository userRepository;


    @GetMapping("/processReportNameList")
    public ResponseEntity<?> processReportNameList(@RequestParam (value = "name",required = false) String name,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return processReportService.getProcessReportNameList(name);
    }


    @GetMapping("/processReport")
    public ResponseEntity<?> processReport(@RequestParam ("branch") Long branch,
                                           @RequestParam ("reportNameId") Long reportNameId,
                                           @RequestParam ("fromDate") String fromDate,
                                           @RequestParam ("toDate") String toDate,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return processReportService.processReport(user.get(), branch , reportNameId, fromDate , toDate);
    }
}
