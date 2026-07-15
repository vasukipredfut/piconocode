package com.aria.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.aria.dao.*;
import com.aria.model.*;

@Component
public class VerifyUtil {

    @Autowired
    BillForwardRepository billForwardRepository;

    @Autowired
    BillReceiveRepository billReceiveRepository;

    @Autowired
    BillExchangeRepository billExchangeRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    DateUtil dateUtil;



    public void updateBillForward(BillInward billInward) {

        BillForward billForward = new BillForward();
        billForward.setBillInwardDate(billInward.getReceivedDate());
        billForward.setStatus("Pending");
        billForward.setCreatedBy(billInward.getCreatedBy());
        billForward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billForward.setBillDate(billInward.getBillDate());
        billForward.setFromBranch(billInward.getDeliverBranch());
        billForward.setBillNumber(billInward.getBillNo());
        billForward.setCreatedBy(billInward.getUpdatedBy());
        billForwardRepository.save(billForward);
    }

    public Map<String, Object> getForwardValue(BillInward billInward) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("BillNo", billInward.getBillNo());
        stringObjectMap.put("BillDate", dateUtil.getStringTODate(billInward.getBillDate()));
        stringObjectMap.put("VendorName", billInward.getVendor().getName());
        stringObjectMap.put("Amount", billInward.getBillAmount());
        return stringObjectMap;
    }


    public void updateBillExchange(BillInward billInward) {

        BillExchange billExchange = new BillExchange();
        billExchange.setBillInwardDate(billInward.getReceivedDate());
        billExchange.setStatus("Pending");
        billExchange.setCreatedBy(billInward.getCreatedBy());
        billExchange.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billExchange.setBillDate(billInward.getBillDate());
        billExchange.setBillNumber(billInward.getBillNo());
        billExchange.setCreatedBy(billInward.getUpdatedBy());
        billExchangeRepository.save(billExchange);
    }

    public Map<String, Object> getExchangeValue(BillInward billInward) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("BillNo", billInward.getBillNo());
        stringObjectMap.put("BillDate", dateUtil.getStringTODate(billInward.getBillDate()));
        stringObjectMap.put("VendorName", billInward.getVendor().getName());
        stringObjectMap.put("Amount", billInward.getBillAmount());
        if (billInward.getItemReceived() != null) {
            stringObjectMap.put("PackageType", billInward.getItemReceived().get(0).getPackageType());
            stringObjectMap.put("PackageTypeQuantity", billInward.getItemReceived().get(0).getReceivedQuantity());
        } else {
            stringObjectMap.put("PackageType", "");
            stringObjectMap.put("PackageTypeQuantity", "");
        }
        return stringObjectMap;
    }

    public void updateBillForwardToReceive(BillForward billInward) {
        BillReceive billReceive = new BillReceive();
        billReceive.setBillInwardDate(billInward.getBillInwardDate());
        billReceive.setStatus("Pending");
        billReceive.setFromPerson(billInward.getFromPerson());
        billReceive.setCreatedBy(billInward.getCreatedBy());
        billReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billReceive.setBillDate(billInward.getBillDate());
        billReceive.setBillNumber(billInward.getBillNumber());
        billReceive.setCreatedBy(billInward.getUpdatedBy());
        billReceive.setForwardDate(billInward.getForwardDate());
        billReceive.setFromBranch(billInward.getFromBranch());
        billReceiveRepository.save(billReceive);
    }

    public void updateBillExchangeToReceive(BillExchange billExchange) {
        BillReceive billReceive = new BillReceive();
        billReceive.setBillInwardDate(billExchange.getBillInwardDate());
        billReceive.setStatus("Pending");
        billReceive.setCreatedBy(billExchange.getCreatedBy());
        billReceive.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billReceive.setBillDate(billExchange.getBillDate());
        billReceive.setBillNumber(billExchange.getBillNumber());
        billReceive.setCreatedBy(billExchange.getUpdatedBy());
        billReceive.setExchangeDate(billExchange.getHandOverDate());
        Optional<BillInward> optional = billInwardRepository.findByBillNoAndDate(billExchange.getBillNumber(), billExchange.getBillDate());
        if (optional.isPresent()) {
            billReceive.setFromBranch(optional.get().getInwardBranch());
        }
        billReceiveRepository.save(billReceive);
    }


    public void updateInwardInspectionEntry(BillInward billInward) {
        if (billInward.getItemReceived().isEmpty()) {
            for (BillInwardItemReceived billInwardProduct : billInward.getItemReceived()) {
                /*InwardInspectionEntry inspectionEntry = new InwardInspectionEntry();
                inspectionEntry.setInwardDate(billInward.getReceivedDate());
                inspectionEntry.setBillNo(billInward.getBillNo());
                inspectionEntry.setCreatedBy(billInward.getCreatedBy());
                inspectionEntry.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                inspectionEntry.setBatchNo(billInwardProduct.getBatchNo());
                inspectionEntry.setMaterial(billInwardProduct.getItem_Name());
                inspectionEntry.setCreatedBy(billInward.getUpdatedBy());
                inwardInspectionEntryRepository.save(inspectionEntry);*/
            }
        }
    }
}
