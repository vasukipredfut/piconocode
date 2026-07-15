package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.CanteenAttendanceDto;
import com.aria.dto.CanteenAttendanceTokenDto;
import com.aria.dto.CanteenMenuDto;
import com.aria.dto.CanteenTokenDto;
import com.aria.model.User;
import com.aria.service.CanteenMenuService;
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
public class CanteenMenuController {

    @Autowired
    CanteenMenuService canteenMenuService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveCanteenMenu")
    public ResponseEntity<?> saveCanteenMenu(@RequestBody CanteenMenuDto canteenMenuDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.saveCanteenMenu(canteenMenuDto, user.get());
    }

    @PutMapping("/updateCanteenMenu")
    public ResponseEntity<?> updateCanteenMenu(@RequestBody CanteenMenuDto canteenMenuDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.updateCanteenMenu(canteenMenuDto, user.get());
    }


    @GetMapping("/getCanteenMenuList")
    public ResponseEntity<?> getCanteenMenuList(Authentication authentication,Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.listAllCanteenMenu(pageable);
        } else {
            return new ResponseEntity<List<CanteenMenuDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getcanteenMenuFilterList")
    public ResponseEntity<?> canteenMenuFilterList(
            @RequestParam(value = "dayId", required = false) Long dayId,
            @RequestParam(value = "foodItemId", required = false) Long foodItemId,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.canteenMenufilterList(dayId, foodItemId, dateFrom, dateTo);
        } else {
            return new ResponseEntity<List<CanteenMenuDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getCanteenMenuDetails/{id}")
    public ResponseEntity<?> getCanteenMenu(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.getCanteenMenuDetails(id);
    }

    @DeleteMapping("/deleteCanteenMenuDetails/{id}")
    public ResponseEntity<?> deleteCanteenMenu(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.deleteCanteenMenu(id);
    }
    
    //Canteen Token

    @PostMapping("/saveCanteenToken")
    public ResponseEntity<?> saveCanteenToken(@RequestBody CanteenTokenDto canteenTokenDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.saveCanteenToken(canteenTokenDto, user.get());
    }

    @PutMapping("/updateCanteenToken")
    public ResponseEntity<?> updateCanteenToken(@RequestBody CanteenTokenDto canteenTokenDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.updateCanteenToken(canteenTokenDto, user.get());
    }

    @GetMapping("/getCanteenTokenList")
    public ResponseEntity<?> getCanteenToken(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.listAllCanteenToken();
        } else {
            return new ResponseEntity<List<CanteenTokenDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getcanteenTokenFilterList")
    public ResponseEntity<?> canteenTokenFilterList(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "tokenFor", required = false) String tokenFor,
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.canteenTokenfilterList(userId, tokenFor, shiftId, dateFrom, dateTo);
        } else {
            return new ResponseEntity<List<CanteenTokenDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getCanteenTokenDetails/{id}")
    public ResponseEntity<?> getCanteenToken(@PathVariable String id, Authentication authentication) {

        return canteenMenuService.getCanteenTokenDetails(id);
    }

    @DeleteMapping("/deleteCanteenToken/{id}")
    public ResponseEntity<?> deleteCanteenToken(@PathVariable String id, Authentication authentication) {

        return canteenMenuService.deletecanteenToken(id);
    }
//canteen attendance

    @PostMapping("/saveCanteenAttendance")
    public ResponseEntity<?> saveCanteenAttendance(@RequestBody CanteenAttendanceDto canteenAttendanceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.saveCanteenAttendance(canteenAttendanceDto, user.get());
    }

    @PutMapping("/updateCanteenAttendance")
    public ResponseEntity<?> updateCanteenAttendance(@RequestBody CanteenAttendanceDto canteenAttendanceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.updateCanteenAttendance(canteenAttendanceDto, user.get());
    }


    @GetMapping("/getCanteenAttendanceList")
    public ResponseEntity<?> getCanteenAttendanceList(@RequestParam(value = "typeKey", required = false) String typeKey,
    		Authentication authentication, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
//            return canteenMenuService.listAllCanteenAttendance(typeKey);
        	return canteenMenuService.getAllData(pageable);
        } else {
            return new ResponseEntity<List<CanteenAttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getCanteenAttendanceDetails/{id}")
    public ResponseEntity<?> getCanteenAttendance(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.getCanteenAttendanceDetails(id);
    }

    @DeleteMapping("/deleteCanteenAttendanceDetails/{id}")
    public ResponseEntity<?> deleteCanteenAttendance(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.deleteCanteenAttendance(id);
    }
//canteen attendance token

    @PostMapping("/saveCanteenAttendanceToken")
    public ResponseEntity<?> saveCanteenAttendanceToken(@RequestBody CanteenAttendanceTokenDto canteenAttendanceTokenDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.saveCanteenAttendanceToken(canteenAttendanceTokenDto, user.get());
    }

    @PutMapping("/updateCanteenAttendanceToken")
    public ResponseEntity<?> updateCanteenAttendanceToken(@RequestBody CanteenAttendanceTokenDto canteenAttendanceTokenDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return canteenMenuService.updateCanteenAttendanceToken(canteenAttendanceTokenDto, user.get());
    }


    @GetMapping("/getCanteenAttendanceTokenList")
    public ResponseEntity<?> getCanteenAttendanceTokenList(@RequestParam(value = "date") String date, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.listAllcanteenAttendanceToken(date);
        } else {
            return new ResponseEntity<List<CanteenAttendanceTokenDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getCanteenAttendanceTokenDetails/{id}")
    public ResponseEntity<?> getCanteenAttendanceToken(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.getCanteenAttendanceToken(id);
    }

    @DeleteMapping("/deleteCanteenAttendanceTokenDetails/{id}")
    public ResponseEntity<?> deleteCanteenAttendanceToken(@PathVariable String id, Authentication authentication) {
        return canteenMenuService.deleteCanteenAttendanceToken(id);
    }

    @GetMapping("/checkDatesAllotted")
    public ResponseEntity<?> checkDatesAllotted(@RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return canteenMenuService.checkDate(fromDate, toDate);
        } else {
            return new ResponseEntity<Map<String,String>>(new HashMap<>(), HttpStatus.OK);
        }
    }
}


