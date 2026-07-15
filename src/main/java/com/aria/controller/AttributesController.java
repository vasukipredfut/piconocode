package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.AttributesService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AttributesController {

    @Autowired
    AttributesService attributesService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/assignAttributes")
    public ResponseEntity<?> getAllAssignAttributesDetails(Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getAllAssignAttributesDetails1(user.get(), pageable);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/kpiName")
    public ResponseEntity<?> getAllKpiName(@RequestParam(value = "kpiName",required = false) String kpiName,
                                           @RequestParam(value = "modeOfEntry",required = false) String modeOfEntry,
                                           Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getAllKpiName(kpiName, modeOfEntry, user.get(), pageable);
        } else {
            return new ResponseEntity<List<KpinameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/attributes/{id}")
    public ResponseEntity<?> getDutyTransferDetails(@PathVariable Long id, Authentication authentication) {
        return attributesService.getAttributesDetails(id);
    }

    @DeleteMapping("/attributes/{id}")
    public ResponseEntity<?> deleteShiftsDetails(@PathVariable Long id, Authentication authentication) {
        return attributesService.deleteAttributes(id);
    }

    @PostMapping("/attributes/saveAttributes")
    public ResponseEntity<?> saveAttributes(@RequestBody List<AttributesDto> reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.saveAttributesFields(reqDto, user.get());
    }

    @PutMapping("/attributes/updateAttributes")
    public ResponseEntity<?> updateAttributes(@RequestBody List<AttributesDto> reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.updateAttributesFields(reqDto, user.get());
    }

    @DeleteMapping("/attributes/deleteAttributes/{id}")
    public ResponseEntity<?> deleteAttributesFields(@PathVariable Long id, Authentication authentication) {
        return attributesService.deleteAttributesFields(id);
    }


    @GetMapping("/attributes/{taskNameId}/{taskCategoryId}")
    public ResponseEntity<?> getAttributes(@PathVariable Long taskNameId, @PathVariable Long taskCategoryId,
                                           Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getAttributesDetails(taskNameId, taskCategoryId, pageable);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }





    // Added By Alagu


    @GetMapping("/kpiName/steps/{kpiNameId}")
    public ResponseEntity<?> getKpiNameStepsDetails(@PathVariable Long kpiNameId, Pageable pageable,
                                                    Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getKpiNameStepsDetails(kpiNameId, pageable);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/kpiName/steps")
    public ResponseEntity<?> addKpiNameSteps(@RequestBody ArrayList<KpiDto> reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.addKpiNameSteps(reqDto, user.get());
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/kpiName/steps/{id}")
    public ResponseEntity<?> deleteKpiNameSteps(@PathVariable Long id, Authentication authentication) {
        return attributesService.deleteKpiNameSteps(id);
    }

    // Attributes table alone

    @GetMapping("/attributes/searchAttributes")
    public ResponseEntity<?> saveAttributes(@RequestParam(value = "tgId") Long tgId,
                                            @RequestParam(value = "attributesName") String attributesName,
                                            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.searchAttributes(tgId, attributesName);
    }

    @GetMapping("/attributes/searchAllAttributes")
    public ResponseEntity<?> searchAllAttributes(@RequestParam(value = "attributesName", required = false) String attributesName,
                                            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.searchAllAttributes(attributesName);
    }

    @PostMapping("/attributes/saveAttributeOption")
    public ResponseEntity<?> saveAttributeOption(@RequestBody List<String> optionList,
                                                 @RequestParam(value = "attributesId") Long attributesId,

                                                 Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.saveAttributeOption(optionList, attributesId);
    }


    @PostMapping("/attributes/deleteAttributeOption")
    public ResponseEntity<?> deleteAttributeOption(@RequestBody AttributeFieldsProcessTypeOptionBasedDto option,
                                                 Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.deleteAttributeOption(option);
    }


    @GetMapping("/attributes/getAllAttributes")
    public ResponseEntity<?> getAllAttributes(@RequestParam(value = "kpiNameId") Long kpiNameId,
                                                           @RequestParam(value = "batchNo", required = false) String batchNo,
                                                           @RequestParam(value = "entryType", required = false) String entryType,
                                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getAllAttributes(kpiNameId, batchNo, entryType);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Maintenance

    @GetMapping("/maintenance")
    public ResponseEntity<?> getMaintenanceList(@RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                                @RequestParam(value = "taskId", required = false) Long taskId,
                                                @RequestParam(value = "taskCategory", required = false) String taskCategory,
                                                @RequestParam(value = "taskName", required = false) String taskName,
                                                Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getMaintenanceList(taskCategoryId, taskId, taskCategory, taskName, pageable);
        } else {
            return new ResponseEntity<List<MaintenanceListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/maintenanceDetails")
    public ResponseEntity<?> getMaintenanceDetails(@RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                                   @RequestParam(value = "taskId", required = false) Long taskId,
                                                   @RequestParam(value = "taskGroupMapId", required = false) Long taskGroupMapId,
                                                   Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getMaintenanceAttributesDetails(taskId, taskCategoryId, taskGroupMapId);
        } else {
            return new ResponseEntity<List<MaintenanceListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/maintenanceAttributesList")
    public ResponseEntity<?> maintenanceAttributesList(@RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                                       @RequestParam(value = "taskId", required = false) Long taskId,
                                                       @RequestParam(value = "taskGroupMapId", required = false) Long taskGroupMapId,
                                                       Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.getMaintenanceAttributes(taskGroupMapId, taskId, taskCategoryId);
        } else {
            return new ResponseEntity<List<MaintenanceListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/pmScheduleMaintenanceAttributesList")
    public ResponseEntity<?> pmScheduleMaintenanceAttributesList(@RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                                       @RequestParam(value = "taskId", required = false) Long taskId,
                                                       @RequestParam(value = "taskGroupId", required = false) Long taskGroupId,
                                                       Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.pmScheduleMaintenanceAttributesList(taskGroupId, taskId, taskCategoryId);
        } else {
            return new ResponseEntity<List<MaintenanceListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    @GetMapping("/auth/updateList")
    public ResponseEntity<?> updateList() {
        return attributesService.updateList();
    }


    

    // Save attributes in ProReportDb table

    @PutMapping("/attributes/saveProreportDbAttributes")
    public ResponseEntity<?> saveProReportDbAttributes(@RequestBody List<ProreprtdbDto> reqDto,
                                                       Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attributesService.saveProreportDbAttributes(reqDto, user.get());
    }

    @DeleteMapping("/attributes/deleteProreportDbAttributes/{id}")
    public ResponseEntity<?> deleteProReportDbAttributes(@PathVariable Long id, Authentication authentication) {
        return attributesService.deleteProreportDbAttributes(id);
    }

    @GetMapping("/formulaAllAttributes")
    public ResponseEntity<?> formulaAllAttributes(@RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
                                                  @RequestParam(value = "id", required = false) Long id,
                                                  @RequestParam(value = "tgId", required = false) Long tgId,
                                                  @RequestParam(value = "attName", required = false) String attName,
                                              Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attributesService.formulaAllAttributes(kpiNameId,id, tgId, attName);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


}
