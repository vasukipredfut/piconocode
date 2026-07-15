package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.HolidayDto;
import com.aria.model.User;
import com.aria.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HolidayController {

    @Autowired
    HolidayService holidayService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveHoliday")
    public ResponseEntity<?> saveHoliday(@RequestBody HolidayDto holidayDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return holidayService.saveHoliday(holidayDto, user.get());
    }

    @PutMapping("/updateHoliday")
    public ResponseEntity<?> updateHoliday(@RequestBody HolidayDto holidayDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return holidayService.updateHoliday(holidayDto, user.get());
    }

    @GetMapping("/getHolidayDetails/{id}")
    public ResponseEntity<?> getHolidayDetails(@PathVariable String id, Authentication authentication) {
        return holidayService.getHolidayDetails(id);
    }

    @DeleteMapping("/deleteHolidayDetails/{id}")
    public ResponseEntity<?> deleteHolidayDetails(@PathVariable String id, Authentication authentication) {
        return holidayService.deleteHolidayDetails(id);
    }

    @GetMapping("/holidayList")
    public ResponseEntity<?> holidayList(
            @RequestParam(value = "holidayType", required = false) String holidayType,
            @RequestParam(value = "year", required = false) String year,
            Pageable pageable,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return holidayService.holidayList(holidayType, year, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }
}
