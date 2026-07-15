package com.aria.controller;

import com.aria.dao.SendExternalItemsRepository;
import com.aria.dao.SendExternalRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.ActionConfigurationDto;
import com.aria.dto.ActionDto;
import com.aria.dto.SendExternalDto;
import com.aria.model.User;
import com.aria.service.ActionService;
import com.aria.service.SendExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/material")
@CrossOrigin
public class SendExternalController {


	@Autowired
	UserRepository userRepository;

	@Autowired
	SendExternalService sendExternalService;



	@GetMapping("/sendExternal")
	public ResponseEntity<?> getSendExternal(@RequestParam(value = "name", required = false) String name,
											 @RequestParam(value = "fromBranch", required = false) String fromBranch,
											 @RequestParam(value = "fromBranchId", required = false) Long fromBranchId,
											 @RequestParam(value = "firmId", required = false) Long firmId,
											 @RequestParam(value = "toReach", required = false) String toReach,
											 @RequestParam(value = "issueBy", required = false) String issueBy,
											 @RequestParam(value = "fromDate", required = false) String fromDate,
											 @RequestParam(value = "toDate", required = false) String toDate,Pageable pageable,Authentication authentication) {
		return sendExternalService.getSendExternal(name, toReach, fromDate, toDate, issueBy,  fromBranch, fromBranchId, firmId , pageable);
	}

	@PostMapping("/sendExternal")
	public ResponseEntity<?> saveSendExternal(@RequestBody SendExternalDto sendExternalDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		return sendExternalService.saveExternal(sendExternalDto,user.get());
	}

	@DeleteMapping("/sendExternal/{id}")
	public ResponseEntity<?> deleteSendExternal(@PathVariable Long id, Authentication authentication) {
		return sendExternalService.deleteSendExternal(id);
	}

	@GetMapping("/sendExternal/{id}")
	public ResponseEntity<?> getSendExternal(@PathVariable Long id, Authentication authentication) {
		return sendExternalService.getSendExternal(id);
	}

}
