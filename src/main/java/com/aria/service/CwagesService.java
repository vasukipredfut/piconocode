package com.aria.service;

import com.aria.dao.CwagesRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.Cwages;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.CSVHelperUtil;
import com.aria.utils.DateUtil;
import com.aria.utils.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CwagesService {

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CwagesRepository cwagesRepository;


    @Autowired
    CSVHelperUtil csvHelperUtil;


    public void uploadCwagesSalaryStructure(MultipartFile file, User loggedUser) {

        try {
            List<Cwages> salaryStructures = csvHelperUtil.uploadCwagesSalaryStructure(file.getInputStream());
            if (!salaryStructures.isEmpty()) {
                for (Cwages cwages : salaryStructures) {
                    cwages.setUsecid(loggedUser.getId().toString());
                    Cwages rs = cwagesRepository.save(cwages);
                    rs.setCodeno("C" + rs.getId());
                    cwagesRepository.save(rs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }


    public void downloadCwagesSalaryStructure(Writer writer, Long trnYear, String type) {
        List<CwagesStructureList> salaryStructures = new ArrayList<>();
        if (type != null) {
            salaryStructures = cwagesRepository.findBy(trnYear);
            csvHelperUtil.writeCwagesSalaryStructureToCsv(writer, salaryStructures);
        } else {
            csvHelperUtil.writeCwagesSalaryStructureHEARDSToCsv(writer);
        }

    }


    public ResponseEntity<?> cWagesList(String name, String journal, Long type, String salCodeNo, String designation, String status, Long year, boolean pageFlag, Pageable pageable) throws UsernameNotFoundException {
       if(pageFlag) {
           Page<CWagesDTOList> list = cwagesRepository.findBy(name, journal, type, salCodeNo, designation, status, year, pageable);
           if (!list.isEmpty()) {
               List<CWagesListDTO> result = list.stream().map(data -> {
                   CWagesListDTO cwagesReqDto = new CWagesListDTO(data);
                   return cwagesReqDto;
               }).collect(Collectors.toList());
               PageDataDto<CWagesListDTO> pageData = new PageDataDto(result, list);
               return new ResponseEntity<PageDataDto<CWagesListDTO>>(pageData, HttpStatus.OK);
           } else {
               return new ResponseEntity<PageDataDto<CWagesListDTO>>(new PageDataDto(), HttpStatus.OK);
           }
       }else {
           List<CWagesDTOList> list = cwagesRepository.findBy(name, journal, type, salCodeNo, designation, status, year);
           if (!list.isEmpty()) {
               List<CWagesListDTO> result = list.stream().map(data -> {
                   CWagesListDTO cwagesReqDto = new CWagesListDTO(data);
                   return cwagesReqDto;
               }).collect(Collectors.toList());
               return new ResponseEntity<List<CWagesListDTO>>(result, HttpStatus.OK);
           } else {
               return new ResponseEntity<List<CWagesListDTO>>(new ArrayList<>(), HttpStatus.OK);
           }
       }
    }

    public ResponseEntity<?> rejectCwagesSalaryStructureList(Long trnYear, String status, Long designationId) {
        List<Cwages> list = cwagesRepository.findByRejectOrVerifyList(trnYear, status, designationId);
        if (!list.isEmpty()) {
            cwagesRepository.deleteAll(list);
            return new ResponseEntity<Status>(new Status("200", "Cwages Salary Structure List Rejected Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Cwages Salary Structure List Rejected Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> verifyCwagesSalaryStructureList(Long trnYear, String status, Long designationId) {
        List<Cwages> list = cwagesRepository.findByRejectOrVerifyList(trnYear, status, designationId);
        if (!list.isEmpty()) {
            for (Cwages salaryStructure : list) {
                salaryStructure.setActivity("A");
                cwagesRepository.save(salaryStructure);
            }
            return new ResponseEntity<Status>(new Status("200", "Salary Structure List Verify Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Structure List Verify Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> inactiveCwagesSalaryStructureList(Long trnYear, String status, Long designationId) {
        List<Cwages> list = cwagesRepository.findByRejectOrVerifyList(trnYear, status, designationId);
        if (!list.isEmpty()) {
            for (Cwages salaryStructure : list) {
                salaryStructure.setActivity("N");
                cwagesRepository.save(salaryStructure);
            }
            return new ResponseEntity<Status>(new Status("200", "Salary Structure List Inactive Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Salary Structure List Inactive Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> uploadCwagesSalaryStructureList(Long trnYear, String status) {
        List<CwagesStructureList> list = cwagesRepository.findBy(trnYear, status);
        if (!list.isEmpty()) {
            List<CwagesStructureListDTO> result = list.stream().map(data -> {
                CwagesStructureListDTO cwagesReqDto = new CwagesStructureListDTO(data);
                return cwagesReqDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CwagesStructureListDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CwagesStructureListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<List<cwagesDTO>> getCWagesList(Long refId, TransactionType transactionType) {
        String type = transactionType.name();
        List<cwagesList> cwagesList = cwagesRepository.getCwagesFilterListByTransactionType(refId, type);
        List<cwagesDTO> list = cwagesList.stream().map(data -> new cwagesDTO(data)).collect(Collectors.toList());

        return new ResponseEntity<List<cwagesDTO>>(list, HttpStatus.OK);
    }
}
