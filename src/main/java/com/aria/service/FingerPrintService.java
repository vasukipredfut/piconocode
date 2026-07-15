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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FingerPrintService {

    @Autowired
    FingerPrintRepository fingerPrintRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;


    public ResponseEntity<?> UpdateFingerPrint(FingerPrintDto fingerPrintDto) throws UsernameNotFoundException {
        FingerPrint fingerPrint = new FingerPrint(fingerPrintDto);
        fingerPrint.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        fingerPrint.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        fingerPrint.setStatus("Added");
        fingerPrintRepository.save(fingerPrint);
        return new ResponseEntity<Status>(new Status("200", "finger print Added!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllUser(Pageable pageable) throws UsernameNotFoundException {

        Page<User> userList = userRepository.findAll(pageable);
        if (!userList.toList().isEmpty()) {
            List<FingerPrintDto> result = userList.toList().stream().map(data -> {
                FingerPrintDto fingerPrintDto = new FingerPrintDto(data);
                fingerPrintDto.setJoinDate(dateUtil.getStringTODateF1(dateUtil.getISTDate()));
                Optional<FingerPrint> fingerPrint = fingerPrintRepository.findByUserId(data.getId());
                if (fingerPrint.isPresent()) {
                    fingerPrintDto.setStatus(fingerPrint.get().getStatus());
                } else {
                    fingerPrintDto.setStatus("Pending");
                }
                return fingerPrintDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<FingerPrintDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FingerPrintDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
