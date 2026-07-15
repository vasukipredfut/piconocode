
package com.aria.controller;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
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
public class JournalController {

    @Autowired
    JournalService journalService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveJournal")
    public ResponseEntity<?> saveJournal(@RequestBody JournalDto journalDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return journalService.saveJournal(journalDto, user.get());
    }

    @PutMapping("/updateJournal")
    public ResponseEntity<?> updateJournal(@RequestBody JournalDto journalDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return journalService.updateJournal(journalDto, user.get());
    }

    @GetMapping("/getJournalList")
    public ResponseEntity<?> getJournal(@RequestParam(value = "journalName", required = false) String journalName,
                                        @RequestParam(value = "status", required = false) String status,
                                        @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                        @RequestParam(value = "productvityType", required = false) String productvityType,
                                        @RequestParam(value = "productName", required = false) String productName,
                                        @RequestParam(value = "department", required = false) String department,
                                        @RequestParam(value = "processType", required = false) String processType,
                                        Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (status == null) {
            status = "Y";
        }

        if (user.get() != null) {
            return journalService.listAll(status, journalName, batchNumber, productvityType, processType, productName, department, pageable);
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getJournalNameList")
    public ResponseEntity<?> getJournalName(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return journalService.listAllJournal(user.get());
        } else {
            return new ResponseEntity<List<JournalNameListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getPreprocessList")
    public ResponseEntity<?> getPreprocess(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return journalService.listAllPreprocess();
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
                return journalService.listProductAll(journalId);
            } else {
                return journalService.listMachineAll(journalId, user.get());
            }
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getJournal/{id}")
    public ResponseEntity<?> getJournal(@PathVariable String id, Authentication authentication) {
        return journalService.getJournal(id);
    }

    @DeleteMapping("/deleteJournal/{id}")
    public ResponseEntity<?> deleteJournal(@PathVariable String id, Authentication authentication) {
        return journalService.deleteJournal(id);
    }

    @GetMapping("/getJournalSelectionListByDept")
    public ResponseEntity<?> getJournal(@RequestParam(value = "dept", required = false) Long dept,
                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (dept != null) {
                return journalService.listAll(dept);
            } else {
                return journalService.listAll(user.get().getDepartment().getId());
            }
        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/blendingJournalList")
    public ResponseEntity<?> findBlendingEnabled(
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {

            return journalService.findBlendingEnabled();

        } else {
            return new ResponseEntity<List<JournalDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getProductionDataSetList")
    public ResponseEntity<?> getDataSetList(@RequestParam("JournalId") Long journalId,
                                            @RequestParam(value = "withType", required = false) String withType,
                                            @RequestParam(value = "withId", required = false) Long withId,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return journalService.listAllDataSet(journalId, user.get(), withType, withId);
        } else {
            return new ResponseEntity<List<PAttributesDto>>(HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/removeJournalMap")
    public ResponseEntity<?> removeJournalMap(@RequestParam("id") Long id,
                                              @RequestParam("type") String type,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return journalService.removeJournalMap(id, type);
    }


    @DeleteMapping("/removeMultipleJournalMap")
    public ResponseEntity<?> removeJournalMap(@RequestParam("idList") String idList,
                                              @RequestParam("type") String type,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return journalService.removeMultipleJournalMap(idList, type);
    }


    @GetMapping("/inActiveJournal/{id}")
    public ResponseEntity<?> inActiveJournal(@PathVariable String id, Authentication authentication) {
        return journalService.inActiveJournal(id);
    }

    @GetMapping("/asrsJournalNameList")
    public ResponseEntity<?> asrsJournalNameList(Authentication authentication, @RequestParam("name") String name) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return journalService.asrsJournalNameList(name, user.get());
        } else {
            return new ResponseEntity<List<JournalNameListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}
