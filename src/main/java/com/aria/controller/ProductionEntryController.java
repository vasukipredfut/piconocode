
package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.User;
import com.aria.service.ProductionEntryService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductionEntryController {

    @Autowired
    ProductionEntryService productionEntryService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveProductionEntry")
    public ResponseEntity<?> saveProductionEntry(@RequestBody ProductionEntryForm productionEntryDto,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productionEntryService.saveProductionEntry(productionEntryDto, user.get());
    }

    @PutMapping("/updateProductionEntry")
    public ResponseEntity<?> updateProductionEntry(@RequestBody ProductionEntryDto productionEntryDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return productionEntryService.updateProductionEntry(productionEntryDto, user.get());
    }

    @GetMapping("/getProductionEntryList")
    public ResponseEntity<?> getProductionEntry(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.listAll(pageable);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductionEntryFilterList")
    public ResponseEntity<?> getProductionEntryFilterList(@RequestParam(value = "journalName", required = false) String journalName,
                                                          @RequestParam(value = "journalId", required = false) Long journalId,
                                                          @RequestParam(value = "firmId", required = false) Long firmId,
                                                          @RequestParam(value = "branch", required = false) Long branch,
                                                          @RequestParam(value = "dept", required = false) Long dept,
                                                          @RequestParam(value = "status", required = false) String status,
                                                          @RequestParam(value = "fromDate", required = false) String fromDate,
                                                          @RequestParam(value = "toDate", required = false) String toDate, Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getProductionEntryFilterList(branch, dept, journalId, journalName, status, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductionEntry/{id}")
    public ResponseEntity<?> getProductionEntry(@PathVariable String id, Authentication authentication) {
        return productionEntryService.getProductionEntry(id);
    }

    @DeleteMapping("/deleteProductionEntry/{id}")
    public ResponseEntity<?> deleteProductionEntry(@PathVariable String id, Authentication authentication) {
        return productionEntryService.deleteProductionEntry(id);
    }


    // journal Entry

    @GetMapping("/getJournalEntryCategoryProductList")
    public ResponseEntity<?> getProductionEntry(@RequestParam("journalId") Long journalId,
                                                @RequestParam("shiftId") Long shiftId,
                                                @RequestParam(value = "branch", required = false) Long branch,
                                                @RequestParam(value = "cate", required = false) String cate,
                                                @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (cate.equalsIgnoreCase("BOM")) {
                if (branch != null) {
                    return productionEntryService.listAllBom(journalId, shiftId, "bom", shiftDate, branch);
                } else {
                    return productionEntryService.listAllBom(journalId, shiftId, "bom", shiftDate, user.get().getBranch_id());
                }
            } else {
                if (branch != null) {
                    return productionEntryService.listAllBye(journalId, shiftId, "bye", shiftDate, branch);
                } else {
                    return productionEntryService.listAllBye(journalId, shiftId, "bye", shiftDate, user.get().getBranch_id());
                }
            }
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/getJournalEntryFilterList")
    public ResponseEntity<?> getJournalEntryFilterList(
            @RequestParam(value = "journalName", required = false) String journalName,
            @RequestParam(value = "journalId", required = false) Long journalId,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate, Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getJournalEntryFilterList(journalId, journalName, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getJournalEntryView")
    public ResponseEntity<?> getJournalEntryView(@RequestParam(value = "journalName", required = false) String journalName,
                                                 @RequestParam(value = "journalId", required = false) Long journalId,
                                                 @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                 @RequestParam(value = "batchNo", required = false) String batchNo,
                                                 @RequestParam(value = "trnNo", required = false) String trnNo,
                                                 @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getJournalEntryView(journalId, shiftDate, batchNo, trnNo, shiftId);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/getProductionEntrySlapDetails")
    public ResponseEntity<?> getProductionEntrySlapDetails(
            @RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getProductionEntrySlapDetails(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/checkProductionEntry")
    public ResponseEntity<?> checkProductionEntry(
            @RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.checkProductionEntry(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getSjtQty")
    public ResponseEntity<?> getSjtQty(@RequestParam("journalId") Long journalId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getSjtQty(journalId);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/getProductionEntryEachSlapWiseDetails")
    public ResponseEntity<?> getProductionEntryEachSlapWiseDetails(
            @RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getProductionEntrySlapWiseDetails(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PostMapping("/getProductionEntryMachineDetails")
    public ResponseEntity<?> getProductionEntryMachineDetails(
            @RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getProductionEntryMachineDetails(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getProductionBatchList")
    public ResponseEntity<?> getProductionBatchList(@RequestParam("journalId") Long journalId,
                                                    @RequestParam("branchId") Long branchId,
                                                    @RequestParam(value = "inwardDate", required = false) String inwardDate,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.getProductionBatchList(journalId, branchId, inwardDate, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @DeleteMapping("/deleteProductionEntrySlapDetails")
    public ResponseEntity<?> deleteProductionEntrySlapDetails(
            @RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.deleteProductionEntrySlapDetails(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @DeleteMapping("/revertJournalEntry")
    public ResponseEntity<?> revertJournalEntry(@RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.revertJournalEntry(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    @DeleteMapping("/deleteJournalEntry")
    public ResponseEntity<?> deleteJournalEntry(@RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.deleteJournalEntry(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }




    @PostMapping("/validateStockQty")
    public ResponseEntity<?> validateStockQty(@RequestBody ProductionEntryFormDto entryFormDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return productionEntryService.validateStockQty(entryFormDto, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

}







