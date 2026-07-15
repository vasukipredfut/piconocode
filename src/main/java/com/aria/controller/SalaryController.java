package com.aria.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.config.ResponseMessage;
import com.aria.dto.SalaryStructureDto;
import com.aria.dto.SalaryTransactionDto;
import com.aria.model.Status;
import com.aria.utils.CSVHelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.dto.PageDto;
import com.aria.model.User;
import com.aria.service.*;
import com.aria.utils.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SalaryController {

    @Autowired
    SalaryService salaryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SalarySchedulerService salarySchedulerService;

    @Autowired
    SalaryStructureService salaryStructureService;

    @Autowired
    CSVHelperUtil csvHelperUtil;

    @Autowired
    CWagesSalaryService wagesSalaryService;

    @Autowired
    CwagesService cwagesService;


    @GetMapping("/getCWagesList")
    public ResponseEntity<?> getCWagesList(@RequestParam(value = "refId", required = true) Long refId,
                                           @RequestParam(value = "transactionType", required = true) TransactionType transactionType,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return cwagesService.getCWagesList(refId, transactionType);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }



    // Define Salary Structure


    @PostMapping("/saveSalaryStructure")
    public ResponseEntity<?> saveSalaryStructure(@RequestBody SalaryStructureDto salaryStructure,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salaryStructureService.saveSalaryStructure(salaryStructure, user.get());
    }


    @PostMapping("/uploadSalaryStructure")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {


        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        String message = "";

        if (csvHelperUtil.hasCSVFormat(file)) {

            try {

                message = csvHelperUtil.checkUploadSalaryStructure(file.getInputStream());
                if (message.equalsIgnoreCase("success")) {

                    salaryStructureService.uploadSalaryStructure(file, user.get());

                    message = "Uploaded the file successfully: " + file.getOriginalFilename();
                    return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
                } else if (message.equalsIgnoreCase("empty")) {
                    message = "Uploaded the file is Empty Please fill AND Import..... :" + file.getOriginalFilename();
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseMessage(message));
                } else {
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


    @PostMapping("/downloadSalaryStructure")
    public void downloadSalaryStructure(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                        @RequestParam(value = "type", required = false) String type,
                                        HttpServletResponse servletResponse,
                                        Authentication authentication) throws IOException {


        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"salarystucture.csv\"");
        salaryStructureService.downloadSalaryStructure(servletResponse.getWriter(), trnYear, type);

    }


    @GetMapping("/salaryStructureList")
    public ResponseEntity<?> salaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "designation", required = false) String designation,
                                                 @RequestParam(value = "salaryType", required = false) String salaryType,
                                                 @RequestParam(value = "applyTo", required = false) String applyTo,
                                                 @RequestParam(value = "pageFlag", required = true) boolean pageFlag,
                                                 Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salaryStructureService.findAll(trnYear, status, designation, salaryType, applyTo, pageFlag, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/rejectSalaryStructureList")
    public ResponseEntity<?> rejectSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "designationId", required = false) Long designationId,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salaryStructureService.rejectSalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/verifySalaryStructureList")
    public ResponseEntity<?> verifySalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "designationId", required = false) Long designationId,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salaryStructureService.verifySalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/inactiveSalaryStructureList")
    public ResponseEntity<?> inactiveSalaryStructureList(@RequestParam(value = "trnYear", required = false) Long trnYear,
                                                         @RequestParam(value = "status", required = false) String status,
                                                         @RequestParam(value = "designationId", required = false) Long designationId,
                                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salaryStructureService.inactiveSalaryStructureList(trnYear, status, designationId);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }




    @GetMapping("/wagesDesignationList")
    public ResponseEntity<?> wagesDesignationList( @RequestParam(value = "designation", required = false) String designation,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salaryStructureService.wagesDesignationList(designation);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }




    //  Prepare Monthly Staff Salary

    @PostMapping("/saveStaffSalary")
    public ResponseEntity<?> saveStaffSalary(@RequestBody List<SalaryTransactionDto> dtoList,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String category = dtoList.get(0).getCategory();
        if (category.equalsIgnoreCase("staff")) {
            return salarySchedulerService.saveStaffSalary( dtoList,user.get());
        } else if (category.equalsIgnoreCase("staffApprentice")) {
            return salarySchedulerService.saveStaffApprenticeSalary(dtoList, user.get());
        } else {
            return new ResponseEntity<Status>(new Status("400", "category not Found"), HttpStatus.OK);
        }
    }
    @GetMapping("/prepareStaffSalary")
    public ResponseEntity<?> prepareStaffSalary(@RequestParam("category") String category,
                                                @RequestParam(value = "year", required = false) String year,
                                                @RequestParam(value = "month", required = false) Long month,
                                                @RequestParam("company") Long company,
                                                @RequestParam("branch") Long branch,
                                                @RequestParam(value = "fmDate", required = false) String fmDate,
                                                @RequestParam(value = "toDate", required = false) String toDate,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (category.equalsIgnoreCase("staff")) {
            return salarySchedulerService.prepareStaffSalary(year, month, company, branch,fmDate , toDate, user.get());
        } else if (category.equalsIgnoreCase("staffApprentice")) {
            return salarySchedulerService.prepareStaffApprenticeSalary(year, month, company, branch,fmDate,toDate, user.get());
        } else {
            return new ResponseEntity<Status>(new Status("400", "category not Found"), HttpStatus.OK);
        }
    }

    //Prepare Wages Monthly Salary

    @PostMapping("/saveLabourSalary")
    public ResponseEntity<?> saveLabourSalary(@RequestBody List<SalaryTransactionDto> dtoList,
                                                 Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String category = dtoList.get(0).getCategory();
        if (category.equalsIgnoreCase("Wages")) {
            return salarySchedulerService.saveLabourSalary(dtoList, user.get());
        } else if (category.equalsIgnoreCase("WagesApprentice")) {
            return salarySchedulerService.saveLabourApprenticeSalary(dtoList, user.get());
        } else
            return new ResponseEntity<Status>(new Status("400", "category not Found"), HttpStatus.OK);

    }
    
    @GetMapping("/prepareLabourSalary")
    public ResponseEntity<?> prepareLabourSalary(@RequestParam("category") String category,
                                                 @RequestParam(value = "year", required = false) String year,
                                                 @RequestParam(value = "month", required = false) Long month,
                                                 @RequestParam("company") Long company,
                                                 @RequestParam("branch") Long branch,
                                                 @RequestParam("dept") Long dept,
                                                 @RequestParam(value = "fmDate", required = false) String fmDate,
                                                 @RequestParam(value = "toDate", required = false) String toDate,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (category.equalsIgnoreCase("Wages")) {
            return salarySchedulerService.prepareLabourSalary(year, month, company, branch, dept,fmDate,toDate, user.get());
        } else if (category.equalsIgnoreCase("WagesApprentice")) {
            return salarySchedulerService.prepareLabourApprenticeSalary(year, month, company, branch, dept,fmDate,toDate, user.get());
        } else
            return new ResponseEntity<Status>(new Status("400", "category not Found"), HttpStatus.OK);

    }


    //Prepare Building Works Wages Monthly Salary

    @PostMapping("/prepareBuildingMaintenanceLabourSalary")
    public ResponseEntity<?> prepareBuildingMaintenanceLabourSalary(@RequestParam("category") String category,
                                                                    @RequestParam("year") String year,
                                                                    @RequestParam("month") Long month,
                                                                    @RequestParam("company") Long company,
                                                                    @RequestParam("branch") Long branch,
                                                                    @RequestParam("dept") Long dept,
                                                                    @RequestParam("fromDate") String fromDate,
                                                                    @RequestParam("toDate") String toDate,
                                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.prepareBuildingMaintenanceLabourSalary(year, month, company, branch, dept, fromDate, toDate, user.get());

    }


    // View Monthly Prepare Salary List BY Branch wise AND CategoryWise

    @GetMapping("/prepareSalaryList")
    public ResponseEntity<?> prepareSalaryList(@RequestParam(value = "category", required = false) String category,
                                               @RequestParam(value = "year", required = false) String year,
                                               @RequestParam(value = "month", required = false) Long month,
                                               @RequestParam(value = "company", required = false) Long company,
                                               @RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "status", required = false) String status,
                                               Pageable pageable,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.salaryPreparationList(category, year, month, company, branch,status, pageable);

    }


    // View Monthly Prepare Salary

    @GetMapping("/viewPrepareSalaryDetailsList")
    public ResponseEntity<?> viewPrepareSalaryDetailsList(@RequestParam(value = "category", required = false) String category,
                                                          @RequestParam(value = "year", required = false) String year,
                                                          @RequestParam(value = "month", required = false) Long month,
                                                          @RequestParam(value = "company", required = false) Long company,
                                                          @RequestParam(value = "branch", required = false) Long branch,
                                                          @RequestParam(value = "trnNo", required = false) String trnNo,
                                                          Pageable pageable,
                                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.viewPrepareSalaryDetailsList(category, year, month, company, branch, trnNo, pageable);

    }


    // Reject Monthly Prepare Salary

    @DeleteMapping("/rejectPrepareSalaryList")
    public ResponseEntity<?> rejectPrepareSalaryList(@RequestParam("category") String category,
                                                     @RequestParam("year") String year,
                                                     @RequestParam("month") Long month,
                                                     @RequestParam("company") Long company,
                                                     @RequestParam("branch") Long branch,
                                                     @RequestParam(value = "trnNo", required = false) String trnNo,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.rejectPrepareSalaryList(category, year, month, company, branch, trnNo);

    }


    // Print Monthly Prepare Salary

    @GetMapping("/printPrepareSalaryDetailsList")
    public ResponseEntity<?> printPrepareSalaryDetailsList(@RequestParam(value = "category") String category,
                                                           @RequestParam(value = "year") String year,
                                                           @RequestParam(value = "month") Long month,
                                                           @RequestParam(value = "company", required = false) Long company,
                                                           @RequestParam(value = "branch", required = false) Long branch,
                                                           @RequestParam(value = "trnNo", required = false) String trnNo,
                                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.printPrepareSalaryDetailsList(category, year, month, company, branch, trnNo);

    }


    @GetMapping("/salaryList")
    public ResponseEntity<?> salaryList(@RequestParam(value = "fromDate", required = false) String fromDate,
                                        @RequestParam(value = "toDate", required = false) String toDate,
                                        @RequestParam(value = "company", required = false) Long company,
                                        @RequestParam(value = "branch", required = false) Long branch,
                                        @RequestParam(value = "pageFlag", required = false) String pageFlag,
                                        Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return salarySchedulerService.listAll(fromDate, toDate, company, branch, pageFlag, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/wagesSalaryList")
    public ResponseEntity<?> wagesSalaryList(@RequestParam(value = "fromDate", required = false) String fromDate,
                                             @RequestParam(value = "toDate", required = false) String toDate,
                                             @RequestParam(value = "company", required = false) Long company,
                                             @RequestParam(value = "branch", required = false) Long branch,
                                             @RequestParam(value = "dept", required = false) Long dept,
                                             @RequestParam(value = "pageFlag", required = false) String pageFlag,
                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return wagesSalaryService.wagesSalaryList(fromDate, toDate, company, branch, dept, pageFlag, pageable);
        } else {
            return new ResponseEntity<List<PageDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // Building Works Wages find lastDate

    @PostMapping("/buildingMaintenanceLabourSalaryLastDate")
    public ResponseEntity<?> buildingMaintenanceLabourSalaryLastDate(@RequestParam("company") Long company,
                                                                     @RequestParam("branch") Long branch,
                                                                     @RequestParam("dept") Long dept,
                                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.buildingMaintenanceLabourSalaryLastDate(company, branch, dept, user.get());

    }

    @PostMapping("/verifyApproveAuthorizePrepareSalaryList")
    public ResponseEntity<?> verifyApproveAuthorizePrepareSalaryList(@RequestParam("category") String category,
                                                     @RequestParam("year") String year,
                                                     @RequestParam("month") Long month,
                                                     @RequestParam("company") Long company,
                                                     @RequestParam("branch") Long branch,
                                                     @RequestParam(value = "trnNo", required = false) String trnNo, 
                                                     @RequestParam("status") String status,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return salarySchedulerService.verifyApproveAuthorizePrepareSalaryList(category, year, month, company, branch, trnNo,status,user.get());

    }
}
