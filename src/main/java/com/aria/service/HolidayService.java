package com.aria.service;

import com.aria.dao.HolidayDetailsRepository;
import com.aria.dao.HolidayRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    @Autowired
    HolidayRepository holidayRepository;

    @Autowired
    HolidayDetailsRepository holidayDetailsRepository;

    @Autowired
    UserRepository userRepository;

    // BinRange

    public ResponseEntity<?> saveHoliday(HolidayDto holidayDto, User loggedUser) {
        Holiday holiday = new Holiday(holidayDto);
        holiday.setCreatedBy(loggedUser.getId());
        holiday.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Holiday result = holidayRepository.save(holiday);
        if (holidayDto.getHolidayDetails() != null && !holidayDto.getHolidayDetails().isEmpty()) {
            for (HolidayDetailsDto holidayDetailsDto : holidayDto.getHolidayDetails()) {
                HolidayDetails holidayDetails = new HolidayDetails(holidayDetailsDto);
                holidayDetails.setHolidayId(result.getId());
                holidayDetailsRepository.save(holidayDetails);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "Holiday save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateHoliday(HolidayDto holidayDto, User loggedUser) {
        Holiday holiday = new Holiday(holidayDto);
        holiday.setUpdatedBy(loggedUser.getId());
        holiday.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Holiday result = holidayRepository.save(holiday);
        if (holidayDto.getHolidayDetails() != null && !holidayDto.getHolidayDetails().isEmpty()) {
            for (HolidayDetailsDto holidayDetailsDto : holidayDto.getHolidayDetails()) {
                HolidayDetails holidayDetails = new HolidayDetails(holidayDetailsDto);
                holidayDetails.setHolidayId(result.getId());
                holidayDetailsRepository.save(holidayDetails);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "Holiday  updated  successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> getHolidayDetails(String id) throws UsernameNotFoundException {
        Optional<Holiday> optionalHoliday = holidayRepository.findById(Long.parseLong(id));
        HolidayDto holidayDto = new HolidayDto(optionalHoliday.get(), true);
        return new ResponseEntity<HolidayDto>(holidayDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteHolidayDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<Holiday> optionalHoliday = holidayRepository.findById(Long.parseLong(id));
            if (optionalHoliday.isPresent()) {
                Holiday holiday = optionalHoliday.get();
                HolidayDto holidayDto = new HolidayDto(holiday, true);
                if (!holidayDto.getHolidayDetails().isEmpty()) {
                    for (HolidayDetailsDto holidayDetailsDto : holidayDto.getHolidayDetails()) {
                        HolidayDetails holidayDetails = new HolidayDetails(holidayDetailsDto);
                        holidayDetailsRepository.delete(holidayDetails);
                    }
                }
                holidayRepository.delete(holiday);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> holidayList(String holidayType, String year, Pageable pageable) throws UsernameNotFoundException {

        Page<Holiday> list = holidayRepository.holidayFilterList(holidayType, year, pageable);
        if (!list.isEmpty()) {
            List<HolidayDto> result = list.toList().stream().map(data -> {
                HolidayDto holidayDto = new HolidayDto(data, false);
                Optional<User> userOptional = userRepository.findById(data.getCreatedBy());
                if(userOptional.isPresent()){
                    holidayDto.setCreatedByName(userOptional.get().getEmployeename());
                }
                return holidayDto;
            }).collect(Collectors.toList());
            PageDataDto<HolidayDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<HolidayDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<HolidayDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

}
