package com.aria.controller;

import java.util.Optional;

import com.aria.dto.SupplierEvaluationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.SupplierEvaluationService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SupplierEvaluationController {

    @Autowired
    SupplierEvaluationService supplierEvaluationService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveSupplierEvaluation")
    public ResponseEntity<?> saveSupplierEvaluation(@RequestBody SupplierEvaluationDTO supplierEvaluation,  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.saveSupplierEvaluation(supplierEvaluation, user.get());
    }

    @GetMapping("/updateEvaluationStatus")
    public ResponseEntity<?> updateEvaluationStatus(@RequestParam(value = "status", required = true) String status,
                                                    @RequestParam(value = "trnNo", required = true) String trnNo,
                                                    @RequestParam(value = "type", required = true) String type,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.updateEvaluationStatus(status, trnNo, type, user.get() );
    }


    @GetMapping("/supplierEvaluationList")
    public ResponseEntity<?> supplierEvaluationList(@RequestParam(value = "vendorId", required = false) Long vendorId,
                                                    @RequestParam(value = "firmId", required = false) Long firmId,
                                                    @RequestParam(value = "vendorName", required = false) String vendorName,
                                                    @RequestParam(value = "fromyear", required = false) Integer fromyear,
                                                    @RequestParam(value = "toyear", required = false) Integer toyear,
                                                    @RequestParam(value = "timePeriod", required = false) String timePeriod,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    Pageable page, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.supplierEvaluationList(firmId, vendorId, vendorName, fromyear, toyear, timePeriod, status, page);
    }

    @GetMapping("/supplierEvaluationListByBranchWise")
    public ResponseEntity<?> supplierEvaluationListByBranchWise(@RequestParam(value = "vendorId", required = false) Long vendorId,
                                                                @RequestParam(value = "vendorName", required = false) String vendorName,
                                                                @RequestParam(value = "firmId", required = true) Long firmId,
                                                                @RequestParam(value = "fromyear", required = false) Integer fromyear,
                                                                @RequestParam(value = "toyear", required = false) Integer toyear,
                                                                @RequestParam(value = "timePeriod", required = false) String timePeriod,
                                                                @RequestParam(value = "trnNo", required = false) String trnNo,
                                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.supplierEvaluationListByBranchWise(vendorId, vendorName, firmId, fromyear, toyear, timePeriod, trnNo);
    }




    @GetMapping("/periodBillListByVendor")
    public ResponseEntity<?> periodBillListByVendor(@RequestParam(value = "firmId", required = true) Long firmId,
                                                    @RequestParam(value = "branch", required = true) Long branch,
                                                    @RequestParam(value = "vendorId", required = true) Long vendorId,
                                                    @RequestParam(value = "period", required = true) String period,
                                                    @RequestParam(value = "year", required = true) Integer year,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.periodBillListByVendor(firmId, branch, vendorId, period, year, user.get());

    }


    @GetMapping("/printSupplierEvaluationList")
    public ResponseEntity<?> printSupplierEvaluationList(@RequestParam(value = "firmId", required = true) Long firmId,
                                                         @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                         @RequestParam(value = "vendorName", required = false) String vendorName,
                                                         @RequestParam(value = "fromYear", required = false) Integer fromYear,
                                                         @RequestParam(value = "toYear", required = false) Integer toYear,
                                                         @RequestParam(value = "timePeriod", required = false) String timePeriod,
                                                         @RequestParam(value = "trnNo", required = true) String trnNo,
                                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.printSupplierEvaluationList(firmId, vendorId, vendorName, fromYear, toYear, timePeriod, trnNo);

    }


    @GetMapping("/listsupplierevaluation")
    public ResponseEntity<?> listsupplierevaluation(@RequestParam(value = "poId", required = false) Long poId, @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                    @RequestParam(value = "fromyear", required = false) Integer fromyear, @RequestParam(value = "toyear", required = false) Integer toyear, @RequestParam(value = "timePeriod", required = false) String timePeriod,
                                                    Pageable page, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.listAllSupplierEvaluation(vendorId, poId, fromyear, toyear, timePeriod, page);
    }

    @GetMapping("/listsupplierevaluationattributes")
    public ResponseEntity<?> listsupplierevaluationattributes(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.listAllSupplierEvaluationattributes();

    }


    @GetMapping("/vendorscore")
    public ResponseEntity<?> listvendorscore(@RequestParam(value = "vendorId", required = true) Long vendorId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return supplierEvaluationService.listvendorSCore(vendorId, user.get());

    }


}
