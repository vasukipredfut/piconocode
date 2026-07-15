package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.StockLedgerService;
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
public class StockLedgerController {



    @Autowired
    StockLedgerService ledgerService;

    @Autowired
    UserRepository userRepository;



    @GetMapping("/pCateProductAndLots")
    public ResponseEntity<?> pCateProductAndLots(
            @RequestParam(value = "pCategory", required = false) String pCategory, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return ledgerService.pCateProductAndLots(pCategory);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/filterStockLedger")
    public ResponseEntity<?> filterStockLedger(
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "stockCategory", required = false) String stockCategory,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "lot", required = false) Double lot,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            Pageable pageable,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return ledgerService.filterStockLedger(companyId, branchId, deptId, group, stockCategory, productId, lot, fromDate, toDate, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/stockLedgerView")
    public ResponseEntity<?> stockLedgerView( @RequestParam(value = "id", required = false) Long id,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return ledgerService.stockLedgerView(id);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }
}
