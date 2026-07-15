
package com.aria.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.SkillsRepository;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.CompentencyMatrixRepository;
import com.aria.dao.SkillDetailsRepository;
import com.aria.utils.DateUtil;

@Service
public class CompentencyMatrixService {

    @Autowired
    CompentencyMatrixRepository compentencyMatrixRepository;

    @Autowired
    SkillDetailsRepository skillDetailsRepository;

    @Autowired
    SkillsRepository skillsRepository;

    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> saveCompentencyMatrix(CompentencyMatrixDto compentencyMatrixDto, User loggedUser) {
        CompentencyMatrix compentencyMatrix = new CompentencyMatrix(compentencyMatrixDto);
        compentencyMatrix.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        compentencyMatrix.setCreatedBy(loggedUser.getId());
        compentencyMatrix.setDate(dateUtil.getStringTODateWk(compentencyMatrixDto.getDate()));
        CompentencyMatrix result = compentencyMatrixRepository.save(compentencyMatrix);
        List<SkillDetails> skillDetailsList = new LinkedList<>();
        for (SkillDetailsDto skillDetailsDto : compentencyMatrixDto.getSkillDetails()) {
            SkillDetails skillDetails = new SkillDetails(skillDetailsDto);
            skillDetails.setCompetencyId(result.getId());
            Skills skills = updateCompentencyMatrixSkills(skillDetailsDto.getSkills());
            skillDetails.setSkills(skills.getName());
            skillDetails.setSkillId(skills.getId());
            skillDetailsList.add(skillDetails);
        }
        skillDetailsRepository.saveAll(skillDetailsList);
        return new ResponseEntity<Status>(new Status("200", "Compentency Matrix save successfully!!!"),
                HttpStatus.OK);
    }


    public Skills updateCompentencyMatrixSkills(String skillName) {

        Optional<Skills> skillsOptional = skillsRepository.findByName(skillName);
        if (skillsOptional.isPresent()) {
            return skillsOptional.get();
        } else {
            Skills skills = new Skills();
            skills.setName(skillName);
            return skillsRepository.save(skills);
        }
    }

    public ResponseEntity<?> updateCompentencyMatrix(CompentencyMatrixDto compentencyMatrixDto, User loggedUser) {
        CompentencyMatrix compentencyMatrix = new CompentencyMatrix(compentencyMatrixDto);
        compentencyMatrix.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        compentencyMatrix.setUpdatedBy(loggedUser.getId());
        compentencyMatrix.setDate(dateUtil.getStringTODateWk(compentencyMatrixDto.getDate()));
        CompentencyMatrix result = compentencyMatrixRepository.save(compentencyMatrix);
        for (SkillDetailsDto skillDetailsDto : compentencyMatrixDto.getSkillDetails()) {
            SkillDetails skillDetails = new SkillDetails(skillDetailsDto);
            skillDetails.setCompetencyId(result.getId());
            Skills skills = updateCompentencyMatrixSkills(skillDetailsDto.getSkills());
            skillDetails.setSkills(skills.getName());
            skillDetails.setSkillId(skills.getId());
            skillDetailsRepository.save(skillDetails);
        }
        return new ResponseEntity<Status>(new Status("200", "Compentency Matrix save successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCompentencyMatrix() throws UsernameNotFoundException {
        List<CompentencyMatrix> compentencyMatrixList = compentencyMatrixRepository.findAll();
        return new ResponseEntity<List<CompentencyMatrixDto>>(compentencyMatrixList.stream().map(user -> {
            CompentencyMatrixDto compentencyMatrixDto = new CompentencyMatrixDto(user);
            compentencyMatrixDto.setDate(dateUtil.getDateToStringDDMMYYYY(user.getDate()));
            return compentencyMatrixDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getCompentencyMatrixDetails(String id) throws UsernameNotFoundException {
        Optional<CompentencyMatrix> optionalCompentencyMatrix = compentencyMatrixRepository
                .findById(Long.parseLong(id));
        CompentencyMatrixDto compentencyMatrixDto = new CompentencyMatrixDto(optionalCompentencyMatrix.get());
        compentencyMatrixDto.setDate(dateUtil.getDateToStringDDMMYYYY(optionalCompentencyMatrix.get().getDate()));
        return new ResponseEntity<CompentencyMatrixDto>(compentencyMatrixDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteCompentencyMatrix(String id) throws UsernameNotFoundException {
        try {
            Optional<CompentencyMatrix> compentencyMatrixOptional = compentencyMatrixRepository
                    .findById(Long.parseLong(id));
            if (compentencyMatrixOptional.isPresent()) {
                CompentencyMatrix compentencyMatrix = compentencyMatrixOptional.get();
                compentencyMatrixRepository.delete(compentencyMatrix);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void save(MultipartFile file) {
        try {
            List<SkillDetails> skillDetails1 = CSVHelper.csvToSkillDetails1(file.getInputStream());
            skillDetailsRepository.saveAll(skillDetails1);
        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }

    public ByteArrayInputStream load() {
        List<SkillDetails> skillDetails1 = skillDetailsRepository.findAll();

        ByteArrayInputStream in = CSVHelper.skillDetails1ToCSV(skillDetails1);
        return in;
    }

    public List<SkillDetails> getAllSkillDetails1() {
        return skillDetailsRepository.findAll();
    }

    public ResponseEntity<?> compentencyMatrixfilterList(Long designationId, Long deptId, String fromDate, String toDate, Pageable pageable)
            throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (fromDate != null && toDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
            endDate = dateUtil.getStringTODateWk(toDate);
        } else if (fromDate != null && toDate == null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else if (fromDate == null && toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }

        Page<CompentencyMatrixList> list = compentencyMatrixRepository.compentencyMatrixfilterList(designationId, deptId, startDate, endDate, pageable);
        if (!list.toList().isEmpty()) {
            List<CompentencyMatrixListDto> result = list.toList().stream().map(data -> {
                CompentencyMatrixListDto compentencyMatrixDto = new CompentencyMatrixListDto(data);
                return compentencyMatrixDto;
            }).collect(Collectors.toList());
            PageDataDto<CompentencyMatrixListDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<CompentencyMatrixListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CompentencyMatrixListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> compentencyMatrixfilterList(Long designationId, Long deptId) throws UsernameNotFoundException {

        List<CompentencyMatrix> list = compentencyMatrixRepository.compentencyMatrixFilter(designationId, deptId);
        if (!list.isEmpty()) {
            List<CompentencyMatrixDto> result = list.stream().map(data -> {
                CompentencyMatrixDto compentencyMatrixDto = new CompentencyMatrixDto(data);
                return compentencyMatrixDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CompentencyMatrixDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CompentencyMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> deleteCompentencyMatrixSkill(Long id, Long skillId) throws UsernameNotFoundException {

        Optional<CompentencyMatrix> optionalCompentencyMatrix = compentencyMatrixRepository.findById(id);
        if (optionalCompentencyMatrix.isPresent()) {
            Optional<SkillDetails> optionalSkillDetails = skillDetailsRepository.optionalSkillDetails(id, skillId);
            if (optionalSkillDetails.isPresent()) {
                skillDetailsRepository.delete(optionalSkillDetails.get());
                return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
            }
        } else {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}