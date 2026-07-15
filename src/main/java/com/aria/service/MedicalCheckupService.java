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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicalCheckupService {

    @Autowired
    MedicalCheckupRepository medicalCheckupRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;


    public ResponseEntity<?> saveMedicalCheckup(MedicalCheckupDto medicalCheckupDto, User loggedUser) {
        MedicalCheckup medicalCheckup = new MedicalCheckup(medicalCheckupDto);
        medicalCheckup.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        medicalCheckup.setCreatedBy(loggedUser.getId());
        medicalCheckup.setCheckupDate(dateUtil.getStringTODateF1(medicalCheckupDto.getCheckupDate()));
        medicalCheckup.setStatus("Completed");
        MedicalCheckup result = medicalCheckupRepository.save(medicalCheckup);
        return new ResponseEntity<Status>(new Status("200", "medical checkup saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateMedicalCheckup(MedicalCheckupDto medicalCheckupDto, User loggedUser) {
        MedicalCheckup medicalCheckup = new MedicalCheckup(medicalCheckupDto);
        medicalCheckup.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        medicalCheckup.setUpdatedBy(loggedUser.getId());
        medicalCheckup.setCheckupDate(dateUtil.getStringTODateF1(medicalCheckupDto.getCheckupDate()));
        medicalCheckup.setStatus("Completed");
        MedicalCheckup result = medicalCheckupRepository.save(medicalCheckup);
        return new ResponseEntity<Status>(new Status("200", "medical checkup updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllMedicalCheckup(String employeeName, String firm, String branch, String deptName, String category, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {
        Page<MedicalCheckup> list = medicalCheckupRepository.findAll(employeeName, firm, branch, deptName , category, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);
        if (!list.toList().isEmpty()) {
            List<MedicalCheckupDto> result = list.toList().stream().map(user -> {
                        MedicalCheckupDto medicalCheckupDto = new MedicalCheckupDto(user);
                        medicalCheckupDto.setCheckupDate(dateUtil.getStringTODate(user.getCheckupDate()));
                        updateEmployeeDetails(medicalCheckupDto);
                        return medicalCheckupDto;
                    }
            ).collect(Collectors.toList());
            PageDataDto<LeaveDetailsDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getMedicalCheckupDetails(String id) throws UsernameNotFoundException {
        Optional<MedicalCheckup> optionalMedicalCheckup = medicalCheckupRepository.findById(Long.parseLong(id));
        MedicalCheckupDto medicalCheckupDto = new MedicalCheckupDto(optionalMedicalCheckup.get());
        medicalCheckupDto.setCheckupDate(dateUtil.getStringTODate(optionalMedicalCheckup.get().getCheckupDate()));
        return new ResponseEntity<MedicalCheckupDto>(medicalCheckupDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMedicalCheckup(String id) throws UsernameNotFoundException {
        try {
            Optional<MedicalCheckup> medicalCheckupOptional = medicalCheckupRepository.findById(Long.parseLong(id));
            if (medicalCheckupOptional.isPresent()) {
                MedicalCheckup user = medicalCheckupOptional.get();
                medicalCheckupRepository.delete(user);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(MedicalCheckupDto medicalCheckup) {

        /*Optional<User> user = userRepository.findById(medicalCheckup.getCreatedBy());
        if (user.isPresent()) {
            //pageDto.setCreatedByName(user.get().getEmployeeName());
            //pageDto.setUpdatedByName(user.get().getEmployeeName());
        }
        if (medicalCheckup.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(medicalCheckup.getUpdatedBy());
            if (user1.isPresent()) {
               // medicalCheckup.setUpdatedByName(user.get().getEmployeeName());
            }
        }*/
    }
}
