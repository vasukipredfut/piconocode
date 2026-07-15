package com.aria.dto;


import com.aria.model.BillInward;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BillInwardDto {


    private Long hoBillId;
    private Long id;

    private Long inwardBranchId;
    private String inwardBranchName;
    private Long deliverBranchId;
    private String deliverBranchName;

    private String receivedDate;
    private String billDate;

    private String roundOfType;
    private String roundOfValue;

    private Long vendorId;
    private String vendorName;
    private Long stateId;
    private String stateName;

    private String billNo;
    private String tcs;
    private Double tcsper;

    private Long firmOrgId;
    private String firmOrgName;

    private String status;

    private String ewayBill;
    private String cessTax;

    private String billCopy;
    private String productCategory;
    private String lorryDate;
    private String lorryWeight;
    private Double insurance;
    private Double billFreightAmount;
    private Double billAmount;
    private String billAmountStringValue;

    private Long createdBy;
    private Long updatedBy;
    private String inwardByName;

    private LocalDateTime created_date;
    private LocalDateTime updated_date;

    private List<BillInwardItemReceivedDto> itemReceives;

    private List<BillInwardLorryDto> lorryDetails;

    private String lorryWithWeight;
    private String lorrySerialNo ;
    private String lorryno ;
    private String lorryWithoutWeight;
    private Boolean lorry_freight;

    private Double cgst;
    private Double igst;
    private Double sgst;
    private Double cgstper;
    private Double igstper;
    private Double sgstper;
    
    private String perBagCost;
    private String camp;
    private Double lorryfreight;
    private Double dedLorryfreight;
    private Double otherAmount;
    private Double dedOtherAmount;
    private Double totBags;
    private String purpose;

    private String amendmentStatus;
    private String deleteStatus;

    public BillInwardDto(BillInward billInward) {
        this.id = billInward.getId();
        this.hoBillId = billInward.getHoBillId();
        if (billInward.getInwardBranch() != null) {
            this.inwardBranchId = billInward.getInwardBranch().getId();
            this.inwardBranchName = billInward.getInwardBranch().getBranchName();
        }
        if (billInward.getDeliverBranch() != null) {
            this.deliverBranchId = billInward.getDeliverBranch().getId();
            this.deliverBranchName = billInward.getDeliverBranch().getBranchName();
        }
        if (billInward.getVendor() != null) {
            this.vendorId = billInward.getVendor().getId();
            this.vendorName = billInward.getVendor().getName();
            this.stateId = billInward.getVendor().getStateid();
            this.stateName = billInward.getVendor().getState();
        }
        if (billInward.getFirmOrg() != null) {
            this.firmOrgId = billInward.getFirmOrg().getId();
            this.firmOrgName = billInward.getFirmOrg().getName();
        }

        this.billNo = billInward.getBillNo();
        this.tcs = billInward.getTcs();
        this.tcsper = billInward.getTcsper();
        this.status = billInward.getStatus();
        this.ewayBill = billInward.getEwayBill();
        this.cessTax = billInward.getCessTax();
        this.productCategory = billInward.getProductCategory();
        this.billCopy = billInward.getBillCopy();
        this.createdBy = billInward.getCreatedBy();
        this.updatedBy = billInward.getUpdatedBy();
        this.created_date = billInward.getCreated_date();
        this.updated_date = billInward.getUpdated_date();

        this.billAmount= (Double) billInward.getBillAmount();
        this.billFreightAmount=billInward.getBillFreightAmount();
        this.insurance=billInward.getInsurance();
        if(billInward.getLorryno() != null) {
        this.lorryno=billInward.getLorryno().toUpperCase();
        }
        this.lorryWithWeight = billInward.getLorry_with_Weight();
        this.lorryWithoutWeight=billInward.getLorry_without_Weight();
        this.lorry_freight=billInward.getLorry_freight();
        this.roundOfType=billInward.getRound_of_type();
        this.roundOfValue=billInward.getRound_of_value();
        this.lorrySerialNo=billInward.getLorrySerialNo();

        this.igst=billInward.getIgst();
        this.sgst=billInward.getSgst();
        this.cgst=billInward.getCgst();
        this.igstper=billInward.getIgstper();
        this.sgstper=billInward.getSgstper();
        this.cgstper=billInward.getCgstper();

        this.perBagCost=billInward.getPerBagCost();

        this.itemReceives = billInward.getItemReceived() != null
                ? billInward.getItemReceived().stream().map(field -> new BillInwardItemReceivedDto(field)).collect(Collectors.toList()) : null;

        this.lorryDetails = billInward.getBillInwardLorryDetails() != null
                ? billInward.getBillInwardLorryDetails().stream().map(field -> new BillInwardLorryDto(field)).collect(Collectors.toList()) : null;


        this.lorryfreight = billInward.getLorryfreight();
        this.dedLorryfreight = billInward.getDedLorryfreight();
        this.otherAmount = billInward.getOtherAmount();
        this.dedOtherAmount = billInward.getDedOtherAmount();
        this.totBags = billInward.getTotBags();
        this.perBagCost = billInward.getPerBagCost();
        this.purpose = billInward.getPerBagCost();
        this.camp = billInward.getCamp();
    }

}
