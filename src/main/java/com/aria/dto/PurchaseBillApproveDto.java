package com.aria.dto;

import com.aria.model.PurchaseBillApprove;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseBillApproveDto {


    public Long id;
    public Long billRefId;
    public Long vendorRefId;

    public String billNumber;

    public String billDate;
    public String billAmount;
    public Long packingType;
    public String packingTypeQty;
    public Long billType;
    public String lorryDate;
    public String lorrySerialNumber;
    public Long receivedDepartment;
    public Long exchangeDepartment;

    public Long company;

    public Long branch;
    public String billDescription;
    public Long receivedById;
    public Long exchangeById;
    public LocalDateTime createdTime;
    public Long billStatus;
    public Long activeFlag;
    public Long exchangeBranchId;
    public int qcFailureFlag;
    public int currencyFlag;
    public int purchaseReturnFlag;
    public int paymentType;
    public int mailStatus;
    public String tcsTaxPercentage;
    public String tcsTaxAmount;
    public String tcsFlag;


    private Double deliveryScore;

    private Double loyaltyScore;

    private Double qcScore;

    private Double totalScore;

    public PurchaseBillApproveDto(PurchaseBillApprove billApproveDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = billApproveDto.getId();
        this.billRefId = billApproveDto.getBillRefId();
        this.vendorRefId = billApproveDto.getVendorRefId();
        this.billNumber = billApproveDto.getBillNumber();
        if (billApproveDto.getBillDate() != null) {
            this.billDate = dateUtil.getDateTOStringWk(billApproveDto.getBillDate());
        }
        this.billAmount = billApproveDto.getBillAmount();
        this.packingType = billApproveDto.getPackingType();
        this.packingTypeQty = billApproveDto.getPackingTypeQty();
        this.billType = billApproveDto.getBillType();
        if (billApproveDto.getLorryDate() != null) {
            this.lorryDate = dateUtil.getDateTOStringWk(billApproveDto.getLorryDate());
        }
        this.lorrySerialNumber = billApproveDto.getLorrySerialNumber();
        this.receivedDepartment = billApproveDto.getReceivedDepartment();
        this.exchangeDepartment = billApproveDto.getExchangeDepartment();
        this.company = billApproveDto.getCompany();
        this.branch = billApproveDto.getBranch();
        this.billDescription = billApproveDto.getBillDescription();
        this.receivedById = billApproveDto.getReceivedById();
        this.exchangeById = billApproveDto.getExchangeById();
        this.createdTime = billApproveDto.getCreatedTime();
        this.billStatus = billApproveDto.getBillStatus();
        this.activeFlag = billApproveDto.getActiveFlag();
        this.exchangeBranchId = billApproveDto.getExchangeBranchId();
        this.qcFailureFlag = billApproveDto.getQcFailureFlag();
        this.currencyFlag = billApproveDto.getCurrencyFlag();
        this.purchaseReturnFlag = billApproveDto.getPurchaseReturnFlag();
        this.paymentType = billApproveDto.getPaymentType();
        this.mailStatus = billApproveDto.getMailStatus();
        this.tcsTaxPercentage = billApproveDto.getTcsTaxPercentage();
        this.tcsTaxAmount = billApproveDto.getTcsTaxAmount();
        this.tcsFlag = billApproveDto.getTcsFlag();
    }
}
