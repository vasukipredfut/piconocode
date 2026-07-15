package com.aria.controller;

import com.aria.config.ResponseMessage;
import com.aria.dao.UserRepository;
import com.aria.dto.CWagesListDTO;
import com.aria.dto.PageDataDto;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.CwagesService;
import com.aria.utils.CSVHelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class CwagesController {


    @Autowired
    CwagesService cwagesService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CSVHelperUtil csvHelperUtil;


    // CWages Salary Structure Define


    @PostMapping("/uploadCwagesSalaryStructure")
    public ResponseEntity<ResponseMessage> uploadCwagesSalaryStructure(@RequestParam("file") MultipartFile file, Authentication authentication) {


        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        String message = "";

        if (csvHelperUtil.hasCSVFormat(file)) {

            try {

                message = csvHelperUtil.checkUploadCwagesSalaryStructure(file.getInputStream());
                if (message.equalsIgnoreCase("success")) {

                    cwagesService.uploadCwagesSalaryStructure(file, user.get());

                    message = "Uploaded the file successfully: " + file.getOriginalFilename();
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
                } else  if (message.equalsIgnoreCase("empty")) {
                    message = "Uploaded the file is Empty Please fill AND Import..... :" + file.getOriginalFilename();
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseMessage(message));
                }else {
                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
                }

            } catch (Exception e) {
                e.printStackTrace();
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));

            }
        }

        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }


    @PostMapping("/downloadCwagesSalaryStructure")
    public void downloadCwagesSalaryStructure(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                              @RequestParam(value = "type", required = false) String type,
                                              HttpServletResponse servletResponse,
                                              Authentication authentication) throws IOException {


        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"cwagessalarystucture.csv\"");
        cwagesService.downloadCwagesSalaryStructure(servletResponse.getWriter(), trnYear, type);

    }


    @GetMapping("/rejectCwagesSalaryStructureList")
    public ResponseEntity<?> rejectSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "designationId", required = false) Long designationId,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.rejectCwagesSalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/verifyCwagesSalaryStructureList")
    public ResponseEntity<?> verifyCwagesSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                             @RequestParam(value = "status", required = false) String status,
                                                             @RequestParam(value = "designationId", required = false) Long designationId,
                                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.verifyCwagesSalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/inactiveCwagesSalaryStructureList")
    public ResponseEntity<?> inactiveCwagesSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                               @RequestParam(value = "status", required = false) String status,
                                                               @RequestParam(value = "designationId", required = false) Long designationId,
                                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.inactiveCwagesSalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/uploadCwagesSalaryStructureList")
    public ResponseEntity<?> uploadCwagesSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                             @RequestParam(value = "status", required = false) String status,
                                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.uploadCwagesSalaryStructureList(trnYear, status);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/cWagesList")
    public ResponseEntity<?> cWagesList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "journal", required = false) String journal,
            @RequestParam(value = "type", required = false) Long type,
            @RequestParam(value = "salCodeNo", required = false) String salCodeNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "year", required = false) Long year,
            @RequestParam(value = "pageFlag", required = true) boolean pageFlag,
            Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.cWagesList(name, journal, type, salCodeNo, designation, status, year, pageFlag,  pageable);
        } else {
            return new ResponseEntity<PageDataDto<CWagesListDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }




}
