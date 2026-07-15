package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.DutyUnplannedRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.DutyUnplannedDto;
import com.aria.model.DutyUnplanned;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class DutyUnplannedService {


    @Autowired
    DutyUnplannedRepository dutyUnplannedRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    AttendanceService attendanceService;


    //    @Transactional
    public ResponseEntity<?> saveDutyUnplanned(DutyUnplannedDto dutyUnplannedDto, User loggedUser) {
        DutyUnplanned dutyUnplanned = new DutyUnplanned(dutyUnplannedDto);
        dutyUnplanned.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyUnplanned.setCreatedBy(loggedUser.getId());
        dutyUnplanned.setDutyFrom(dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyFrom()));
        dutyUnplanned.setDutyTo(dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyTo()));
        dutyUnplannedRepository.save(dutyUnplanned);
        attendanceService.save(dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyFrom()),
                dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyTo()), dutyUnplannedDto.getUserId());
        return new ResponseEntity<Status>(new Status("200", "DutyUnplanned saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDutyUnplanned(DutyUnplannedDto dutyUnplannedDto, User loggedUser) {
        DutyUnplanned dutyUnplanned = new DutyUnplanned(dutyUnplannedDto);
        dutyUnplanned.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyUnplanned.setUpdatedBy(loggedUser.getId());
        dutyUnplanned.setDutyFrom(dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyFrom()));
        dutyUnplanned.setDutyTo(dateUtil.getStringTODateWk(dutyUnplannedDto.getDutyTo()));
        DutyUnplanned result = dutyUnplannedRepository.save(dutyUnplanned);
        return new ResponseEntity<Status>(new Status("200", "DutyUnplanned updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllDutyUnplanned(Pageable pageable) throws UsernameNotFoundException {
        Page<DutyUnplanned> dutyUnplanned = dutyUnplannedRepository.findAll(pageable);
        return new ResponseEntity<List<DutyUnplannedDto>>(dutyUnplanned.toList().stream().map(user -> {
                    DutyUnplannedDto dutyUnplannedDto = new DutyUnplannedDto(user);
                    dutyUnplannedDto.setDutyFrom(dateUtil.getDateTOStringWk(user.getDutyFrom()));
                    dutyUnplannedDto.setDutyTo(dateUtil.getDateTOStringWk(user.getDutyTo()));

                    //updateEmployeeDetails(dutyUnplannedDto);
                    return dutyUnplannedDto;
                }
        ).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getDutyUnplannedDetails(String id) throws UsernameNotFoundException {
        Optional<DutyUnplanned> optionalDutyUnplanned = dutyUnplannedRepository.findById(Long.parseLong(id));
        DutyUnplannedDto dutyUnplannedDto = new DutyUnplannedDto(optionalDutyUnplanned.get());
        dutyUnplannedDto.setDutyFrom(dateUtil.getDateTOStringWk(optionalDutyUnplanned.get().getDutyFrom()));
        dutyUnplannedDto.setDutyTo(dateUtil.getDateTOStringWk(optionalDutyUnplanned.get().getDutyTo()));
        return new ResponseEntity<DutyUnplannedDto>(dutyUnplannedDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteDutyUnplanned(String id) throws UsernameNotFoundException {
        try {
            Optional<DutyUnplanned> dutyUnplannedOptional = dutyUnplannedRepository.findById(Long.parseLong(id));
            if (dutyUnplannedOptional.isPresent()) {
                DutyUnplanned dutyUnplanned = dutyUnplannedOptional.get();
                dutyUnplannedRepository.delete(dutyUnplanned);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> dutyUnplannedfilterList(Long userId, Long departmentId, String dateFrom, String dateTo, Pageable pageable) throws UsernameNotFoundException {
        Page<DutyUnplanned> dutyUnplanned = dutyUnplannedRepository.
                dutyUnplannedfilterList(userId, departmentId, dateUtil.getStringTODateF1(dateFrom), dateUtil.getStringTODateF1(dateTo), pageable);
        return new ResponseEntity<List<DutyUnplannedDto>>(dutyUnplanned.toList().stream().map(user -> {
                    DutyUnplannedDto dutyUnplannedDto = new DutyUnplannedDto(user);
                    dutyUnplannedDto.setDutyFrom(dateUtil.getDateTOStringWk(user.getDutyFrom()));
                    dutyUnplannedDto.setDutyTo(dateUtil.getDateTOStringWk(user.getDutyTo()));
                    return dutyUnplannedDto;
                }
        ).collect(Collectors.toList()), HttpStatus.OK);
    }
}


