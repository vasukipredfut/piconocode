package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.dto.DayEndStockClosingSummaryDto;
import com.aria.dto.PhysicalStockDto;
import com.aria.dto.ProductionEntryDto;
import com.aria.dto.StockPaperReqDto;
import com.aria.model.User;
import com.aria.service.DayEndClosingService;
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
public class DayEndClosingController {


    @Autowired
    UserRepository userRepository;

    @Autowired
    DayEndClosingService dayEndClosingService;


    /*

    @GetMapping("/physicalStockList")
    public ResponseEntity<?> physicalStockList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.physicalStockList(user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @PostMapping("/savePhysicalStock")
    public ResponseEntity<?> savePhysicalStock(@RequestBody List<PhysicalStockDto> physicalStockDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.savePhysicalStock(physicalStockDto, user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PutMapping("/updatePhysicalStock")
    public ResponseEntity<?> updatePhysicalStock(@RequestBody List<PhysicalStockDto> physicalStockDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.savePhysicalStock(physicalStockDto, user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/closingStockDetails")
    public ResponseEntity<?> closingStockDetails(@RequestParam(value = "branch") Long branch,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.closingStockDetails(branch);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }




    @GetMapping("/filterPhysicalStockList")
    public ResponseEntity<?> filterPhysicalStockList(@RequestParam(value = "branch", required = false) Long branch,
                                                     @RequestParam(value = "dept", required = false) Long dept,
                                                     @RequestParam(value = "lot", required = false) Double lot,
                                                     @RequestParam(value = "binName", required = false) Long binName,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.filterPhysicalStockList(branch, dept, lot, binName, user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    */


    @PostMapping("/updateDayEndClosingStock")
    public ResponseEntity<?> updateDayEndClosingStock(@RequestBody List<DayEndStockClosingSummaryDto> closingSummaryDtoList, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.updateDayEndClosingStock(closingSummaryDtoList, user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/yieldSummary")
    public ResponseEntity<?> yieldSummary(@RequestParam(value = "branch", required = false) Long branch,
                                          @RequestParam(value = "accountingDate", required = false) String accountingDate,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.yieldSummary(branch, accountingDate , user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/journalOrMrnNotClosing")
    public ResponseEntity<?> journalOrMrnNotClosing(@RequestParam(value = "branch", required = false) Long branch,
                                                    @RequestParam(value = "dept", required = false) Long dept,
                                                    @RequestParam(value = "accountingDate", required = false) String accountingDate,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.journalOrMrnNotClosing(branch, dept, user.get(), accountingDate);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/journalFilterDayClosing")
    public ResponseEntity<?> journalFilterDayClosing(@RequestParam(value = "journalName", required = false) String journalName,
                                                     @RequestParam(value = "journalId", required = false) Long journalId,
                                                     @RequestParam(value = "branch", required = false) Long branch,
                                                     @RequestParam(value = "dept", required = false) Long dept,
                                                     @RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "fromDate", required = false) String fromDate,
                                                     @RequestParam(value = "toDate", required = false) String toDate,
                                                     Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {

            return dayEndClosingService.journalFilterDayClosing(branch, dept, journalId, journalName, status, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/productionLedger")
    public ResponseEntity<?> productionLedger(@RequestParam(value = "companyId", required = false) Long companyId,
                                              @RequestParam(value = "branchId", required = false) Long branchId,
                                              @RequestParam(value = "deptId", required = false) Long deptId,
                                              @RequestParam(value = "productId", required = false) Long productId,
                                              @RequestParam(value = "lot", required = false) Double lot,
                                              @RequestParam(value = "fromDate", required = false) String fromDate,
                                              @RequestParam(value = "toDate", required = false) String toDate,
                                              Pageable pageable,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.productionLedger(companyId, branchId, deptId, productId, lot, fromDate, toDate, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/productionLedgerView")
    public ResponseEntity<?> productionLedgerView(@RequestParam(value = "trnNo", required = false) String trnNo,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.productionLedgerView(trnNo);
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/rawMaterialMovementSummary")
    public ResponseEntity<?> rawMaterialMovementSummary(@RequestParam(value = "trnDate", required = false) String trnDate,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.rawMaterialMovementSummary(trnDate, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/accountClosingValidation")
    public ResponseEntity<?> accountClosingValidation(@RequestParam(value = "accountDate", required = false) String accountDate,
                                                        @RequestParam(value = "branch", required = false) Long branch,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.accountClosingValidation(accountDate, branch, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/rawMaterialMovementQtyWiseSummary")
    public ResponseEntity<?> rawMaterialMovementQtyWiseSummary(@RequestParam(value = "accountingDate", required = false) String accountingDate,
                                                               @RequestParam(value = "type", required = false) String type,
                                                               @RequestParam(value = "productId", required = false) Long productId,
                                                               @RequestParam(value = "lot", required = false) Double lot,
                                                               @RequestParam(value = "branch", required = false) Long branch,
                                                               @RequestParam(value = "reqBranch", required = false) Long reqBranch,
                                                               @RequestParam(value = "issueBranch", required = false) Long issueBranch,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.rawMaterialMovementQtyWiseSummary(type, accountingDate, productId , branch, reqBranch, issueBranch,  lot, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/checkDateServer")
    public ResponseEntity<?> checkDateServer(Authentication authentication) {
        return dayEndClosingService.checkDateServer();
    }


    // dummy api local check
    @GetMapping("/updateSummary")
    public ResponseEntity<?> updateSummary(@RequestParam(value = "branch", required = false) Long branch,
                                          @RequestParam(value = "accountingDate", required = false) String accountingDate,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.updateSummary(branch, accountingDate , user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }
    @GetMapping("/dayEndClosingValidation")
    public ResponseEntity<?> dayEndClosingValidation(
                                          @RequestParam(value = "accountingDate", required = false) String accountingDate,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return dayEndClosingService.dayEndClosingValidation(accountingDate , user.get());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

}
