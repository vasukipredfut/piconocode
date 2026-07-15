package com.aria.controller;

import com.aria.config.ResponseMessage;
import com.aria.dao.UserRepository;
import com.aria.dto.CompentencyMatrixDto;
import com.aria.model.CSVHelper;
import com.aria.model.SkillDetails;
import com.aria.model.User;
import com.aria.service.CompentencyMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CompentencyMatrixController {


    @Autowired
    CompentencyMatrixService compentencyMatrixService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/compentencyMatrix")
    public ResponseEntity<?> saveCompentencyMatrix(@RequestBody CompentencyMatrixDto compentencyMatrixDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return compentencyMatrixService.saveCompentencyMatrix(compentencyMatrixDto, user.get());
    }

    @PutMapping("/compentencyMatrix")
    public ResponseEntity<?> updateCompentencyMatrix(@RequestBody CompentencyMatrixDto compentencyMatrixDto,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return compentencyMatrixService.updateCompentencyMatrix(compentencyMatrixDto, user.get());
    }

    @GetMapping("/compentencyMatrix/{id}")
    public ResponseEntity<?> getCompentencyMatrixDetails(@PathVariable String id, Authentication authentication) {

        return compentencyMatrixService.getCompentencyMatrixDetails(id);
    }

    @DeleteMapping("/compentencyMatrix/{id}")
    public ResponseEntity<?> deleteCompentencyMatrixDetails(@PathVariable String id, Authentication authentication) {

        return compentencyMatrixService.deleteCompentencyMatrix(id);
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.hasCSVFormat(file)) {
            try {
                compentencyMatrixService.save(file);

                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }

        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

    @GetMapping("/compentencyMatrixSkills")
    public ResponseEntity<List<SkillDetails>> getAllskillDetails1() {
        try {
            List<SkillDetails> skillDetails1 = compentencyMatrixService.getAllSkillDetails1();

            if (skillDetails1.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(skillDetails1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> getFile() {
        String filename = "compentencyMatrix.csv";
        InputStreamResource file = new InputStreamResource(compentencyMatrixService.load());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }


    @GetMapping("/compentencyMatrix")
    public ResponseEntity<?> compentencyMatrix(
            @RequestParam(value = "designationId", required = false) Long designationId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return compentencyMatrixService.compentencyMatrixfilterList(designationId, deptId, dateFrom, dateTo, pageable);
        } else {
            return new ResponseEntity<List<CompentencyMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/compentencyMatrixFilter")
    public ResponseEntity<?> compentencyMatrixFilter(
            @RequestParam(value = "designationId", required = false) Long designationId,
            @RequestParam(value = "deptId", required = false) Long deptId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return compentencyMatrixService.compentencyMatrixfilterList(designationId, deptId);
        } else {
            return new ResponseEntity<List<CompentencyMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @DeleteMapping("/deleteCompentencyMatrixSkill")
    public ResponseEntity<?> deleteCompentencyMatrixSkill(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "skillId", required = false) Long skillId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return compentencyMatrixService.deleteCompentencyMatrixSkill(id, skillId);
        } else {
            return new ResponseEntity<List<CompentencyMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
	

	


