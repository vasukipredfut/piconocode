package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.PAttributesDto;
import com.aria.dto.PAttributesMappingDto;
import com.aria.model.User;
import com.aria.service.PAttributesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PAttributesController {

    @Autowired
    PAttributesService pAttributesService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/savePAttributes")
    public ResponseEntity<?> savePAttributes(@RequestBody List<PAttributesDto> pAttributesDto,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pAttributesService.savePAttributes(pAttributesDto, user.get());
    }

    @PutMapping("/updatePAttributes")
    public ResponseEntity<?> updatePAttributes(@RequestBody List<PAttributesDto> pAttributesDto,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pAttributesService.updatePAttributes(pAttributesDto, user.get());
    }

    @GetMapping("/getPAttributesList")
    public ResponseEntity<?> getPAttributesList(@RequestParam(value = "attributesName",required = false) String attributesName,
                                                @RequestParam(value = "deptName",required = false) String  deptName,Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pAttributesService.listAll(attributesName,deptName,pageable);
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getPAttributes/{id}")
    public ResponseEntity<?> getPAttributes(@PathVariable String id, Authentication authentication) {
        return pAttributesService.getPAttributes(id);
    }

    @DeleteMapping("/deletePAttributes/{id}")
    public ResponseEntity<?> deletePAttributes(@PathVariable String id, Authentication authentication) {
        return pAttributesService.deletePAttributes(id);
    }


    // Sj Data set

    @PostMapping("/saveSJDataSet")
    public ResponseEntity<?> savePAttributesMapping(@RequestBody PAttributesMappingDto pAttributesDto,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pAttributesService.savePAttributesMapping(pAttributesDto, user.get());
    }

    @PutMapping("/updateSJDataSet")
    public ResponseEntity<?> updatePAttributesMapping(@RequestBody PAttributesMappingDto pAttributesDto,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return pAttributesService.updatePAttributesMapping(pAttributesDto, user.get());
    }

    @GetMapping("/getSJDataSetList")
    public ResponseEntity<?> getSJDataSetList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pAttributesService.listAllPAttributesMapping();
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getDataSetList")
    public ResponseEntity<?> getDataSetList(@RequestParam("JournalId") Long journalId,
                                            @RequestParam("productId") Long productId,
                                            @RequestParam("machineId") Long machineId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pAttributesService.listAllDataSet(journalId, productId , machineId);
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getDataSetListwJournalId")
    public ResponseEntity<?> getDataSetList(@RequestParam("JournalId") Long journalId,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pAttributesService.listAllDataSet(journalId);
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }

/*
    @GetMapping("/getProductionDataSetList")
    public ResponseEntity<?> getDataSetList(@RequestParam("JournalId") Long journalId,
                                            @RequestParam(value = "withType", required = false) String withType,
                                            @RequestParam(value = "withId",required = false) Long withId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return pAttributesService.listAllDataSet(journalId, user.get(),    withType, withId );
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }*/




    @GetMapping("/getSJDataSetDetails")
    public ResponseEntity<?> getSJDataSetDetails(@RequestParam("JournalId") Long journalId,
                                                 @RequestParam("withType") String withType,
                                                 @RequestParam("withId") Long withId , Authentication authentication) {
        return pAttributesService.getPAttributesMapping(journalId, withType , withId);
    }


    @GetMapping("/getSJDataSet/{id}")
    public ResponseEntity<?> getSJDataSet(@PathVariable String id, Authentication authentication) {
        return pAttributesService.getPAttributesMapping(id);
    }


    @DeleteMapping("/deleteSJDataSet/{id}")
    public ResponseEntity<?> deleteSJDataSet(@PathVariable String id, Authentication authentication) {
        return pAttributesService.deletePAttributesMapping(id);
    }

    @GetMapping("/getSJMachine/{id}")
    public ResponseEntity<?> getSJMachine(@PathVariable String id, Authentication authentication) {
        return pAttributesService.getSjMachine(id);
    }
}
