package com.aria.service;

import com.aria.dao.BankDetailsRepository;
import com.aria.dao.BankRepository;
import com.aria.dto.BankDTO;
import com.aria.dto.BankDetailsDTO;
import com.aria.dto.PageDataDto;
import com.aria.model.Bank;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.BankDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BankDetailsService {

    @Autowired
    BankDetailsRepository bankDetailsRepository;

    @Autowired
    BankRepository bankRepository;


    public ResponseEntity<?> saveBank(BankDTO bankDTO, User loggedUser) {
        Bank bank = new Bank(bankDTO);
        bankRepository.save(bank);
        return new ResponseEntity<Status>(new Status("200", "Bank Account save successfully!!!"), HttpStatus.OK);
    }
    
    public ResponseEntity<?> saveBankDetails(BankDetailsDTO bankDetailsDTO, User loggedUser) {
        BankDetails bankDetails = new BankDetails(bankDetailsDTO);
        bankDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        bankDetailsRepository.save(bankDetails);
        return new ResponseEntity<Status>(new Status("200", "Bank Account save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBankDetails(BankDetailsDTO bankDetailsDTO, User loggedUser) {
        BankDetails bankDetails = new BankDetails(bankDetailsDTO);
        bankDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        bankDetailsRepository.save(bankDetails);
        return new ResponseEntity<Status>(new Status("200", "Bank Account updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAll(String bankName, String branchName, String ifscCode, Pageable pageable) throws UsernameNotFoundException {
        Page<BankDetails> list = bankDetailsRepository.findBy(bankName, branchName, ifscCode, pageable);
        if (!list.isEmpty()) {
            List<BankDetailsDTO> result = list.toList().stream().map(data -> {
                BankDetailsDTO bankDetailsDTO = new BankDetailsDTO(data);
                return bankDetailsDTO;
            }).collect(Collectors.toList());
            PageDataDto<BankDetailsDTO> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<BankDetailsDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BankDetailsDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> bankDetails(Long id) throws UsernameNotFoundException {
        Optional<BankDetails> bankDetailsOptional = bankDetailsRepository.findById(id);
        if (bankDetailsOptional.isPresent()) {
            BankDetailsDTO result = new BankDetailsDTO(bankDetailsOptional.get());
            return new ResponseEntity<BankDetailsDTO>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<BankDetailsDTO>(HttpStatus.NOT_FOUND);
        }
    }
    
    
    public ResponseEntity<?> deleteBankDetails(Long id) throws UsernameNotFoundException {
        Optional<BankDetails> bankDetailsOptional = bankDetailsRepository.findById(id);
        if (bankDetailsOptional.isPresent()) {
            BankDetails bankDetails = bankDetailsOptional.get();
            bankDetailsRepository.delete(bankDetails);
            return new ResponseEntity<Status>(new Status("200", "deleted successfully!!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("200", "Couldn't Delete the Record!!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> bankList(String bankName, String branchName, String ifscCode) throws UsernameNotFoundException {
        List<BankDetails> list = bankDetailsRepository.findBy(bankName, branchName, ifscCode);
        if (!list.isEmpty()) {
            List<BankDetailsDTO> result = list.stream().map(data -> {
                BankDetailsDTO bankDetailsDTO = new BankDetailsDTO(data);
                return bankDetailsDTO;
            }).collect(Collectors.toList());
            return new ResponseEntity< List<BankDetailsDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BankDetailsDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
