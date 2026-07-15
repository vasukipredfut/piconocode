package com.aria.controller;


import com.aria.dao.UserRepository;
import com.aria.dto.AttributesAssignDto;
import com.aria.dto.InspectionEntryRequest;
import com.aria.dto.QcConsumptionSetupDto;
import com.aria.dto.QcInProcessDto;
import com.aria.dto.QcInProcessEntryRequest;
import com.aria.dto.partcategoryListDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.QcInProcessService;
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
public class QcInProcessController {


    @Autowired
    QcInProcessService qcInProcessService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/qcInProcess/batchList")
    public ResponseEntity<?> qcInProcessBatchList(@RequestParam(value = "batchNo", required = false) String batchNo,
                                                  @RequestParam(value = "productId", required = false) Long productId,
                                                  @RequestParam(value = "branch", required = false) Long branch,
                                                  @RequestParam(value = "dept", required = false) Long dept,
                                                  @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                                  @RequestParam(value = "shiftId", required = false) Long shiftId,
                                                  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if(user.isPresent()) {
            if(branch == null){
                branch = user.get().getBranch_id();
            }
            if(dept == null){
                dept = user.get().getDepartment().getId();
            }
            return qcInProcessService.inProcessBatchList(user.get(), batchNo, productId, dept, branch, shiftDate, shiftId);
        }else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/saveQcInProcess")
    public ResponseEntity<?> saveQcInProcessData(@RequestBody List<QcInProcessDto> reqDto,
                                                 Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return qcInProcessService.saveQcInProcessData(reqDto, user.get());
    }


    @GetMapping("/attributes/qcInProcess/{kpiNameId}")
    public ResponseEntity<?> getInProcessData(@PathVariable(value = "kpiNameId", required = false) Long kpiNameId,
                                              @RequestParam(value = "batchNo", required = false) String batchNo,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                              Authentication authentication) {
        return qcInProcessService.getAttributesQcInProcessData(kpiNameId, batchNo, shiftId , shiftDate);
    }


    @GetMapping("/attributes/qcInProcessList")
    public ResponseEntity<?> getAllDetailsInProcess(@RequestParam(value = "batchNo", required = false) String batchNo,
                                                    @RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
                                                    @RequestParam(value = "inwardDate", required = false) String inwardDate,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return qcInProcessService.getAllInProcessDetails(batchNo, kpiNameId, inwardDate, status, pageable);
        } else {
            return new ResponseEntity<List<QcInProcessDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/attributes/qcInProcessDetails")
    public ResponseEntity<?> getInProcessData(@RequestParam(value = "batchNo", required = false) String batchNo,
                                              @RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                              Authentication authentication) {
        return qcInProcessService.getAttributesQcInProcessDetailsData(kpiNameId, batchNo, shiftId, shiftDate );
    }


    @GetMapping("/attributes/completeQcInProcess")
    public ResponseEntity<?> completeQcInProcess(@RequestParam(value = "batchNo", required = false) String batchNo,
                                              @RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                              Authentication authentication) {
        return qcInProcessService.completeQcInProcess(kpiNameId, batchNo, shiftId, shiftDate );
    }


    @PostMapping("/rot/qcInProcessReport")
    public ResponseEntity<?> qcInProcessReport(@RequestBody List<QcInProcessEntryRequest> requestList,
                                                      Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return qcInProcessService.qcInProcessReport(requestList);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    @GetMapping("/attributes/getBinOrTankNumber")
    public ResponseEntity<?> getBinOrTankNumber(@RequestParam(value = "productId", required = false) Long productId, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.getBinOrTankNumber(productId , user.get() );
    }
    @GetMapping("/attributes/getBatchNoByBinNo")
    public ResponseEntity<?> getBatchNoByBinNo(@RequestParam(value = "productId", required = false) Long productId,@RequestParam(value = "binNo", required = false) Long binNo, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.getBatchNoByBinNo(productId ,binNo , user.get() );
    }
    @GetMapping("/attributes/getPackingBatchNoByBinNo")
    public List<partcategoryListDto> getPackingBatchNoByBinNo(@RequestParam(value = "batchNo", required = false) String batchNo, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.getPackingBatchNoByBinNo(batchNo);
    }
    @PostMapping("/attributes/saveConsumpton")
    public ResponseEntity<?> saveConsumpton(@RequestBody QcConsumptionSetupDto qcConsumptionSetupDto,
                                                      Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());      
            return qcInProcessService.saveConsumpton(qcConsumptionSetupDto , user.get());       
    }
    @GetMapping("/attributes/getAvailbleQuantity")
    public ResponseEntity<?> getAvailbleQuantity(@RequestParam(value = "batchNo", required = false) String batchNo,@RequestParam(value = "binNo", required = false) Long binNo,@RequestParam(value = "productId", required = false) Long productId,@RequestParam(value = "quantity", required = false) Double quantity, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.getAvailbleQuantity(batchNo,binNo,productId,quantity);
    }
    @PostMapping("/attributes/stockConsumedByQuantity")
    public ResponseEntity<?> stockConsumedByQuantity(@RequestBody QcInProcessDto dto, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.stockConsumedByQuantity(dto, user.get());
    }
    @PostMapping("/attributes/stockConsumedByConsumableQuantity")
    public ResponseEntity<?> stockConsumedByConsumableQuantity(@RequestBody QcInProcessDto dto, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
         try {
    	return qcInProcessService.stockConsumedByConsumableQuantity(dto, user.get());
         }catch (RuntimeException e) {
        	 return ResponseEntity.badRequest().body(new Status("400", e.getMessage()));
         }
    }
    @GetMapping("/attributes/getConsumable")
    public ResponseEntity<?> getConsumable(@RequestParam(value = "kpiId", required = false) Long kpiId,@RequestParam(value = "productId", required = false) Long productId, Authentication authentication) {
    	 UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
         Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
    	return qcInProcessService.getConsumable(kpiId,productId);
    }
    @PutMapping("/attributes/updateConsumable")
    public ResponseEntity<?> updateConsumable(@RequestBody QcConsumptionSetupDto qcConsumptionSetupDto, Authentication authentication) {
          UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
          Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());      
          return qcInProcessService.updateConsumable(qcConsumptionSetupDto , user.get());       
}
    @DeleteMapping("/attributes/deleteConsumable")
    public ResponseEntity<?> deleteConsumable(@RequestParam (value = "id", required = false) List<Long> id, Authentication authentication) {
          UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
          Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());      
          return qcInProcessService.deleteConsumable(id , user.get());       
}
    @GetMapping("/attributes/getAllAttributeQc")
    public ResponseEntity<?> getAllAttributeQc(@RequestParam(value = "batchNo", required = false) String batchNo,
                                              @RequestParam(value = "kpiNameId", required = false) Long kpiNameId,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                              Authentication authentication) {
        return qcInProcessService.getAllAttributeQc(kpiNameId, batchNo, shiftId, shiftDate );
    }
   

}
