package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.SplitDto;
import com.aria.model.User;
import com.aria.service.BinDetailsServices;
import com.aria.service.SplitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SplitController {

    @Autowired
    SplitService service;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BinDetailsServices binDetailsServices;


    @GetMapping("/splitFinishedGooseBinsList")
    public ResponseEntity<?> finishedGooseBinsList(@RequestParam(value = "binId", required = false) Long binId,
                                                   @RequestParam(value = "branchId", required = false) Long branchId,
                                                   @RequestParam(value = "productId", required = false) Long productId,
                                                   @RequestParam(value = "type", required = false) String type,
                                                   @RequestParam(value = "name", required = false) String name, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.finishedGooseBinsList(binId, branchId, productId, type, name);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getSplitBinLotWiseStock")
    public ResponseEntity<?> getSplitBinLotWiseStock(@RequestParam(value = "binNo", required = false) Long binNo,
                                                     @RequestParam(value = "productId", required = false) Long productId,
                                                     @RequestParam(value = "lot", required = false) Double lot,
                                                     @RequestParam(value = "palletNo", required = false) String palletNo,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getSplitBinLotWiseStock(productId, binNo, lot, palletNo);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/getSplitJournalBatchDetails")
    public ResponseEntity<?> getSplitJournalBatch(
            @RequestParam(value = "batchNo", required = false) String batchNo,
            @RequestParam(value = "binNo", required = false) Long binNo,
            @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return service.getSplitJournalBatch(batchNo, binNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getSplitJournalBatchProductList")
    public ResponseEntity<?> getSplitJournalBatchProductList(@RequestParam(value = "binName", required = false) Long binName,
                                                             @RequestParam(value = "batchNo", required = false) String batchNo,
                                                             @RequestParam(value = "splitConfigbased") String splitConfigbased,
                                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return service.getSplitJournalBatchProductList(binName, batchNo,splitConfigbased);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PostMapping("/saveSplit")
    public ResponseEntity<?> saveSplit(@RequestBody SplitDto splitDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return service.saveSplit(splitDto, user.get());
    }


    @GetMapping("/splitList")
    public ResponseEntity<?> splitList(@RequestParam(value = "productName", required = false) String productName,
                                       @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                       @RequestParam(value = "dateTo", required = false) String dateTo,
                                       @RequestParam(value = "branch", required = false) Long branch,
                                       @RequestParam(value = "dept", required = false) Long dept,
                                       Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return service.splitList(productName, dateFrom, dateTo, branch, dept , pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/splitView")
    public ResponseEntity<?> splitView(@RequestParam(value = "id", required = false) Long id,
                                       @RequestParam(value = "trnNo", required = false) String trnNo,
                                       @RequestParam(value = "branch", required = false) Long branch,
                                       @RequestParam(value = "dept", required = false) Long dept,
                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return service.getSplitDetails(id, trnNo, branch, dept);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @PostMapping("/approveSplit")
    public ResponseEntity<?> approveSplit(@RequestParam(value = "id", required = false) Long id,
                                          @RequestParam(value = "trnNo", required = false) String trnNo,
                                          @RequestParam(value = "type", required = false) String type,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return service.approveSplit(id, trnNo,type, user.get());
    }


    @GetMapping("/getSplit")
    public ResponseEntity<?> getSplit(@RequestParam(value = "id", required = false) Long id,
                                      @RequestParam(value = "trnNo", required = false) String trnNo,
                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return service.getSplitDetails(id, trnNo);
    }

    @PostMapping("/updateSplit")
    public ResponseEntity<?> updateSplit(@RequestBody SplitDto splitDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return service.updateSplit(splitDto, user.get());
    }

}
