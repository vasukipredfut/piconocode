package com.aria.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.BillInwardDto;
import com.aria.dto.BillInwardItemReceivedDto;
import com.aria.model.*;

import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;


@Component
public class BillUtil {

    @Autowired
    BillForwardRepository billForwardRepository;

    @Autowired
    BillExchangeRepository billExchangeRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    VerifyUtil verifyUtil;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    BillForwardProductRepository billForwardProductRepository;

    @Autowired
    BillExchangeProductRepository billExchangeProductRepository;

    @Autowired
    DirectPurchaseRepository directPurchaseRepository;

    @Autowired
    DpEntryStoredProductRepository storedProductRepository;

    @Autowired
    ColorRepository colorRepository;

    @Autowired
    CampRepository campRepository;


    public void updateBillExchange(BillInward billInward, List<BillInwardItemReceivedDto> billInwardProducts) {
        BillExchange billExchange = new BillExchange();
        billExchange.setBillInwardDate(billInward.getReceivedDate());
        billExchange.setStatus("Pending");
        billExchange.setVerificationStatus("Pending");
        billExchange.setCreatedBy(billInward.getCreatedBy());
        billExchange.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billExchange.setBillDate(billInward.getBillDate());
        billExchange.setBillNumber(billInward.getBillNo());
        billExchange.setVendor(billInward.getVendor());
        billExchange.setBillAmount(billInward.getBillAmount());
        billExchange.setCreatedBy(billInward.getUpdatedBy());
        billExchange.setBillInwardId(billInward.getId());
        billExchange.setHandOverToBranch(billInward.getDeliverBranch());
        if (billInward.getFirmOrg() != null) {
            billExchange.setFirm_org_id(billInward.getFirmOrg().getId());
        }
        BillExchange result = billExchangeRepository.save(billExchange);
        updateBillExchangeProduct(result, billInwardProducts);
    }


    public void updateBillForward(BillInward billInward, List<BillInwardItemReceivedDto> billInwardProducts) {
        BillForward billForward = new BillForward();
        billForward.setBill_ref_id(billInward.getId());
        if (billInward.getFirmOrg() != null) {
            billForward.setFirm_org_id(billInward.getFirmOrg().getId());
        }
        billForward.setBillInwardDate(billInward.getReceivedDate());
        billForward.setStatus("Pending");
        billForward.setCreatedBy(billInward.getCreatedBy());
        billForward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billForward.setBillDate(billInward.getBillDate());
        billForward.setFromBranch(billInward.getDeliverBranch());
        billForward.setBillNumber(billInward.getBillNo());
        billForward.setVendor(billInward.getVendor());
        billForward.setCreatedBy(billInward.getUpdatedBy());
        BillForward result = billForwardRepository.save(billForward);
        updateBillForwardProduct(result, billInwardProducts);
    }

    public void updateBillForwardProduct(BillForward billForward, List<BillInwardItemReceivedDto> billInwardProducts) {
        for (BillInwardItemReceivedDto billInwardProduct : billInwardProducts) {
            BillForwardProduct billForwardProduct = new BillForwardProduct(billInwardProduct);
            billForwardProduct.setForwardId(billForward.getId());
            billForwardProductRepository.save(billForwardProduct);
        }
    }

    public void updateBillExchangeProduct(BillExchange billExchange, List<BillInwardItemReceivedDto> billInwardProducts) {
        for (BillInwardItemReceivedDto billInwardProduct : billInwardProducts) {
            BillExchangeProduct billForwardProduct = new BillExchangeProduct(billInwardProduct);
            billForwardProduct.setExchangeId(billExchange.getId());
            billExchangeProductRepository.save(billForwardProduct);
        }
    }

