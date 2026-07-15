package com.aria.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.QualityInspectionDTO;
import com.aria.model.User;
import com.aria.service.QualityInspectionService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PurchaseQualityInspectionScoreController {

	@Autowired
	QualityInspectionService qualityInspectionService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/saveinspection")
	public ResponseEntity<?> saveinspection(@RequestBody List<QualityInspectionDTO> qualityInspectionDTO,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return qualityInspectionService.saveQualityInspections(qualityInspectionDTO, user.get());
	}

	@GetMapping("/listinspection")
	public ResponseEntity<?> listinspection(@RequestParam(value = "po_id", required = false) Long po_id,
			@RequestParam(value = "billNumber", required = false) String billNumber,
			@RequestParam(value = "vendorId", required = false) Long vendorId,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate, Pageable page,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return qualityInspectionService.listallQualityInspection(po_id, billNumber, vendorId, fromDate, toDate,
				user.get(), page);
	}

	@GetMapping("/qualityinspectionid/{id}")
	public ResponseEntity<?> listinspection(@PathVariable(value = "id") Long id, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return qualityInspectionService.getQualityInspectionbyId(id, user.get());
	}

}
