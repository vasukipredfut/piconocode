package com.aria.model;

import com.aria.dto.PurchaseBillApproveDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_bill_approve")
public class PurchaseBillApprove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name ="bill_ref_id" )
    private Long billRefId;
    @Column(name ="vendor_ref_id" )
    public Long vendorRefId;

    @Column(name ="bill_Number" )
    public String billNumber;

    @Column(name ="bill_Date" )
    public Date billDate;

    @Column(name ="bill_Amount" )
    public String billAmount;
    @Column(name ="packing_Type" )
    public Long packingType;
    @Column(name ="packing_Type_Qty" )
    public String packingTypeQty;
    @Column(name ="bill_Type" )
    public Long billType;

    @Column(name ="lorry_Date" )
    public Date lorryDate;
    @Column(name ="lorry_Serial_Number" )
    public String lorrySerialNumber;
    @Column(name ="received_Department" )
    public Long receivedDepartment;
    @Column(name ="exchange_Department" )
    public Long exchangeDepartment;

    @Column(name ="company" )
    public Long company;

    @Column(name ="branch" )
    public Long branch;
    @Column(name ="bill_Description" )
    public String billDescription;
    @Column(name ="received_By_Id" )
    public Long receivedById;
    @Column(name ="exchange_By_Id" )
    public Long exchangeById;
    @Column(name ="created_Time" )
    public LocalDateTime createdTime;
    @Column(name ="bill_Status" )
    public Long billStatus;

    @Column(name ="active_Flag" )
    public Long activeFlag;
    @Column(name ="exchange_Branch_Id" )
    public Long exchangeBranchId;
    @Column(name ="qc_Failure_Flag" )
    public int qcFailureFlag;
    @Column(name ="currency_Flag" )
    public int currencyFlag;
    @Column(name ="purchaseReturn_Flag" )
    public int purchaseReturnFlag;
    @Column(name ="payment_type" )
    public int paymentType;
    @Column(name ="mail_Status" )
    public int mailStatus;
    @Column(name ="tcs_tax_percentage" )
    public String tcsTaxPercentage;
    @Column(name ="tcs_tax_amount" )
    public String tcsTaxAmount;
    @Column(name ="tcs_flag" )
    public String tcsFlag;


    public PurchaseBillApprove(PurchaseBillApproveDto billApproveDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = billApproveDto.getId();
        this.billRefId = billApproveDto.getBillRefId();
        this.vendorRefId = billApproveDto.getVendorRefId();
        this.billNumber = billApproveDto.getBillNumber();
        if (billApproveDto.getBillDate() != null) {
            this.billDate = dateUtil.getStringTODateWk(billApproveDto.getBillDate());
        }
        this.billAmount = billApproveDto.getBillAmount();
        this.packingType = billApproveDto.getPackingType();
        this.packingTypeQty = billApproveDto.getPackingTypeQty();
        this.billType = billApproveDto.getBillType();
        if (billApproveDto.getLorryDate() != null) {
            this.lorryDate = dateUtil.getStringTODateWk(billApproveDto.getLorryDate());
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
