package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.BillInwardDto;
import com.aria.dto.QtyMainDto;
import com.aria.dto.ReptNameDto;
import com.aria.dto.StockPaperReqDto;
import com.aria.model.User;
import com.aria.service.ReportService;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class ReportController {

    @Autowired
    ReportService reportService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @GetMapping("/unitReport")
    public ResponseEntity<?> unitReportDetails(@RequestParam(value = "type", required = false) String type,
                                               @RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "fromDate", required = false) String fromDate,
                                               @RequestParam(value = "toDate", required = false) String toDate,
                                               @RequestParam(value = "dept", required = false) Long dept,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return reportService.getUnitReport(user.get() , type, branch, fromDate, toDate, dept);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



    // Godown Stock Report


    @GetMapping("/stockPaperProductList")
    public ResponseEntity<?> stockPaperProductList(@RequestParam(value = "branchId", required = false) Long branchId,
                                                   @RequestParam(value = "deptId", required = false) Long deptId,
                                                   @RequestParam(value = "type", required = false) String type,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return reportService.stockPaperProductList(branchId, deptId, type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PostMapping("/stockPaperReport")
    public ResponseEntity<?> stockPaperReport(@RequestBody StockPaperReqDto stockPaperReqDto, Authentication authentication) {

        Long branchId = null;
        Long deptId = null;
        List<Long> productIds = null;
        Long shiftId = null;
        Date shiftDate= null;

        if (stockPaperReqDto.getBranchId() != null) {
            branchId = stockPaperReqDto.getBranchId();
        }
        if (stockPaperReqDto.getDeptId() != null) {
            deptId = stockPaperReqDto.getDeptId();
        }

        if (stockPaperReqDto.getShiftId() != null) {
            shiftId = stockPaperReqDto.getShiftId();
        }

        if (stockPaperReqDto.getShiftDate() != null) {
            shiftDate = dateUtil.getStringTODateWk(stockPaperReqDto.getShiftDate());
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return reportService.stockPaperReport(branchId, deptId, shiftId , shiftDate, stockPaperReqDto.getProductsWithLot());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }



    // Finished goods summary

    @GetMapping("/finishedGoodsSummary")
    public ResponseEntity<?> finishedGoodsSummary(@RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "dateValue", required = false) String dateValue,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get()!= null) {
            return reportService.getFinishedGoodsSummary(user.get(), branch, dateValue);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    // FG closed summary
    @GetMapping("/finishedGoodsClosedSummary")
    public ResponseEntity<?> finishedGoodsSummary(@RequestParam(value = "date", required = false) String date,@RequestParam(value = "month", required = false) Long month,
    		@RequestParam(value = "year", required = false) Long year,  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get()!= null) {
            return reportService.getFinishedGoodsClosedSummary(user.get(), date,month,year);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    // FG stock report
    @GetMapping("/getAllFGName")
    public ResponseEntity<?> getAllFGName( Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return reportService.getAllFGName();
        } else {
            return new ResponseEntity<List<ReptNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
       
    }
    
    @GetMapping("/finishedGoodsStockSummary")
    public ResponseEntity<?> finishedGoodsStockSummary(@RequestParam(value = "fromDate", required = false) String fromDate,@RequestParam(value = "toDate", required = false) String toDate,@RequestParam(value = "branch", required = false) Long branch,
    		@RequestParam(value = "fgId", required = false) Long fgId,	@RequestParam(value = "firmId", required = false) Long firmId,  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get()!= null) {
            return reportService.getFinishedGoodsStockSummary(user.get(),fromDate,toDate,branch,fgId,firmId);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    
    @GetMapping("/oilPackedGroup")
    public ResponseEntity<?> oilPackedDetails(
    		  Authentication authentication) {
    	  UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

          Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
          if (user.get() != null) {
          	 return reportService.getAllGroup();
          } else {
              return new ResponseEntity<List<ReptNameDto>>(new ArrayList<>(), HttpStatus.OK);
          }
         
    }
    
    @GetMapping("/oilPackedDetails")
    public ResponseEntity<?> oilPackedDetails(@RequestParam(value = "fromDate", required = false) String fromDate,@RequestParam(value = "toDate", required = false) String toDate,@RequestParam(value = "branch", required = false) Long branch,
    		@RequestParam(value = "sjid", required = false) Long sjid,  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        System.out.println("System Timezone : " + ZoneId.systemDefault());
        if (user.get()!= null) {
            return reportService.oilPackedDetails(user.get(),fromDate,toDate,branch,sjid);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
}
