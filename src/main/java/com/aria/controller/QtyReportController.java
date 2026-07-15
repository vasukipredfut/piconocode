package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
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
import com.aria.dto.QtyMainDto;
import com.aria.dto.Sub2ProductDto;
import com.aria.model.User;
import com.aria.service.QtyReportService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class QtyReportController {
	
	@Autowired
    UserRepository userRepository;
	@Autowired
	QtyReportService qtyReportService;
	
	@GetMapping("/getAllQtyName")
    public ResponseEntity<?> getAllVoucherFormat( Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return qtyReportService.getAllQtyProduct();
        } else {
            return new ResponseEntity<List<QtyMainDto>>(new ArrayList<>(), HttpStatus.OK);
        }
       
    }
	@GetMapping("/qtyStatement")
	public ResponseEntity<?> qtyStatement(@RequestParam (value = "branch",required = false) Long branch,
            @RequestParam (value = "itemId", required = true) Long itemId,
            @RequestParam ("fromDate") String fromDate,
            @RequestParam ("toDate") String toDate,
            Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return qtyReportService.qtyStatement( branch, itemId, fromDate , toDate,user.get());
	}

	@GetMapping("/qtyStatementDetails")
	public List<Sub2ProductDto> getQtyDetail(@RequestParam (value = "branch",required = false) Long branch,
            @RequestParam (value = "itemId", required = true) Long itemId,
            @RequestParam (value = "sub1Id", required = true) Long sub1Id,
            @RequestParam ("fromDate") String fromDate,
            @RequestParam ("toDate") String toDate,
            Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return qtyReportService.getQtyDetail( branch, itemId,sub1Id, fromDate , toDate,user.get());
	}
}
