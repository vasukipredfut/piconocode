package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.SupplierEvaluationReportList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierEvaluationReportListDTO {

    private Long id;

    private Long branch;
    private String billId;
    private String billNo;
    private Long company;
    private Date deliveryDueDate;
    private Integer deliveryScore;
    private Date inwardDate;
    private Long ledgerId;
    private String vendorName;
    private Integer loyaltyScore;
    private Long poid;
    private String poNumber;
    private String period;
    private Integer qualityScore;
    private String trnNo;
    private Integer year;
    
    public SupplierEvaluationReportListDTO(SupplierEvaluationReportList supplierEvaluationReportList)
    {
    	this.id = supplierEvaluationReportList.getId();
    	this.branch = supplierEvaluationReportList.getBranch();
    	this.billId = supplierEvaluationReportList.getBillId();
    	this.billNo = supplierEvaluationReportList.getBillNo();;
    	this.company = supplierEvaluationReportList.getCompany();
    	this.deliveryDueDate = supplierEvaluationReportList.getDeliveryDueDate();
    	this.deliveryScore = supplierEvaluationReportList.getDeliveryScore();
    	this.inwardDate = supplierEvaluationReportList.getInwardDate();
    	this.ledgerId = supplierEvaluationReportList.getLedgerId();
    	this.vendorName = supplierEvaluationReportList.getVendorName();
    	this.loyaltyScore = supplierEvaluationReportList.getLoyaltyScore();
    	this.poid = supplierEvaluationReportList.getPoid();
    	this.poNumber = supplierEvaluationReportList.getPoNumber();
    	this.period = supplierEvaluationReportList.getPeriod();
    	this.qualityScore = supplierEvaluationReportList.getQualityScore();
    	this.trnNo = supplierEvaluationReportList.getTrnNo();
    	this.year = supplierEvaluationReportList.getYear();
    }

}
