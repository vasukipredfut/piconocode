package com.aria.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.ViewAccountLedgerDto;
import com.aria.model.User;
import com.aria.service.ViewAccountsLedgerService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ViewAccountsLedgerController {

    @Autowired
    ViewAccountsLedgerService viewAccountLedgerService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/viewAccountsLedgerListAll")
    public ResponseEntity<?> viewAccountsLedgerListAll(@RequestParam(value = "firmId", required = false) Long firmId,
                                                       @RequestParam(value = "partyId", required = false) Long partyId,
                                                       @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                       @RequestParam(value = "dateTo", required = false) String dateTo,
                                                       Pageable pageable,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return viewAccountLedgerService.viewAccountsLedgerListAll(firmId, partyId, dateFrom, dateTo, pageable);
        } else {
            return new ResponseEntity<List<ViewAccountLedgerDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/printAccountsLedgerReport")
    public ResponseEntity<?> printAccountsLedgerReport(@RequestParam(value = "partyId", required = false) Long partyId,
                                                       @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                       @RequestParam(value = "dateTo", required = false) String dateTo,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return viewAccountLedgerService.printAccountsLedgerReport(partyId, dateFrom, dateTo);
        } else {
            return new ResponseEntity<List<ViewAccountLedgerDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
