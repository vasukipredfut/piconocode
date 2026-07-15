package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.SearchDto;
import com.aria.model.*;
import com.aria.utils.VerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class VerifyService {

    @Autowired
    VerifyUtil verifyUtil;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    BillForwardRepository billForwardRepository;

    @Autowired
    BillReceiveRepository billReceiveRepository;

    @Autowired
    BillExchangeRepository billExchangeRepository;


    // update Plant Manager

    public ResponseEntity<?> updatePlantVerify(SearchDto searchDto, User loggedUser) {

        if (searchDto.getVerifyType() != null) {

            if (searchDto.getVerifyType().equalsIgnoreCase("BillInward")) {
                Optional<BillInward> optional = billInwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillInward billInward = optional.get();
                   // billInward.setPlatStatus("verify");
                    billInwardRepository.save(billInward);
                }

            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillForward")) {
                Optional<BillForward> optional = billForwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillForward billForward = optional.get();
                    //billForward.setPlatStatus("verify");
                    billForwardRepository.save(billForward);
                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillReceive")) {
                Optional<BillReceive> optional = billReceiveRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillReceive billReceive = optional.get();
                    //billReceive.setPlatStatus("verify");
                    billReceiveRepository.save(billReceive);
                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillExchange")) {
                Optional<BillExchange> optional = billExchangeRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillExchange billReceive = optional.get();
             //       billReceive.setPlatStatus("verify");
                    billExchangeRepository.save(billReceive);
                }
            }


            return new ResponseEntity<Status>(new Status("200", "PlantManager verify status successfully!!!"), HttpStatus.OK);
        } else {

            return new ResponseEntity<Status>(new Status("400", "PlantManager verify not  successfully!!!"), HttpStatus.OK);
        }
    }


    // update DGM


    public ResponseEntity<?> updateDGMVerify(SearchDto searchDto, User loggedUser) {
        if (searchDto.getVerifyType() != null) {

            if (searchDto.getVerifyType().equalsIgnoreCase("BillInward")) {
                Optional<BillInward> optional = billInwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillInward billInward = optional.get();
                   // billInward.setDgmStatus("verify");
                    billInwardRepository.save(billInward);
                }

            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillForward")) {
                Optional<BillForward> optional = billForwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillForward billForward = optional.get();
                    //billForward.setDgmStatus("verify");
                    billForwardRepository.save(billForward);
                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillReceive")) {
                Optional<BillReceive> optional = billReceiveRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillReceive billReceive = optional.get();
                    //billReceive.setDgmStatus("verify");
                    billReceiveRepository.save(billReceive);
                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillExchange")) {
                Optional<BillExchange> optional = billExchangeRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillExchange billReceive = optional.get();
                //    billReceive.setDgmStatus("verify");
                    billExchangeRepository.save(billReceive);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "DGM verify status successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "DGM verify status not successfully!!!"), HttpStatus.OK);
        }
    }


    // update MD


    public ResponseEntity<?> updateMDVerify(SearchDto searchDto, User loggedUser) {
        if (searchDto.getVerifyType() != null) {
            if (searchDto.getVerifyType().equalsIgnoreCase("BillInward")) {
                Optional<BillInward> optional = billInwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillInward billInward = optional.get();
                    //billInward.setDgmStatus("verify");
                    BillInward result = billInwardRepository.save(billInward);
                    if (result.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                        verifyUtil.updateBillExchange(result);
                        verifyUtil.updateInwardInspectionEntry(result);
                    } else {
                        verifyUtil.updateBillForward(result);
                    }
                }

            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillForward")) {
                Optional<BillForward> optional = billForwardRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillForward billForward = optional.get();
                    //billForward.setDgmStatus("verify");
                    BillForward result = billForwardRepository.save(billForward);
                    verifyUtil.updateBillForwardToReceive(result);
                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillReceive")) {
                Optional<BillReceive> optional = billReceiveRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillReceive billReceive = optional.get();
                    //billReceive.setDgmStatus("verify");
                    BillReceive result = billReceiveRepository.save(billReceive);

                }
            } else if (searchDto.getVerifyType().equalsIgnoreCase("BillExchange")) {
                Optional<BillExchange> optional = billExchangeRepository.findById(searchDto.getId());
                if (optional.isPresent()) {
                    BillExchange billReceive = optional.get();
           //         billReceive.setDgmStatus("verify");
                    BillExchange result = billExchangeRepository.save(billReceive);
                    verifyUtil.updateBillExchangeToReceive(result);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "MD verify status successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "MD verify status not successfully!!!"), HttpStatus.OK);
        }
    }
}
