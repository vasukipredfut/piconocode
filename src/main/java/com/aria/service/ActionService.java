package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.ActionConfigurationRepository;
import com.aria.dao.ActionRepository;
import com.aria.dao.ShiftsRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.ActionConfigurationDto;
import com.aria.dto.ActionDto;
import com.aria.dto.PageDataDto;
import com.aria.model.Action;
import com.aria.model.ActionConfiguration;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class ActionService {

	@Autowired
	ShiftsRepository shiftsRepository;

	@Autowired
	ActionConfigurationRepository actionConfigurationRepository;

	@Autowired
	ActionRepository actionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveAction(ActionDto reqDto, User user) {
		try {
			Action model = new Action(reqDto);
			model.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			model.setCreatedBy(user.getId());
			actionRepository.save(model);
			return new ResponseEntity<Status>(new Status("200", "Action saved successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Action not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllDetails(User user, Pageable pageable) {
		Page<Action> list = actionRepository.findAllWithFilters(pageable);
		if (!list.toList().isEmpty()) {
			List<ActionDto> result = list.toList().stream().map(data -> {
				ActionDto generatorDto = new ActionDto(data);
				return generatorDto;
			}).collect(Collectors.toList());
			PageDataDto<ActionDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<ActionDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<ActionDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}
	
	public ResponseEntity<?> saveActionConfiguration(ActionConfigurationDto reqDto, User user) {
		try {
			ActionConfiguration model = new ActionConfiguration(reqDto);
			model.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			model.setCreatedBy(user.getId());
			actionConfigurationRepository.save(model);
			return new ResponseEntity<Status>(new Status("200", "Action Configuration saved successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Action Configuration not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllDetailsForActionConfiguration(User user, Pageable pageable) {
		Page<ActionConfiguration> list = actionConfigurationRepository.findAllWithFilters(pageable);
		if (!list.toList().isEmpty()) {
			List<ActionConfigurationDto> result = list.toList().stream().map(data -> {
				ActionConfigurationDto generatorDto = new ActionConfigurationDto(data);
				return generatorDto;
			}).collect(Collectors.toList());
			PageDataDto<ActionConfigurationDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<ActionConfigurationDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<ActionConfigurationDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getActionDetailsById(String id) {
		Optional<Action> optinoalData = actionRepository.findById(Long.parseLong(id));
		ActionDto actionDto = new ActionDto(optinoalData.get());
		return new ResponseEntity<ActionDto>(actionDto, HttpStatus.OK);
	}

	public ResponseEntity<?> getActionConfigDetailsById(String id) {
		Optional<ActionConfiguration> optinoalData = actionConfigurationRepository.findById(Long.parseLong(id));
		ActionConfigurationDto actionDto = new ActionConfigurationDto(optinoalData.get());
		return new ResponseEntity<ActionConfigurationDto>(actionDto, HttpStatus.OK);
	}

}
