package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.ExpenseDto;
import com.aria.dto.TransportTripDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.ExpenseService;
import com.aria.service.TransportTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TransportTripController {

    @Autowired
    TransportTripService transportTripService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/transportTrip")
    public ResponseEntity<?> transportTrip(@RequestParam(value = "name",required = false) String name,@RequestParam(value = "cate",required = false) String cate,Authentication authentication, Pageable pageable) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return transportTripService.getAllTransportTrip(pageable,name,cate);
    }

    @GetMapping("/transportTrip/{id}")
    public ResponseEntity<?> transportTrip(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<TransportTripDto>(transportTripService.getTransportTrip(id), HttpStatus.OK);
    }

    @DeleteMapping("/transportTrip/{id}")
    public ResponseEntity<?> deleteTransportTrip(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        transportTripService.deleteTransportTrip(id);
        return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
    }

    @PostMapping("/addOrEdit/transportTrip")
    public ResponseEntity<?> addOrEditTransportTrip(@RequestBody TransportTripDto expenseDto, Authentication authentication) throws ParseException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String message = "Transport Trip updated successfully!!";
        if (  expenseDto.getId() == null || expenseDto.getId() == 0) {
            transportTripService.saveTransportTrip(expenseDto, user.get());
            message = "Transport Trip save successfully!!!";
        } else
            transportTripService.saveTransportTrip(expenseDto, user.get());
        return new ResponseEntity<Status>(new Status("200", message), HttpStatus.OK);
    }


}
