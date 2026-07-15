package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.BillInwardDto;
import com.aria.dto.AwardsDto;
import com.aria.model.User;
import com.aria.service.AwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AwardController {

    @Autowired
    AwardService awardService;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/saveAward")
    public ResponseEntity<?> saveAward(@RequestBody AwardsDto awardsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return awardService.saveAward(awardsDto, user.get());
    }

    @PutMapping("/updateAward")
    public ResponseEntity<?> updateBillInward(@RequestBody AwardsDto awardsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return awardService.updateAward(awardsDto, user.get());
    }

    @GetMapping("/getAwardList")
    public ResponseEntity<?> getAwardList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return awardService.listAllAward(pageable);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getAwardDetails/{id}")
    public ResponseEntity<?> getAwardDetails(@PathVariable String id, Authentication authentication) {

        return awardService.getAwardDetails(id);
    }

    @DeleteMapping("/deleteAwardDetails/{id}")
    public ResponseEntity<?> deleteAwardDetails(@PathVariable String id, Authentication authentication) {

        return awardService.deleteAward(id);
    }


    @GetMapping("/getEmployeeAwardList")
    public ResponseEntity<?> getEmployeeAwardList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return awardService.employeeListAllAward();
        } else {
            return new ResponseEntity<List<AwardsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getParticularEmployeeAllAwardList")
    public ResponseEntity<?> getParticularEmployeeAllAwardList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return awardService.getParticularEmployeeAllAwardList(user.get());
    }

    @GetMapping("/getParticularEmployeeAwardList")
    public ResponseEntity<?> getParticularEmployeeAwardList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return awardService.userByListAllAward(user.get());
    }

    @PostMapping("/saveDummyAward")
    public ResponseEntity<?> saveDummyAward(@RequestBody AwardsDto awardsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return awardService.saveDummyAward(awardsDto, user.get());
    }

    @GetMapping("/getEmployeeDetailByAwardList")
    public ResponseEntity<?> getEmployeeDetailByAwardList(@RequestParam(value = "id", required = false) String id, @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate" , required = false) String toDate, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if(id != null) {
                return awardService.getEmployeeDetailByAwardList(id, fromDate, toDate);
            }else {
                return awardService.getEmployeeDetailByAwardList(String.valueOf(user.get().getId()), fromDate, toDate);
            }
        } else {
            return new ResponseEntity<Map<String, Object>>(new HashMap<>(), HttpStatus.OK);
        }
    }

}
