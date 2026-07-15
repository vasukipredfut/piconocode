package com.aria.service;

import com.aria.dao.SalaryStructureRepo;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.CSVHelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Tuple;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryStructureService {


    @Autowired
    SalaryStructureRepo salaryStructureRepo;

    @Autowired
    CSVHelperUtil csvHelperUtil;


    public ResponseEntity<?> saveSalaryStructure(SalaryStructureDto salaryStructureDto, User loggedUser) {

        SalaryStructure salary = new SalaryStructure(salaryStructureDto);

        SalaryStructure result = salaryStructureRepo.save(salary);

        return new ResponseEntity<Status>(new Status("200", "salary save successfully!!!"), HttpStatus.OK);
    }

    public void uploadSalaryStructure(MultipartFile file, User loggedUser) {

        try {
            List<SalaryStructure> salaryStructures = csvHelperUtil.uploadSalaryStructure(file.getInputStream());
            if(!salaryStructures.isEmpty()) {
                    salaryStructureRepo.saveAllAndFlush(salaryStructures);
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }


    public void downloadSalaryStructure(Writer writer, Long trnYear, String type) {
        List<SalaryStructure> salaryStructures = new ArrayList<>();
        if (type != null) {
            salaryStructures = salaryStructureRepo.findBy(trnYear);
            csvHelperUtil.writeSalaryStructureToCsv(writer, salaryStructures);
        }else {
            csvHelperUtil.writeSalaryStructureToCsv(writer);
        }

    }


    public ResponseEntity<?> findAll(Long trnYear, String status, String designation, String salaryType, String applyTo, boolean pageFlag, Pageable pageable) {


        if(pageFlag) {
            Page<SalaryStructureList> list = salaryStructureRepo.findByFilterList(trnYear, status, designation, salaryType, applyTo, pageable);

            if (!list.isEmpty()) {
                List<SalaryStructureListDTO> result = list.toList().stream().map(data -> {
                    SalaryStructureListDTO salaryDto = new SalaryStructureListDTO(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                PageDataDto<SalaryStructureListDTO> pageData = new PageDataDto<>(result, list);
                return new ResponseEntity<PageDataDto<SalaryStructureListDTO>>(pageData, HttpStatus.OK);
            } else {
                return new ResponseEntity<PageDataDto<SalaryStructureListDTO>>(new PageDataDto(), HttpStatus.OK);
            }
        } else {
            List<SalaryStructureList> list = salaryStructureRepo.findByFilterList(trnYear, status, designation, salaryType, applyTo);

            if (!list.isEmpty()) {
                List<SalaryStructureListDTO> result = list.stream().map(data -> {
                    SalaryStructureListDTO salaryDto = new SalaryStructureListDTO(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<List<SalaryStructureListDTO>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<SalaryStructureListDTO>>(new ArrayList<>(), HttpStatus.OK);
            }
        }
    }


    public ResponseEntity<?> rejectSalaryStructureList(Long trnYear, String status, Long designationId) {

        List<SalaryStructure> list = salaryStructureRepo.findByRejectOrVerifyList(trnYear, status, designationId);

        if (!list.isEmpty()) {
            salaryStructureRepo.deleteAll(list);
            return new ResponseEntity<Status>(new Status("200", "Salary Structure List Rejected Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Structure List Rejected Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> verifySalaryStructureList(Long trnYear, String status, Long designationId) {
        List<SalaryStructure> list = salaryStructureRepo.findByRejectOrVerifyList(trnYear, status, designationId);
        if (!list.isEmpty()) {
            for(SalaryStructure salaryStructure : list) {
                salaryStructure.setStatus("A");
                salaryStructureRepo.save(salaryStructure);
            }
            return new ResponseEntity<Status>(new Status("200", "Salary Structure List Verify Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Structure List Verify Not Successfully!!!"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> inactiveSalaryStructureList(Long trnYear, String status, Long designationId) {
        List<SalaryStructure> list = salaryStructureRepo.findByRejectOrVerifyList(trnYear, status, designationId);
        if (!list.isEmpty()) {
            for(SalaryStructure salaryStructure : list) {
                salaryStructure.setStatus("N");
                salaryStructureRepo.save(salaryStructure);
            }
            return new ResponseEntity<Status>(new Status("200", "Salary Structure List inactive Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Structure List inactive Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> wagesDesignationList( String designation) {
        List<Tuple> list = salaryStructureRepo.wagesDesignationList(designation);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
    }


}