    public void updateDpEntry(BillInward billInward, BillInwardDto inwardDto, List<BillInwardItemReceivedDto> billInwardProducts) {


        DirectPurchase directPurchase = null;
        String status = null;
        Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findBy(billInward.getBillNo(), billInward.getId());
        if (optionalDirectPurchase.isPresent()) {
            directPurchase = optionalDirectPurchase.get();
            status = directPurchase.getStock_status();
        } else {
            directPurchase = new DirectPurchase();
            directPurchase.setBillRefId(billInward.getId());
        }

        directPurchase.setStorageDate(billInward.getReceivedDate());
        if (billInward.getCreatedBy() != null) {
            directPurchase.setCreatedBy(billInward.getCreatedBy());
        } else {
            directPurchase.setCreatedBy(billInward.getUpdatedBy());
        }
        directPurchase.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        directPurchase.setReferenceNo(billInward.getBillNo());
        directPurchase.setBill_amount(billInward.getBillAmount());
        directPurchase.setInward_date(billInward.getReceivedDate());
        directPurchase.setLorry_date(billInward.getLorryDate());
        directPurchase.setBill_date(billInward.getBillDate());
        directPurchase.setStock_status("Pending");
        directPurchase.setBranch(billInward.getDeliverBranch());
        directPurchase.setVendor_id(billInward.getVendor().getId());
        directPurchase.setProductCategory(billInward.getProductCategory());

        if (billInward.getFirmOrg() != null) {
            directPurchase.setFirm_org_id(billInward.getFirmOrg().getId());
        }

        Optional<Camp> campOptional = campRepository.fndByCampName(inwardDto.getCamp());
        if (campOptional.isPresent()) {
            directPurchase.setCamp(campOptional.get());
        } else {
            System.out.println(inwardDto.getCamp());
            if(!inwardDto.getCamp().isEmpty() && inwardDto.getCamp() != null ) {
                Camp camp = new Camp();
                camp.setCamp(inwardDto.getCamp());
                Camp rs = campRepository.save(camp);
                directPurchase.setCamp(rs);
            }
        }

        DirectPurchase result = directPurchaseRepository.save(directPurchase);

        if (status == null || status.equalsIgnoreCase("pending")) {

            List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(result.getId());
            if (!dpEntryStoredProducts.isEmpty()) {
                storedProductRepository.deleteAll(dpEntryStoredProducts);
            }

            for (BillInwardItemReceivedDto billInwardProduct : billInwardProducts) {

                DpEntryStoredProduct storedProduct = new DpEntryStoredProduct();

                storedProduct.setEntryId(result.getId());
                storedProduct.setBatchNo(billInwardProduct.getBatchNo());
                storedProduct.setBatchId(billInwardProduct.getBatchId());
                storedProduct.setTotalQuantity(billInwardProduct.getReceivedQuantity());
                storedProduct.setItemId(billInwardProduct.getItemId());
                storedProduct.setItemName(billInwardProduct.getItemName());
                storedProduct.setItemType(billInwardProduct.getItemType());
                storedProduct.setPackageType(billInwardProduct.getPackageType());
                storedProduct.setUnitCost(billInwardProduct.getUnitCost());
                storedProduct.setUnitRate(billInwardProduct.getUnitRate());
                storedProduct.setLot(billInwardProduct.getLot());
                storedProduct.setPo_id(billInwardProduct.getPoId());
                storedProduct.setPo_number(billInwardProduct.getPoNumber());
                storedProduct.setBill_ref_id(billInward.getId());
                Optional<Color> color = colorRepository.findBy(billInwardProduct.getColor());
                if (color.isPresent()) {
                    storedProduct.setColor(color.get());
                }
                storedProductRepository.save(storedProduct);
            }
        }
    }


    public void invokeUpdateDpEntry(BillExchange billExchange , Long billinwardId) {

        Optional<BillInward> billInwardget = billInwardRepository.findById(billinwardId);

        if (billInwardget.isPresent()) {

            BillInward billInward = billInwardget.get();

            BillInwardDto inwardDto = new BillInwardDto(billInwardget.get());

            List<BillInwardItemReceivedDto> billInwardItemReceivedDto = billInward.getItemReceived().stream().map(val -> new BillInwardItemReceivedDto(val)).collect(Collectors.toList());

            updateDpEntryFromBillExchange(billExchange, billInward, inwardDto, billInwardItemReceivedDto);

        }
    }


    public void updateBillApproveToForward(BillInward billInward, List<BillInwardItemReceivedDto> billInwardProducts, User loggedUser) {
        BillForward billForward = new BillForward();
        billForward.setBill_ref_id(billInward.getId());
        billForward.setBillInwardDate(billInward.getReceivedDate());
        billForward.setStatus("Pending");
        billForward.setCreatedBy(loggedUser.getId()); // approvedBy
        billForward.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        billForward.setBillDate(billInward.getBillDate());
        billForward.setFromBranch(billInward.getDeliverBranch()); // delivery  branch
        billForward.setBillNumber(billInward.getBillNo());
        billForward.setVendor(billInward.getVendor());
        billForward.setBillApprovedBy(loggedUser.getId());
        billForward.setBill_approved_date(dateUtil.getISTDate());
        billForward.setVendor(billInward.getVendor());

        if(billInward.getFirmOrg() != null){
            billForward.setFirm_org_id(billInward.getFirmOrg().getId());
        }


        BillForward result = billForwardRepository.save(billForward);
        updateBillForwardProduct(result, billInwardProducts);
    }





