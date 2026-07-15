package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.dto.*;
import com.aria.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aria.dao.UserRepository;
import com.aria.model.User;
import com.aria.service.ExpenseService;
import com.aria.service.ProposalService;
import com.aria.service.PurchaseOrderService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ExpenseController {

    @Autowired
    ExpenseService expenseService;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/listpurchaseexpense")
    public ResponseEntity<?> listpurchaseexpense(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.findAllPurchaseExpense(user.get());
    }


    @GetMapping("/listpurchaseAMCexpense")
    public ResponseEntity<?> listpurchaseAMCexpense(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.findAllPurchaseAMcExpense(user.get());
    }

    @GetMapping("/secondaryExpense")
    public ResponseEntity<?> secondaryExpense(@RequestParam(value = "rname",required = false) String rname,@RequestParam(value = "rtype",required = false) String rtype,@RequestParam(value = "primaryExpenseName",required = false) String primaryExpenseName, Authentication authentication,Pageable pageable) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return expenseService.getAllSecondaryExpense(user.get(),pageable,rname,rtype,primaryExpenseName);
    }

    @GetMapping("/secondaryExpense/{id}")
    public ResponseEntity<?> secondaryExpense(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return new ResponseEntity<ExpensesMappingResponse>(expenseService.getSecondaryExpense(id), HttpStatus.OK);
    }

    @DeleteMapping("/secondaryExpense/{id}")
    public ResponseEntity<?> deleteSecondaryExpense(@PathVariable Long id,Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        expenseService.deleteSecondaryExpense(id);
        return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
    }

    @PostMapping("/addOrEdit/secondaryExpense")
    public ResponseEntity<?> addOrEditSecondaryExpense(@RequestBody ExpenseDto expenseDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String message = "Secondary Expense updated successfully!!";
        if (  expenseDto.getId() == null || expenseDto.getId() == 0) {
            expenseService.saveSecondaryExpense(expenseDto, user.get());
            message = "Secondary Expense save successfully!!!";
        }
        return new ResponseEntity<Status>(new Status("200", message), HttpStatus.OK);
    }



    @PostMapping("/savePurchaseExpense")
    public ResponseEntity<?> saveAMCExpense(@RequestBody PurchaseExpenseDto purchaseExpenseDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.saveAMCExpense(purchaseExpenseDto, user.get());
    }

    @PutMapping("/updatePurchaseExpense")
    public ResponseEntity<?> updatePurchaseExpense(@RequestBody PurchaseExpenseDto purchaseExpenseDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.updateAMCExpense(purchaseExpenseDto, user.get());
    }


    @GetMapping("/getPurchaseExpense/{id}")
    public ResponseEntity<?> getAMCExpense(@PathVariable Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.getAMCExpense(id);
    }


    @DeleteMapping("/deletePurchaseExpense/{id}")
    public ResponseEntity<?> deleteAMCExpense(@PathVariable Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.deleteAMCExpense(id);
    }

    @GetMapping("/getPurchaseExpenseList")
    public ResponseEntity<?> getPurchaseExpenseList(@RequestParam(value = "amcCategoryId", required = false) Long amcCategoryId,
                                                     @RequestParam(value = "amcExpenseType", required = false) Long amcExpenseType,
                                                     @RequestParam(value = "amcExpenseUnit", required = false) Long amcExpenseUnit,
                                                     @RequestParam(value = "amcFlag") int amcFlag,
                                                     @RequestParam(value = "amcMachineRefId", required = false) Long amcMachineRefId,
                                                     Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return expenseService.getAMCExpenseListByPage(amcCategoryId, amcExpenseType, amcExpenseUnit, amcFlag, amcMachineRefId, pageable);

    }
//   created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir
    @PostMapping("/savePrimaryExpenses")
    public ResponseEntity<?> savePrimaryExpenses(@RequestBody PrimaryExpensesDto primaryExpenseDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.savePrimaryExpenses(primaryExpenseDto, user.get());
    }
    
    //   created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir     
    @GetMapping("/getAllPrimaryExpense")
    public ResponseEntity<?> getPrimaryExpense( Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return expenseService.getAllPrimaryExpense();
        } else {
            return new ResponseEntity<List<PrimaryExpensesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
       
    }
//    
    @PostMapping("/saveVoucherFormat")
    public ResponseEntity<?> saveVoucherFormat(@RequestBody  VoucherFormatDto voucherFormatDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return expenseService.saveVoucherFormat(voucherFormatDto, user.get());
    }
//    
    @GetMapping("/getAllVoucherFormat")
    public ResponseEntity<?> getAllVoucherFormat( Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return expenseService.getAllVoucherFormat();
        } else {
            return new ResponseEntity<List<VoucherFormatDto>>(new ArrayList<>(), HttpStatus.OK);
        }
       
    }
    @GetMapping("/getAllSecondaryExpense")
    public ResponseEntity<?> findAllSecondaryExpense(@RequestParam(value = "rname",required = false) String rname ,  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return expenseService.findAllSecondaryExpense(rname);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }       
    }    
    @PostMapping("/editExpenses")
    public ResponseEntity<?> editExpenses(@RequestBody  ExpensesMappingResponse expensesMappingResponse, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return expenseService.editExpenses(expensesMappingResponse, user.get());
    }
    @GetMapping("/findAllSecondaryExpense")
    public ResponseEntity<?> getAllSecondaryExpense(@RequestParam(value = "rname",required = false) String rname ,  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
        	 return expenseService.getAllSecondaryExpensesList();
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }       
        
    }
    
}
