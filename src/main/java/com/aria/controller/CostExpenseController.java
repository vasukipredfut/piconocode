package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.User;
import com.aria.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CostExpenseController {

    @Autowired
    CostExpenseService costExpenseService;

    @Autowired
    ActivityService activityService;

    @Autowired
    ProScheduleService proScheduleService;

    @Autowired
    ConsZoneService consZoneService;

    @Autowired
    ConsProductsService consProductsService;

    @Autowired
    UserRepository userRepository;


    // activity details

    @PostMapping("/saveActivity")
    public ResponseEntity<?> saveActivity(@RequestBody ActivityDto activityDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return activityService.saveActivity(activityDto, user.get());
    }

    @PutMapping("/updateActivity")
    public ResponseEntity<?> updateActivity(@RequestBody ActivityDto activityDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return activityService.updateActivity(activityDto, user.get());
    }


    @GetMapping("/getActivity/{id}")
    public ResponseEntity<?> getActivity(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return activityService.getActivity(id);
    }


    @DeleteMapping("/deleteActivity/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return activityService.deleteActivity(id);
    }

    @GetMapping("/getActivityList")
    public ResponseEntity<?> getActivityList(@RequestParam(value = "journalName", required = false) String journalName,
                                             @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                             @RequestParam(value = "productvityType", required = false) String productvityType,
                                             @RequestParam(value = "productName", required = false) String productName,
                                             @RequestParam(value = "department", required = false) String department,
                                             @RequestParam(value = "processType", required = false) String processType,
                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //return costExpenseService.listAll(journalName, batchNumber, productvityType, processType, productName, department, pageable);
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // pro schedule details

    @PostMapping("/saveProSchedule")
    public ResponseEntity<?> saveProSchedule(@RequestBody ProScheduleDto proScheduleDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proScheduleService.saveProSchedule(proScheduleDto, user.get());
    }

    @PutMapping("/updateProSchedule")
    public ResponseEntity<?> updateProSchedule(@RequestBody ProScheduleDto proScheduleDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proScheduleService.updateProSchedule(proScheduleDto, user.get());
    }


    @GetMapping("/getProSchedule/{id}")
    public ResponseEntity<?> getProSchedule(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proScheduleService.getProSchedule(id);
    }


    @DeleteMapping("/deleteProSchedule/{id}")
    public ResponseEntity<?> deleteProSchedule(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return proScheduleService.deleteProSchedule(id);
    }

    @GetMapping("/getProScheduleList")
    public ResponseEntity<?> getProScheduleList(@RequestParam(value = "journalName", required = false) String journalName,
                                             @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                             @RequestParam(value = "productvityType", required = false) String productvityType,
                                             @RequestParam(value = "productName", required = false) String productName,
                                             @RequestParam(value = "department", required = false) String department,
                                             @RequestParam(value = "processType", required = false) String processType,
                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //return costExpenseService.listAll(journalName, batchNumber, productvityType, processType, productName, department, pageable);
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    // cons zone details

    @PostMapping("/saveConsZone")
    public ResponseEntity<?> saveConsZone(@RequestBody ConsZoneDto consZoneDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consZoneService.saveConsZone(consZoneDto, user.get());
    }

    @PutMapping("/updateConsZone")
    public ResponseEntity<?> updateConsZone(@RequestBody ConsZoneDto consZoneDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consZoneService.updateConsZone(consZoneDto, user.get());
    }


    @GetMapping("/getConsZone/{id}")
    public ResponseEntity<?> getConsZone(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consZoneService.getConsZone(id);
    }


    @DeleteMapping("/deleteConsZone/{id}")
    public ResponseEntity<?> deleteConsZone(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consZoneService.deleteConsZone(id);
    }

    @GetMapping("/getConsZoneList")
    public ResponseEntity<?> getConsZoneList(@RequestParam(value = "journalName", required = false) String journalName,
                                                @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                                @RequestParam(value = "productvityType", required = false) String productvityType,
                                                @RequestParam(value = "productName", required = false) String productName,
                                                @RequestParam(value = "department", required = false) String department,
                                                @RequestParam(value = "processType", required = false) String processType,
                                                Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //return costExpenseService.listAll(journalName, batchNumber, productvityType, processType, productName, department, pageable);
            return new ResponseEntity<List<ConsZoneDto>>(new ArrayList<>(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ConsZoneDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // cons products details

    @PostMapping("/saveConsProducts")
    public ResponseEntity<?> saveConsProducts(@RequestBody ConsProductsDto consProductsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consProductsService.saveConsProducts(consProductsDto, user.get());
    }

    @PutMapping("/updateConsProducts")
    public ResponseEntity<?> updateConsProducts(@RequestBody ConsProductsDto consProductsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consProductsService.updateConsProducts(consProductsDto, user.get());
    }


    @GetMapping("/getConsProducts/{id}")
    public ResponseEntity<?> getConsProducts(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consProductsService.getConsProducts(id);
    }


    @DeleteMapping("/deleteConsProducts/{id}")
    public ResponseEntity<?> deleteConsProducts(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return consProductsService.deleteConsProducts(id);
    }

    @GetMapping("/getConsProductsList")
    public ResponseEntity<?> getConsProductsList(@RequestParam(value = "journalName", required = false) String journalName,
                                             @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                             @RequestParam(value = "productvityType", required = false) String productvityType,
                                             @RequestParam(value = "productName", required = false) String productName,
                                             @RequestParam(value = "department", required = false) String department,
                                             @RequestParam(value = "processType", required = false) String processType,
                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //return costExpenseService.listAll(journalName, batchNumber, productvityType, processType, productName, department, pageable);
            return new ResponseEntity<List<ConsProductsDto>>(new ArrayList<>(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ConsProductsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    // cost expense

    @PostMapping("/saveCostExpense")
    public ResponseEntity<?> saveCostExpense(@RequestBody CostExpenseDto expenseDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return costExpenseService.saveCostExpense(expenseDto, user.get());
    }

    @PutMapping("/updateCostExpense")
    public ResponseEntity<?> updateCostExpense(@RequestBody CostExpenseDto expenseDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return costExpenseService.updateCostExpense(expenseDto, user.get());
    }


    @GetMapping("/getCostExpense/{id}")
    public ResponseEntity<?> getCostExpense(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return costExpenseService.getCostExpense(id);
    }


    @DeleteMapping("/deleteCostExpense/{id}")
    public ResponseEntity<?> deleteJournal(@PathVariable String id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return costExpenseService.deleteCostExpense(id);
    }

    @GetMapping("/getCostExpenseList")
    public ResponseEntity<?> getCostExpense(@RequestParam(value = "journalName", required = false) String journalName,
                                            @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                            @RequestParam(value = "productvityType", required = false) String productvityType,
                                            @RequestParam(value = "productName", required = false) String productName,
                                            @RequestParam(value = "department", required = false) String department,
                                            @RequestParam(value = "processType", required = false) String processType,
                                            Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            //return costExpenseService.listAll(journalName, batchNumber, productvityType, processType, productName, department, pageable);
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

   /* @GetMapping("/getJournalNameList")
    public ResponseEntity<?> getJournalName(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return costExpenseService.listAllJournal(user.get());
        } else {
            return new ResponseEntity<List<JournalNameListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getPreprocessList")
    public ResponseEntity<?> getPreprocess(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return costExpenseService.listAllPreprocess();
        } else {
            return new ResponseEntity<List<PreprocessDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getJournalProductOrMachineList")
    public ResponseEntity<?> getJournal(@RequestParam("journalId") Long journalId,
                                        @RequestParam("type") String type, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (type.equalsIgnoreCase("product")) {
                return costExpenseService.listProductAll(journalId);
            } else {
                return costExpenseService.listMachineAll(journalId, user.get());
            }
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }*/


}