    public void updateDpEntryFromBillExchange(BillExchange billExchange , BillInward billInward, BillInwardDto inwardDto, List<BillInwardItemReceivedDto> billInwardProducts) {


        DirectPurchase directPurchase = null;
        String status = null;
        Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findBy(billInward.getBillNo(), billInward.getId());
        if (optionalDirectPurchase.isPresent()) {
            directPurchase = optionalDirectPurchase.get();
            status = directPurchase.getStock_status();
        } else {
            directPurchase = new DirectPurchase();
            directPurchase.setBillRefId(billInward.getId());
        }

        directPurchase.setStorageDate(billInward.getReceivedDate());
        if (billInward.getCreatedBy() != null) {
            directPurchase.setCreatedBy(billInward.getCreatedBy());
        } else {
            directPurchase.setCreatedBy(billInward.getUpdatedBy());
        }
        directPurchase.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        directPurchase.setReferenceNo(billInward.getBillNo());
        directPurchase.setBill_amount(billInward.getBillAmount());
        directPurchase.setInward_date(billInward.getReceivedDate());
        directPurchase.setLorry_date(billInward.getLorryDate());
        directPurchase.setBill_date(billInward.getBillDate());
        directPurchase.setStock_status("Pending");
        directPurchase.setBranch(billInward.getDeliverBranch());
        directPurchase.setVendor_id(billInward.getVendor().getId());
        directPurchase.setProductCategory(billInward.getProductCategory());
        directPurchase.setHandOverDepartment(billExchange.getHandOverDepartment());

        if (billInward.getFirmOrg() != null) {
            directPurchase.setFirm_org_id(billInward.getFirmOrg().getId());
        }

        Optional<Camp> campOptional = campRepository.fndByCampName(inwardDto.getCamp());
        if (campOptional.isPresent()) {
            directPurchase.setCamp(campOptional.get());
        } else {
            System.out.println(inwardDto.getCamp());
            Camp camp = new Camp();
            camp.setCamp(inwardDto.getCamp());
            Camp rs = campRepository.save(camp);
            directPurchase.setCamp(rs);
        }

        DirectPurchase result = directPurchaseRepository.save(directPurchase);

        if (status == null || status.equalsIgnoreCase("pending")) {

            List<DpEntryStoredProduct> dpEntryStoredProducts = storedProductRepository.findBy(result.getId());
            if (!dpEntryStoredProducts.isEmpty()) {
                storedProductRepository.deleteAll(dpEntryStoredProducts);
            }

            for (BillInwardItemReceivedDto billInwardProduct : billInwardProducts) {

                DpEntryStoredProduct storedProduct = new DpEntryStoredProduct();

                storedProduct.setEntryId(result.getId());
                storedProduct.setBatchNo(billInwardProduct.getBatchNo());
                storedProduct.setBatchId(billInwardProduct.getBatchId());
                storedProduct.setTotalQuantity(billInwardProduct.getReceivedQuantity());
                storedProduct.setItemId(billInwardProduct.getItemId());
                storedProduct.setItemName(billInwardProduct.getItemName());
                storedProduct.setItemType(billInwardProduct.getItemType());
                storedProduct.setPackageType(billInwardProduct.getPackageType());
                storedProduct.setUnitCost(billInwardProduct.getUnitCost());
                storedProduct.setUnitRate(billInwardProduct.getUnitRate());
                storedProduct.setLot(billInwardProduct.getLot());
                storedProduct.setPo_id(billInwardProduct.getPoId());
                storedProduct.setPo_number(billInwardProduct.getPoNumber());
                storedProduct.setBill_ref_id(billInward.getId());
                Optional<Color> color = colorRepository.findBy(billInwardProduct.getColor());
                if (color.isPresent()) {
                    storedProduct.setColor(color.get());
                }
                storedProductRepository.save(storedProduct);
            }
        }
    }
}
