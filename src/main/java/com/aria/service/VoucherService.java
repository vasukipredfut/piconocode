package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherService {

    @Autowired
    EpfVoucherRepo epfVoucherRepo;

    @Autowired
    EsiVoucherRepo esiVoucherRepo;

    @Autowired
    SalaryVoucherRepo salaryVoucherRepo;

    @Autowired
    LeaveSalVoucherRepo leaveSalVoucherRepo;

    @Autowired
    DateUtil dateUtil;


    @Autowired
    SalaryTransactionRepo salaryTransactionRepo;


    public ResponseEntity<?> saveSalaryVoucher(SalaryVoucherDto voucherDto, User loggedUser) {
        SalaryVoucher salary = new SalaryVoucher(voucherDto);
        salary.setEntry(dateUtil.getISTDate());
        salary.setIstaff(loggedUser.getId());
        SalaryVoucher result = salaryVoucherRepo.save(salary);
        return new ResponseEntity<Status>(new Status("200", "SalaryVoucher save successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> saveLeaveSalaryVoucher(LeaveSalaryVoucherDto voucherDto, User loggedUser) {
        LeaveSalVoucher salary = new LeaveSalVoucher(voucherDto);
        salary.setTrndate(dateUtil.getISTDate());
        salary.setCby(loggedUser.getId());
        LeaveSalVoucher result = leaveSalVoucherRepo.save(salary);
        return new ResponseEntity<Status>(new Status("200", "LeaveSalVoucher save successfully!!!"), HttpStatus.OK);
    }


    // EPF Voucher

    public ResponseEntity<?> epfSalaryList(Long firmId, String year, Long month, int empType, String epfType) throws UsernameNotFoundException {
        String connectTable = null;
        if (empType == 0) {
            connectTable = "emaster";
        } else {
            connectTable = "emastdaily";
        }
        List<EpfSalaryList> list = new ArrayList<>();
        if (epfType != null && epfType.equalsIgnoreCase("all")) {
            list = salaryTransactionRepo.epfSalaryList(firmId, year, month, connectTable);
        } else {
            list = salaryTransactionRepo.epfExemptSalaryList(firmId, year, month, connectTable);
        }
        if (!list.isEmpty()) {
            List<EpfSalaryListDTO> result = list.stream().map(data -> new EpfSalaryListDTO(data)).collect(Collectors.toList());
            return new ResponseEntity<List<EpfSalaryListDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<EpfSalaryListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> saveEpfVoucher(EpfVoucherDto voucherDto, User loggedUser) {
        EpfVoucher salary = new EpfVoucher(voucherDto);
        salary.setUpon(dateUtil.getISTDate());
        salary.setUpby(loggedUser.getId());
        EpfVoucher result = epfVoucherRepo.save(salary);
        return new ResponseEntity<Status>(new Status("200", "EpfVoucher save successfully!!!"), HttpStatus.OK);
    }

    // ESI Voucher

    public ResponseEntity<?> esiSalaryList(Long firmId, String year, Long month, int empType, String epfType) throws UsernameNotFoundException {
        String connectTable = null;
        if (empType == 0) {
            connectTable = "emaster";
        } else {
            connectTable = "emastdaily";
        }
        List<EsiSalaryList> list = new ArrayList<>();
        if (epfType != null && epfType.equalsIgnoreCase("all")) {
            list = salaryTransactionRepo.esiSalaryList(firmId, year, month, connectTable);
        } else {
            list = salaryTransactionRepo.esiExemptSalaryList(firmId, year, month, connectTable);
        }
        if (!list.isEmpty()) {
            List<EsiSalaryListDTO> result = list.stream().map(data -> new EsiSalaryListDTO(data)).collect(Collectors.toList());
            return new ResponseEntity<List<EsiSalaryListDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<EpfSalaryListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveEsiVoucher(EsiVoucherDto voucherDto, User loggedUser) {
        EsiVoucher esiVoucher = new EsiVoucher(voucherDto);
        esiVoucher.setUpon(dateUtil.getISTDate());
        esiVoucher.setUpby(loggedUser.getId());
        EsiVoucher result = esiVoucherRepo.save(esiVoucher);
        return new ResponseEntity<Status>(new Status("200", "Esi Voucher save successfully!!!"), HttpStatus.OK);
    }
}
