package com.aria.dto;


import java.math.BigDecimal;
import java.util.List;

import com.aria.model.DirectPurchaseList;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class DirectPurchaseListDto {

    private Long id;

    private Long branchId;

    private String branchName;
    
    private String vendorName;

    private String inwardDate;
    private String inwardDateTime;

    private Long billInwardId;

    private String billNumber;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String gstNumber;
    
    private Double subTotalAmount;
    
    private Double sgstAmount;

    private Double cgstAmount;
    
    private Double igstAmount;
    
    private  Double sgst;
    
    private Double igst;
    
    private Double cgst;

    private Double cgstper;
    private Double igstper;
    private Double sgstper;
    
    private Double billAmount;

    private String billAmountStringValue;
    private String lorryDate;
    private String billDate;
    private String status;
    private Long vendor;
    private Double billFrightAmount;
    private String billInwardperson;
    private String stockInwardperson;
    private String billApprovePerson;
    private String billAuthorizedPerson;

    private String ewayBill;


    private String lorryWithWeight;
    private String lorryWithoutWeight;

    private List<DpEntryStoredProductDto> dpStoredProduct;
    private List<BillInwardLorryDto> lorryDetails;

    private String lorrySerialNo ;
    private String lorryno ;
    private Double insurance;
    private String cessTax;
    private String roundOfType;
    private String roundOfValue;
    private String productCategory;
    private Boolean lorryFreight;

    private Double lorryfreightValue;
    private Double dedLorryfreight;

    private String tcs;
    private Double tcsper;

    public DirectPurchaseListDto(DirectPurchaseList directPurchaseList) {
        DateUtil dateUtil = new DateUtil();
        this.id=directPurchaseList.getId();
        this.branchId = directPurchaseList.getBranchId();
        this.branchName = directPurchaseList.getBranchName();
        this.billInwardId = directPurchaseList.getBillInwardId();
        if (directPurchaseList.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(directPurchaseList.getInwardDate());
        }
        if (directPurchaseList.getBillDate() != null) {
            this.billDate = dateUtil.getDateToStringDDMMYYYY(directPurchaseList.getBillDate());
        }
        if (directPurchaseList.getLorryDate() != null) {
            this.lorryDate = dateUtil.getDateToStringDDMMYYYY(directPurchaseList.getLorryDate());
        }
        this.address=directPurchaseList.getAddress();
        if(directPurchaseList.getBillAmount() != null) {
            this.billAmount = directPurchaseList.getBillAmount();
            this.billAmountStringValue = new BigDecimal(directPurchaseList.getBillAmount()).toPlainString();
        }
        this.billNumber=directPurchaseList.getBillNumber();

        this.cgstAmount=directPurchaseList.getCgstAmount();
        this.city=directPurchaseList.getCity();
        this.gstNumber=directPurchaseList.getGstNumber();

        this.igstAmount=directPurchaseList.getIgstAmount();
        this.sgstAmount=directPurchaseList.getSgstAmount();

        this.state=directPurchaseList.getState();
        this.subTotalAmount=directPurchaseList.getSubTotalAmount();
        this.vendorName=directPurchaseList.getVendorName();
        this.status=directPurchaseList.getStatus();
        this.vendor=directPurchaseList.getVendor();
        this.productCategory=directPurchaseList.getProductCategory();
        this.lorryWithoutWeight=directPurchaseList.getLorryWithoutWeight();

        this.billFrightAmount=directPurchaseList.getBillFrightAmount();
        this.ewayBill=directPurchaseList.getEwayBill();
        this.lorryWithoutWeight=directPurchaseList.getLorryWithoutWeight();
        this.lorryWithWeight=directPurchaseList.getLorryWithWeight();
        this.lorrySerialNo=directPurchaseList.getLorrySerialNo();
        this.lorryFreight=directPurchaseList.getLorryFreight();
        this.dedLorryfreight=directPurchaseList.getDedLorryfreight();
        this.lorryfreightValue=directPurchaseList.getLorryfreightValue();
        this.tcs=directPurchaseList.getTcs();
        this.tcsper=directPurchaseList.getTcsper();
        this.cessTax=directPurchaseList.getCessTax();
        this.roundOfType=directPurchaseList.getRoundOfType();
        this.insurance=directPurchaseList.getInsurance();
        this.lorryno=directPurchaseList.getLorryno();
        this.roundOfValue=directPurchaseList.getRoundOfValue();
        this.roundOfValue=directPurchaseList.getRoundOfValue();
        this.stockInwardperson=directPurchaseList.getStockInwardperson();

        if(directPurchaseList.getUpdated_byname() != null){
            this.billInwardperson = directPurchaseList.getUpdated_byname();
            this.billApprovePerson = directPurchaseList.getUpdated_byname();
        }else if(directPurchaseList.getCreated_byname() != null){
            this.billInwardperson = directPurchaseList.getCreated_byname();
        }

        if(productCategory.equalsIgnoreCase("RawMaterial")){
            this.cgst=directPurchaseList.getCgst();
            this.cgstper=directPurchaseList.getCgst();
            this.igst=directPurchaseList.getIgst();
            this.igstper=directPurchaseList.getIgstper();
            this.sgst=directPurchaseList.getSgst();
            this.sgstper=directPurchaseList.getSgstper();
        }

        if(directPurchaseList.getUpdated_date() != null){
            this.inwardDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchaseList.getUpdated_date());
        }else if(directPurchaseList.getCreated_date() != null){
            this.inwardDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchaseList.getCreated_date());
        }

    }
}
