
package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WelfareRegistrationService {

    @Autowired
    WelfareRegistrationRepository welfareRegistrationRepository;

    @Autowired
    WelfareEmployeeRepository welfareEmployeeRepository;

    @Autowired
    UserRepository userRepository;

   
    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> saveWelfareRegistration(WelfareRegistrationDto welfareRegistrationDto, User loggedUser) {
    	WelfareRegistration welfareRegistration = new WelfareRegistration(welfareRegistrationDto);
    	welfareRegistration.setFromDate(dateUtil.getStringTODateF1(welfareRegistrationDto.getFromDate()));
    	welfareRegistration.setToDate(dateUtil.getStringTODateF1(welfareRegistrationDto.getToDate()));
    	welfareRegistration.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        welfareRegistration.setCreatedBy(loggedUser.getId());
    	WelfareRegistration result = welfareRegistrationRepository.save(welfareRegistration);
        for (WelfareEmployeeDto welfareEmployeeDto : welfareRegistrationDto.getWelfareEmployee()) {
        	WelfareEmployee welfareEmployee = new WelfareEmployee(welfareEmployeeDto);
        	welfareEmployee.setEmpId(result.getId());
        	//welfareEmployee.setRegistrationOn(dateUtil.getStringTODateF1(welfareEmployeeDto.getRegistrationOn()));
        	welfareEmployeeRepository.save(welfareEmployee);
        }
        return new ResponseEntity<Status>(new Status("200", "Welfare Registration save successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateWelfareRegistration(WelfareRegistrationDto welfareRegistrationDto, User loggedUser) {
    	WelfareRegistration welfareRegistration = new WelfareRegistration(welfareRegistrationDto);
    	welfareRegistration.setFromDate(dateUtil.getStringTODateF1(welfareRegistrationDto.getFromDate()));
    	welfareRegistration.setToDate(dateUtil.getStringTODateF1(welfareRegistrationDto.getToDate()));
    	welfareRegistration.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        welfareRegistration.setUpdatedBy(loggedUser.getId());
    	WelfareRegistration result = welfareRegistrationRepository.save(welfareRegistration);
        for (WelfareEmployeeDto welfareEmployeeDto : welfareRegistrationDto.getWelfareEmployee()) {
        	WelfareEmployee welfareEmployee = new WelfareEmployee(welfareEmployeeDto);
        	welfareEmployee.setEmpId(result.getId());
        	//welfareEmployee.setRegistrationOn(dateUtil.getStringTODateF1(welfareEmployeeDto.getRegistrationOn()));
        	welfareEmployeeRepository.save(welfareEmployee);
        }
        return new ResponseEntity<Status>(new Status("200", "WelfareEmployee  updated  successfully!!!"), HttpStatus.OK);
    }


 public ResponseEntity<?> listAllwelfareRegistration() throws UsernameNotFoundException {
        List<WelfareRegistration> list = welfareRegistrationRepository.findAll();
        if (!list.isEmpty()) {
            List<WelfareRegistrationDto> result = list.stream().map(data -> {
            	WelfareRegistrationDto welfareRegistrationDto = new WelfareRegistrationDto(data);
            	welfareRegistrationDto.setFromDate(dateUtil.getStringTODate(data.getFromDate()));
            	welfareRegistrationDto.setToDate(dateUtil.getStringTODate(data.getToDate()));
            	updateEmployeeDetails(welfareRegistrationDto);
                return welfareRegistrationDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<WelfareRegistrationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WelfareRegistrationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    } 
   

    public ResponseEntity<?> getWelfareRegistrationDetails(String id) throws UsernameNotFoundException {
        Optional<WelfareRegistration> optionalWelfareRegistration = welfareRegistrationRepository.findById(Long.parseLong(id));
        WelfareRegistrationDto welfareRegistrationDto = new WelfareRegistrationDto(optionalWelfareRegistration.get());
        welfareRegistrationDto.setFromDate(dateUtil.getStringTODate(optionalWelfareRegistration.get().getFromDate()));
        welfareRegistrationDto.setToDate(dateUtil.getStringTODate(optionalWelfareRegistration.get().getToDate()));
        updateEmployeeDetails(welfareRegistrationDto);
        return new ResponseEntity<WelfareRegistrationDto>(welfareRegistrationDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWelfareRegistration(String id) throws UsernameNotFoundException {
        try {
            Optional<WelfareRegistration> optionalWelfareRegistration = welfareRegistrationRepository.findById(Long.parseLong(id));
            if (optionalWelfareRegistration.isPresent()) {
            	WelfareRegistration welfareRegistration = optionalWelfareRegistration.get();
            	welfareRegistrationRepository.delete(welfareRegistration);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateEmployeeDetails(WelfareRegistrationDto welfareRegistration) {

        Optional<User> user = userRepository.findById(welfareRegistration.getCreatedBy());
        if (user.isPresent()) {
            //welfareRegistration.setCreatedByName(user.get().getEmployeeName());
            //welfareRegistration.setUpdatedByName(user.get().getEmployeeName());
        }
        if (welfareRegistration.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(welfareRegistration.getUpdatedBy());
            if (user1.isPresent()) {
                //welfareRegistration.setUpdatedByName(user.get().getEmployeeName());
            }
        }
    }
}
