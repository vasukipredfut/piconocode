
package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminInspectionService {

	@Autowired
	AdminInspectionRepository adminInspectionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveAdminInspection(AdminInspectionDto adminInspectionDto, User loggedUser) {
		AdminInspection adminInspection = new AdminInspection(adminInspectionDto);
		adminInspection.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		adminInspection.setDate(dateUtil.getStringTODateDDMMYYYY(adminInspectionDto.getDate()));
		adminInspection.setCreatedBy(loggedUser.getId());
		adminInspectionRepository.save(adminInspection);
		return new ResponseEntity<Status>(new Status("200", "Admin inspection save successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateAdminInspection(AdminInspectionDto adminInspectionDto, User loggedUser) {
		AdminInspection adminInspection = new AdminInspection(adminInspectionDto);
		adminInspection.setDate(dateUtil.getStringTODateDDMMYYYY(adminInspectionDto.getDate()));
		adminInspection.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		adminInspection.setUpdatedBy(loggedUser.getId());
		adminInspectionRepository.save(adminInspection);
		return new ResponseEntity<Status>(new Status("200", "Admin inspection  updated  successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {

		Page<AdminInspection> list = adminInspectionRepository.findAll(pageable);
		if (!list.toList().isEmpty()) {
			List<AdminInspectionDto> result = list.toList().stream().map(data -> {
				AdminInspectionDto adminInspectionDto = new AdminInspectionDto(data);
				adminInspectionDto.setDate(dateUtil.getDateToStringDDMMYYYY(data.getDate()));
				adminInspectionDto.setCreatedByName(userRepository.findById(adminInspectionDto.getCreatedBy()).get().getEmployeename());
				return adminInspectionDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<AdminInspectionDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<AdminInspectionDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getAdminInspection(String id) throws UsernameNotFoundException {
		Optional<AdminInspection> optionalAdminInspection = adminInspectionRepository.findById(Long.parseLong(id));
		AdminInspectionDto adminInspectionDto = new AdminInspectionDto(optionalAdminInspection.get());
		adminInspectionDto.setDate(dateUtil.getDateToStringDDMMYYYY(optionalAdminInspection.get().getDate()));
		adminInspectionDto.setCreatedByName(userRepository.findById(optionalAdminInspection.get().getCreatedBy()).get().getEmployeename());
		return new ResponseEntity<AdminInspectionDto>(adminInspectionDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteAdminInspection(String id) throws UsernameNotFoundException {
		try {
			Optional<AdminInspection> optionalAdminInspection = adminInspectionRepository.findById(Long.parseLong(id));
			if (optionalAdminInspection.isPresent()) {
				AdminInspection branch = optionalAdminInspection.get();
				adminInspectionRepository.delete(branch);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

}
