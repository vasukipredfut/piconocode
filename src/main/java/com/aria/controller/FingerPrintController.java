package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.FingerPrintDto;
import com.aria.model.User;
import com.aria.service.FingerPrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FingerPrintController {

    @Autowired
    FingerPrintService fingerPrintService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/auth/fingerPrintAdd")
    public ResponseEntity<?> fingerPrintAdd(@Valid @RequestBody FingerPrintDto request) {
        return fingerPrintService.UpdateFingerPrint(request);
    }

    @GetMapping("/fingerPrintList")
    public ResponseEntity<?> fingerPrintList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return fingerPrintService.listAllUser(pageable);
    }

}
