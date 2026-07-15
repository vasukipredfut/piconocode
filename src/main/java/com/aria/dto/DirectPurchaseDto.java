package com.aria.dto;


import com.aria.model.DirectPurchase;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Setter
@Getter
@NoArgsConstructor
public class DirectPurchaseDto {

    private Long id;

    private String referenceNo;

    private Long branchId;

    private String branchName;

    private String storageDate;

    private Long createdBy;

    private Long updatedBy;

    private String stockInwardBy;
    private String stockInwardByDate;
    private String stockInwardByDateTime;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<DpEntryStoredProductDto> storedProducts;

    private Long vendorId;

    private String inwardDate;

    private String lorryDate;
    private String billDate;

    private Double billAmount;
    private Double lorryFreightAmount;
    private String lorryFreightAmountValue;
    private String billAmountStringValue;

    private String stockStatus;

    private String productCategory;

    private String billVerified;

    public Long campId;

    public String campName;

    public Long billRefId;

    List<StoreBinStockObjectDto> binStockObjects;

    private Boolean excessOrShortage;

    public Long firmOrgId;

    private String creditOrDebit;
    private String eTransfer;
    private Long verifiedBy;
    private String verifiedByName;
    private String verifiedDate;
    private String verifiedDateTime;
    private Long approvedBy;
    private String approvedByName;
    private String approvedDate;
    private String approvedDateTime;
    private Long authorisedBy;   //md
    private String authorisedByName;  //md
    private String authorisedDate;
    private String authorisedDateTime;
    private Long rejectedBy;
    private String rejectedByName;
    private String rejectedDate;
    private String rejectedDateTime;

    private Long handOverToDepartment;

    private String handOverToDepartmentName;

