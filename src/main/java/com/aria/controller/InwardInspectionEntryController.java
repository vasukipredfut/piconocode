package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.dto.AttributesAssignDto;
import com.aria.dto.InspectionEntryRequest;
import com.aria.dto.InspectionEntryRequestList;
import com.aria.dto.InwardAuditDTO;
import com.aria.dto.PatlogDto;
import com.aria.dto.RoqCommentDTO;
import com.aria.dto.UpdateOilTasteDTO;
import com.aria.model.User;
import com.aria.service.InwardInspectionEntryService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class InwardInspectionEntryController {

	@Autowired
	InwardInspectionEntryService inwardInspectionEntryService;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/attributes/inwardInspection")
	public ResponseEntity<?> saveInwardInspectionEntry(@RequestBody List<PatlogDto> reqDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return inwardInspectionEntryService.saveInwardInspectionEntry(reqDto, user.get());
	}

	@PutMapping("/attributes/inwardInspection")
	public ResponseEntity<?> updateInwardInspectionEntry(@RequestBody List<PatlogDto> reqDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return inwardInspectionEntryService.updateInwardInspectionEntry(reqDto, user.get());
	}

	@GetMapping("/attributes/inwardInspectionDetails")
	public ResponseEntity<?> getAttributesInwardInspectionData(
			@RequestParam(value = "entryType", required = false) String entryType,
			@RequestParam(value = "batchNo", required = false) String batchNo,
			@RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
			@RequestParam(value = "inwardDate", required = false) String inwardDate, Authentication authentication) {
		return inwardInspectionEntryService.getAttributesInwardInspectionData(entryType, kpiNameId, inwardDate,
				batchNo);
	}

	@GetMapping("/attributes/inwardInspection")
	public ResponseEntity<?> getAllDetailsInward(@RequestParam(value = "entryType", required = false) String entryType,
			@RequestParam(value = "batchNo", required = false) String batchNo,
			@RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
			@RequestParam(value = "inwardDate", required = false) String inwardDate,
			@RequestParam(value = "status", required = false) String status, Pageable pageable,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.getAllInwardInspectionDetails(entryType, batchNo, kpiNameId, inwardDate,
					status, pageable);
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/print/inwardInspection")
	public ResponseEntity<?> getprintDetailsInward(@RequestBody InspectionEntryRequestList requestList,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.getAttributesInwardInspectionDataPrint(requestList);
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/printRot/inwardInspection")
	public ResponseEntity<?> getprintDetailsInwardRot(@RequestBody List<InspectionEntryRequest> requestList,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.getAttributesInwardInspectionDataPrintRot(requestList);
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PutMapping("/closeInwardInspection")
	public ResponseEntity<?> closeInwardInspection(@RequestBody PatlogDto patlogDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.closeInwardInspection(patlogDto, user.get());
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PutMapping("/createRecheckInwardInspection")
	public ResponseEntity<?> createRecheckInwardInspection(@RequestBody PatlogDto patlogDto,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.createRecheckInwardInspection(patlogDto, user.get());
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/viewInwardInspectionData")
	public ResponseEntity<?> viewInwardInspectionData(
			@RequestParam(value = "entryType", required = false) String entryType,
			@RequestParam(value = "batchNo", required = false) String batchNo,
			@RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
			@RequestParam(value = "inwardDate", required = false) String inwardDate, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.viewInwardInspectionData(entryType, kpiNameId, inwardDate, batchNo);
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/inwardInspectionSkipLog")
	public ResponseEntity<?> saveInwardInspectionSkipLog(@RequestBody List<PatlogDto> reqDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return inwardInspectionEntryService.saveInwardInspectionSkipLog(reqDto, user.get());
	}

	@PostMapping("/updateOilTaste")
	public ResponseEntity<?> updateOilTaste(@RequestBody UpdateOilTasteDTO tasteDTO, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.updateOilTaste(tasteDTO, user.get());
		} else {
			return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/commentOnTasteList")
	public ResponseEntity<?> roqCommentList(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.listRoqComment();
		} else {
			return new ResponseEntity<List<RoqCommentDTO>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/updateTasteInInwardAudit")
	public ResponseEntity<?> updateInwardAudit(@RequestBody InwardAuditDTO auditDto, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return inwardInspectionEntryService.updateInwardAudit(auditDto, user.get());
		} else {
			return ResponseEntity.badRequest().body("User not found");
		}
	}
}
