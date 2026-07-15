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
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.dto.CWagesSalaryReqDTO;
import com.aria.dto.DWagesSalarySummaryDTO;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.CWagesSalaryService;
import com.aria.utils.TransactionType;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CWagesSalaryController {

    @Autowired
    CWagesSalaryService cWagesSalaryService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/getTransactionListfilterByType")
    public ResponseEntity<?> getTransactionListFilterByType(
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "shiftDate", required = false) String shiftDate,
            @RequestParam(value = "transactionType", required = true) TransactionType transactionType,
            @RequestParam(value = "branch", required = false) Long branch,
            @RequestParam(value = "dept", required = false) Long dept,
            @RequestParam(value = "trnDate", required = false) String trnDate,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (user.get() != null) {

            if (branch == null) {
                branch = user.get().getBranch_id();
            }
            if (dept == null) {
                dept = user.get().getDepartment().getId();
            }
            return cWagesSalaryService.getTransactionListByType(trnDate, transactionType, branch, dept);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }

    }

    @GetMapping("/getJournalDetails")
    public ResponseEntity<?> getJournalDetail(@RequestParam(value = "trnNo", required = true) String trnNo,
                                              Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.getJournalDetail(trnNo);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }


    @GetMapping("/getDWagesEmployeeList")
    public ResponseEntity<?> getDWagesEmpList(@RequestParam(value = "shiftId", required = true) String shiftId,
                                              @RequestParam(value = "shiftType", required = true) String shiftType,
                                              @RequestParam(value = "shiftDate", required = true) String shiftDate, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.getDWagesEmpList(user.get(), shiftType, Long.valueOf(shiftId), shiftDate);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PutMapping("/changeSalaryStatus")
    public ResponseEntity<?> changeSalStatus(@RequestParam(value = "trnNo", required = true) String trnNo,
                                             @RequestParam(value = "transactionType", required = true) TransactionType transactionType,
                                             @RequestParam(value = "productId", required = false) Long productId,
                                             @RequestParam(value = "type", required = false) String type,
                                             Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.changeSalStatus(trnNo, transactionType, productId, type);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }


    @PostMapping("/saveCWagesSalary")
    public ResponseEntity<?> saveCWagesSalary(@RequestBody CWagesSalaryReqDTO cWagesSalaryReqDTO,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return cWagesSalaryService.saveCWagesSalary(cWagesSalaryReqDTO, user.get());
    }

    @GetMapping("/cWagesSalaryList")
    public ResponseEntity<?> cWagesSalaryList(@RequestParam(value = "type", required = false) String type,
                                              @RequestParam(value = "branchId", required = false) Long branchId,
                                              @RequestParam(value = "dept", required = false) Long dept,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "shiftName", required = false) String shiftName,
                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                              @RequestParam(value = "trnNo", required = false) String trnNo,
                                              @RequestParam(value = "trnType", required = false) String trnType,
                                              @RequestParam(value = "trnName", required = false) String trnName,
                                              Authentication authentication,
                                              Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (user.get() != null) {
            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            System.out.println(branchId);
            return cWagesSalaryService.cWagesSalaryList(user.get(), branchId, dept, shiftName, shiftId, shiftDate,
                    pageable, trnType, trnName, trnNo);
        } else {
            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/rejectCWagesSalaryAllocation")
    public ResponseEntity<?> rejectCWagesSalaryAllocation(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "dept", required = false) Long dept,
            @RequestParam(value = "trnNo", required = false) String trnNo,
            @RequestParam(value = "refTrnNo", required = false) String refTrnNo,
            @RequestParam(value = "transactionType", required = false) TransactionType transactionType,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (user.get() != null) {
            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            return cWagesSalaryService.rejectCWagesSalaryAllocation(user.get(), branchId, dept, trnNo, refTrnNo,
                    transactionType);
        } else {
            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getWagesSummary")
    public ResponseEntity<?> getWagesSummary(@RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "branchId", required = false) Long branchId,
                                             @RequestParam(value = "dept", required = false) Long dept,
                                             @RequestParam(value = "shiftId", required = false) Long shiftId,
                                             @RequestParam(value = "shiftName", required = false) String shiftName,
                                             @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                             @RequestParam(value = "shiftType", required = false) String shiftType, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (branchId == null) {
                branchId = user.get().getBranch_id();
            }
            return cWagesSalaryService.getWagesSummary(user.get(), branchId, dept, shiftName, shiftId, shiftDate, shiftType);
        } else {
            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Wages Salary Verify List

    @PostMapping("/verifyWagesSummary")
    public ResponseEntity<?> verifyWagesSummary(@RequestBody List<DWagesSalarySummaryDTO> dWagesSalarySummaryDTOList,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return cWagesSalaryService.verifyWagesSummary(user.get(), dWagesSalarySummaryDTOList);

    }

    @GetMapping("/checkVerifyWagesSummary")
    public ResponseEntity<?> checkVerifyWagesSummary(@RequestParam(value = "category", required = true) String category,
                                                     @RequestParam(value = "branchId", required = false) Long branchId,
                                                     @RequestParam(value = "dept", required = false) Long dept,
                                                     @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                     @RequestParam(value = "pShiftId", required = false) Long pShiftId,
                                                     @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                     @RequestParam(value = "shiftType", required = false) String shiftType, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return cWagesSalaryService.checkVerifyWagesSummary(user.get(), category, branchId, dept, shiftId, pShiftId,
                shiftDate);

    }

    // Wages Salary Transaction List
    @GetMapping("/wagesSalaryTransactionList")
    public ResponseEntity<?> wagesSalaryTransactionList(@RequestParam(value = "company", required = false) Long company,
                                                        @RequestParam(value = "branch", required = false) Long branch,
                                                        @RequestParam(value = "dept", required = false) Long dept,
                                                        @RequestParam(value = "fromDate", required = false) String fromDate,
                                                        @RequestParam(value = "toDate", required = false) String toDate,
                                                        Pageable pageable,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.wagesSalaryTransactionList(company, branch, dept, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Wages Salary Transaction Print List

    @GetMapping("/wagesSalaryTransactionDetailsList")
    public ResponseEntity<?> wagesSalaryTransactionDetailsList(@RequestParam(value = "company", required = false) Long company,
                                                               @RequestParam(value = "branch", required = false) Long branch,
                                                               @RequestParam(value = "dept", required = false) Long dept,
                                                               @RequestParam(value = "fromDate", required = false) String fromDate,
                                                               @RequestParam(value = "toDate", required = false) String toDate, Pageable pageable,
                                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.wagesSalaryTransactionPrintList(company, branch, dept, fromDate, toDate, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Wages Salary Transaction Print List

    @GetMapping("/printWagesSalaryTransactionDetailsList")
    public ResponseEntity<?> printWagesSalaryTransactionDetailsList(
            @RequestParam(value = "trnNo", required = false) String trnNo,
            @RequestParam(value = "branch", required = false) Long branch,
            @RequestParam(value = "dept", required = false) Long dept,
            @RequestParam(value = "shiftDate", required = false) String shiftDate, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.printWagesSalaryTransactionDetailsList(trnNo, branch, dept, shiftDate);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/rejectWagesSummary")
    public ResponseEntity<?> rejectWagesSummary(@RequestParam(value = "company", required = false) Long company,
                                                @RequestParam(value = "branch", required = false) Long branch,
                                                @RequestParam(value = "dept", required = false) Long dept,
                                                @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                @RequestParam(value = "pShiftId", required = false) Long pShiftId,
                                                @RequestParam(value = "shiftType", required = false) String shiftType,
                                                @RequestParam(value = "category", required = true) String category, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return cWagesSalaryService.rejectWagesSummary(company, branch, dept, shiftDate, shiftId, pShiftId, shiftType, category);

    }


    @GetMapping("/getTrnProductDetails")
    public ResponseEntity<?> getTrnProductDetails(@RequestParam(value = "trnNo", required = true) String trnNo,
                                                  @RequestParam(value = "transactionType", required = true) TransactionType transactionType,
                                                  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cWagesSalaryService.getTrnProductDetails(trnNo, transactionType);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