    public DirectPurchaseDto(DirectPurchase directPurchase) {

        this.id = directPurchase.getId();
        this.referenceNo = directPurchase.getReferenceNo();
        if (directPurchase.getBranch() != null) {
            this.branchId = directPurchase.getBranch().getId();
            this.branchName = directPurchase.getBranch().getBranchName();
        }

        this.createdBy = directPurchase.getCreatedBy();
        this.updatedBy = directPurchase.getUpdatedBy();
        this.created_date = directPurchase.getCreated_date();
        DateUtil dateUtil = new DateUtil();
        if (directPurchase.getStorageDate() != null) {
            this.storageDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getStorageDate());
        }
        if (directPurchase.getBill_date() != null) {
            this.billDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getBill_date());
        }
        if (directPurchase.getLorry_date() != null) {
            this.lorryDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getLorry_date());
        }
        if (directPurchase.getUpdated_date() != null) {
            this.updated_date = directPurchase.getUpdated_date();
            this.stockInwardByDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getUpdated_date());
            this.stockInwardByDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getUpdated_date());
        } else {
            this.stockInwardByDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getCreated_date());
            this.stockInwardByDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getCreated_date());
            this.updated_date = directPurchase.getCreated_date();
        }

        this.vendorId = directPurchase.getVendor_id();
        if (directPurchase.getBill_amount() != null) {
            this.billAmount = directPurchase.getBill_amount();
            this.billAmountStringValue = new BigDecimal(directPurchase.getBill_amount()).toPlainString();
        }
        if (directPurchase.getLorry_freight_amount() != null) {
            this.lorryFreightAmount = directPurchase.getLorry_freight_amount();
            this.lorryFreightAmountValue = new BigDecimal(directPurchase.getLorry_freight_amount()).toPlainString();
        }
        if (directPurchase.getStorageDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getInward_date());
        }
        this.stockStatus = directPurchase.getStock_status();
        this.approvedBy = directPurchase.getApproved_by();
        this.productCategory = directPurchase.getProductCategory();
        this.storedProducts = directPurchase.getStoredProducts() != null ? directPurchase.getStoredProducts().stream().map(field -> new DpEntryStoredProductDto(field)).collect(Collectors.toList()) : null;

        if (directPurchase.getCamp() != null) {
            this.campId = directPurchase.getCamp().getId();
            this.campName = directPurchase.getCamp().getCamp();
        }
        this.billRefId = directPurchase.getBillRefId();
        this.excessOrShortage = directPurchase.getExcessOrShortage();
        this.firmOrgId = directPurchase.getFirm_org_id();
        this.creditOrDebit = directPurchase.getCredit_or_debit();
        this.eTransfer = directPurchase.getETransfer();
        this.verifiedBy = directPurchase.getVerified_by();
        if (directPurchase.getVerified_date() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getVerified_date());
            this.verifiedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getVerified_date());
        }
        this.authorisedBy = directPurchase.getAuthorised_by();
        if (directPurchase.getAuthorised_date() != null) {
            this.authorisedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getAuthorised_date());
            this.authorisedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getAuthorised_date());
        }
        this.approvedBy = directPurchase.getApproved_by();
        if (directPurchase.getApproved__date() != null) {
            this.approvedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getApproved__date());
            this.approvedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getApproved__date());
        }
        this.rejectedBy = directPurchase.getRejected_by();
        if (directPurchase.getRejected_date() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getRejected_date());
            this.rejectedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getRejected_date());
        }
        if (directPurchase.getHandOverDepartment() != null) {
            this.handOverToDepartment = directPurchase.getHandOverDepartment().getId();
            this.handOverToDepartmentName = directPurchase.getHandOverDepartment().getName();
        }
    }


    public DirectPurchaseDto(DirectPurchaseDetails directPurchase) {
        this.id = directPurchase.getId();
        this.referenceNo = directPurchase.getReferenceNo();
        this.branchId = directPurchase.getBranchId();
        this.branchName = directPurchase.getBranchName();
        this.createdBy = directPurchase.getCreatedBy();
        this.updatedBy = directPurchase.getUpdatedBy();
        this.created_date = directPurchase.getStockInwardByDate1();
        DateUtil dateUtil = new DateUtil();
        if (directPurchase.getStorageDate() != null) {
            this.storageDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getStorageDate());
        }
        if (directPurchase.getBillDate() != null) {
            this.billDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getBillDate());
        }
        if (directPurchase.getLorryDate() != null) {
            this.lorryDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getLorryDate());
        }
        if (directPurchase.getStockInwardBy() != null) {
            this.stockInwardBy = directPurchase.getStockInwardBy();
        }else {
            this.stockInwardBy = directPurchase.getStockInwardBy();
        }
        if (directPurchase.getStockInwardByDate1() != null) {
            this.updated_date = directPurchase.getStockInwardByDate1();
            this.stockInwardByDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getStockInwardByDate1());
            this.stockInwardByDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getStockInwardByDate1());
        } else {
            this.stockInwardByDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getStockInwardByDate());
            this.stockInwardByDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getStockInwardByDate());
            this.updated_date = directPurchase.getStockInwardByDate();
        }

        this.vendorId = directPurchase.getVendorId();
        this.billRefId = directPurchase.getBillRefId();
        if (directPurchase.getBillAmount() != null) {
            this.billAmount = directPurchase.getBillAmount();
            this.billAmountStringValue = new BigDecimal(directPurchase.getBillAmount()).toPlainString();
        }
        if (directPurchase.getLorryFreightAmount() != null) {
            this.lorryFreightAmount = directPurchase.getLorryFreightAmount();
            this.lorryFreightAmountValue = new BigDecimal(directPurchase.getLorryFreightAmount()).toPlainString();
        }
        if (directPurchase.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(directPurchase.getInwardDate());
        }
        this.stockStatus = directPurchase.getStockStatus();
        this.approvedBy = directPurchase.getApprovedBy();
        this.productCategory = directPurchase.getProductCategory();
        this.campId = directPurchase.getCampId();
        this.campName = directPurchase.getCampName();
        this.billRefId = directPurchase.getBillRefId();
        this.excessOrShortage = directPurchase.getExcessOrShortage();
        this.firmOrgId = directPurchase.getFirmOrgId();
        this.creditOrDebit = directPurchase.getCreditOrDebit();
        this.eTransfer = directPurchase.geteTransfer();
        this.verifiedBy = directPurchase.getVerifiedBy();
        this.verifiedByName = directPurchase.getVerifiedByName();
        if (directPurchase.getVerifiedDate() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getVerifiedDate());
            this.verifiedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getVerifiedDate());
        }
        this.authorisedBy = directPurchase.getAuthorisedBy();
        this.authorisedByName = directPurchase.getAuthorisedByName();
        if (directPurchase.getAuthorisedDate() != null) {
            this.authorisedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getAuthorisedDate());
            this.authorisedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getAuthorisedDate());
        }
        this.approvedBy = directPurchase.getApprovedBy();
        this.approvedByName = directPurchase.getApprovedByName();
        if (directPurchase.getApprovedDate() != null) {
            this.approvedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getApprovedDate());
            this.approvedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getApprovedDate());
        }
        this.rejectedBy = directPurchase.getRejectedBy();
        this.rejectedByName = directPurchase.getRejectedByName();
        if (directPurchase.getRejectedDate() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToDateString(directPurchase.getRejectedDate());
            this.rejectedDateTime = dateUtil.getLocalDateTimeToDateTimeString(directPurchase.getRejectedDate());
        }
        this.handOverToDepartment = directPurchase.getHandOverToDepartment();
        this.handOverToDepartmentName = directPurchase.getHandOverToDepartmentName();
    }


}
