package com.aria.service;

import com.aria.dao.MasterComponentRepository;
import com.aria.dao.MasterFieldRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.MasterComponentDto;
import com.aria.dto.MasterFieldDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasterService {

    @Autowired
    MasterComponentRepository componentRepository;

    @Autowired
    MasterFieldRepository fieldRepository;

    @Autowired
    UserRepository userRepository;

    public ResponseEntity<?> saveComponent(MasterComponentDto componentDto, User loggedUser) {
        MasterComponent component = new MasterComponent(componentDto);
        component.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        component.setCreatedBy(loggedUser.getId());
        MasterComponent masterComponent = componentRepository.save(component);
        List<MasterFieldDto> fieldDtoList = componentDto.getFieldList();
        for (MasterFieldDto fieldDto : fieldDtoList) {
            MasterField masterField = new MasterField(fieldDto);
            masterField.setMasterComponentId(masterComponent.getId());
            masterField.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            masterField.setCreatedBy(loggedUser.getId());
            fieldRepository.save(masterField);
        }
        return new ResponseEntity<Status>(new Status("200", "master component saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateComponent(MasterComponentDto componentDto, User loggedUser) {
        MasterComponent component = new MasterComponent(componentDto);
        component.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        component.setUpdatedBy(loggedUser.getId());
        MasterComponent masterComponent = componentRepository.save(component);
        List<MasterFieldDto> fieldDtoList = componentDto.getFieldList();
        for (MasterFieldDto fieldDto : fieldDtoList) {
            MasterField masterField = new MasterField(fieldDto);
            masterField.setMasterComponentId(masterComponent.getId());
            masterField.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            masterField.setUpdatedBy(loggedUser.getId());
            fieldRepository.save(masterField);
        }
        return new ResponseEntity<Status>(new Status("200", "master component updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllComponent(Pageable pageable) throws UsernameNotFoundException {

        Page<MasterComponent> masterComponentList = componentRepository.findAll(pageable);
        if (!masterComponentList.isEmpty()) {
            List<MasterComponentDto> result = masterComponentList.toList().stream().map(data -> {
                MasterComponentDto masterComponentDto = new MasterComponentDto(data);
                if (data.getFieldList() != null) {
                    masterComponentDto.setFieldList(data.getFieldList().stream().map(field -> {
                        MasterFieldDto masterFieldDto = new MasterFieldDto(field);
                        updateDetails(masterFieldDto);
                        return masterFieldDto;
                    }).collect(Collectors.toList()));
                }
                updateEmployeeDetails(masterComponentDto);
                return masterComponentDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MasterComponentDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MasterComponentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getMasterComponentDetails(String id) throws UsernameNotFoundException {
        Optional<MasterComponent> optionalMasterComponent = componentRepository.findById(Long.parseLong(id));
        MasterComponentDto masterComponentDto = new MasterComponentDto(optionalMasterComponent.get());
        if (optionalMasterComponent.get().getFieldList() != null) {
            masterComponentDto.setFieldList(optionalMasterComponent.get().getFieldList().stream().map(field -> {
                MasterFieldDto masterFieldDto = new MasterFieldDto(field);
                updateDetails(masterFieldDto);
                return masterFieldDto;
            }).collect(Collectors.toList()));
        }
        updateEmployeeDetails(masterComponentDto);
        return new ResponseEntity<MasterComponentDto>(masterComponentDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteComponent(String componentId) throws UsernameNotFoundException {

        try {
            Optional<MasterComponent> componentOptional = componentRepository.findById(Long.parseLong(componentId));
            if (componentOptional.isPresent()) {
                MasterComponent user = componentOptional.get();
                componentRepository.delete(user);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateEmployeeDetails(MasterComponentDto pageDto) {

        Optional<User> user = userRepository.findById(pageDto.getCreatedBy());
        if (user.isPresent()) {
            pageDto.setCreatedByName(user.get().getEmployeename());
            pageDto.setUpdatedByName(user.get().getEmployeename());
        }
        if (pageDto.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(pageDto.getUpdatedBy());
            if (user1.isPresent()) {
                pageDto.setUpdatedByName(user.get().getEmployeename());
            }
        }
    }

    public void updateDetails(MasterFieldDto pageDto) {

        Optional<User> user = userRepository.findById(pageDto.getCreatedBy());
        if (user.isPresent()) {
            pageDto.setCreatedByName(user.get().getEmployeename());
            pageDto.setUpdatedByName(user.get().getEmployeename());
        }
        if (pageDto.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(pageDto.getUpdatedBy());
            if (user1.isPresent()) {
                pageDto.setUpdatedByName(user.get().getEmployeename());
            }
        }


    }
}
