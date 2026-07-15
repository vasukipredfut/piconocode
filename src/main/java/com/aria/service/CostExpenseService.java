package com.aria.service;

import com.aria.dao.CostExpenseRepository;
import com.aria.dto.CostExpenseDto;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class CostExpenseService {


    @Autowired
    CostExpenseRepository costExpenseRepository;


    public ResponseEntity<?> saveCostExpense(CostExpenseDto costExpenseDto, User loggedUser) {
        CostExpense costExpense = new CostExpense(costExpenseDto);
        costExpense.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        costExpense.setCreated_by(loggedUser.getId());
        CostExpense result = costExpenseRepository.save(costExpense);
        return new ResponseEntity<Status>(new Status("200", "CostExpense created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCostExpense(CostExpenseDto costExpenseDto, User loggedUser) {
        CostExpense costExpense = new CostExpense(costExpenseDto);
        costExpense.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        CostExpense result = costExpenseRepository.save(costExpense);
        return new ResponseEntity<Status>(new Status("200", "CostExpense updated SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getCostExpense(String id) throws UsernameNotFoundException {
        Optional<CostExpense> optionalCostExpense = costExpenseRepository.findById(Long.parseLong(id));
        if (optionalCostExpense.isPresent()) {
            CostExpenseDto costExpenseDto = new CostExpenseDto(optionalCostExpense.get());
            return new ResponseEntity<CostExpenseDto>(costExpenseDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> deleteCostExpense(String id) throws UsernameNotFoundException {
        try {
            Optional<CostExpense> optionalCostExpense = costExpenseRepository.findById(Long.parseLong(id));
            if (optionalCostExpense.isPresent()) {
                CostExpense costExpense = optionalCostExpense.get();
                costExpenseRepository.delete(costExpense);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}
