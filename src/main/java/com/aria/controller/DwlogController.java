package com.aria.controller;

import com.aria.dto.DwlogDto;
import com.aria.model.User;
import com.aria.service.DwlogService;
import com.aria.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class DwlogController {

    

    @Autowired
    private DwlogService dwlogService;

    @Autowired
    private UserRepository userRepository;



    @PutMapping("/updateDwlog")
    public ResponseEntity<?> updateDwlog(@RequestBody DwlogDto dto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return dwlogService.updateDwlog(dto, user.get());
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @GetMapping("/dwlog/{id}")
    public ResponseEntity<?> getDwlog(@PathVariable Long id, Authentication authentication) {
        return dwlogService.getDwlog(id);
    }

    @GetMapping("/dwlogList")
    public ResponseEntity<?> listDwlogs(Authentication authentication) {
        return dwlogService.listDwlogs();
    }

    @DeleteMapping("/deleteDwlog/{id}")
    public ResponseEntity<?> deleteDwlog(@PathVariable Long id, Authentication authentication) {
        return dwlogService.deleteDwlog(id);
    }
}
