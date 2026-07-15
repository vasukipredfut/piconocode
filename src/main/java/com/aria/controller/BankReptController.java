package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.BankReptDto;
import com.aria.dto.CustomVoucherNameDto;
import com.aria.model.User;
import com.aria.service.BankReptService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BankReptController {

	@Autowired
    UserRepository userRepository;
	@Autowired
	BankReptService bankReptService;
	
	@GetMapping("/bankRept")
    public ResponseEntity<?> bankReport(@RequestParam (value = "branch",required = true) Long branch,
                                           @RequestParam (value = "firmId", required = false) Long firmId,
                                            @RequestParam (value = "year" , required = false) Long year,
                                           @RequestParam (value = "month" , required = false) Long month, @RequestParam (value = "fromdate" , required = false) String fromdate, @RequestParam (value = "todate" , required = false) String todate,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return bankReptService.bankReport(user.get(), branch, firmId , year , month,fromdate,todate);
    }
}
