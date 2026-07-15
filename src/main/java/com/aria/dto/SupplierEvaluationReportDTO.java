package com.aria.dto;

import java.util.Date;


import com.aria.model.SupplierEvaluationReport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierEvaluationReportDTO {

    private Long id;
    private Long branch;
    private String billId;
    private String billNo;
    private Long company;
    private Date deliveryDueDate;
    private Integer deliveryScore;
    private Date inwardDate;
    private Long ledgerId;
    private Integer loyaltyScore;
    private Long poId;
    private String period;
    private Integer qualityScore;
    private String trnNo;
    private Integer year;

   

    public SupplierEvaluationReportDTO(SupplierEvaluationReport model) {
        this.id = model.getId();
        this.branch = model.getBranch();
        this.billId = model.getBillId();
        this.billNo = model.getBillNo();
        this.company = model.getCompany();
        this.deliveryDueDate = model.getDeliveryDueDate();
        this.deliveryScore = model.getDeliveryScore();
        this.inwardDate = model.getInwardDate();
        this.ledgerId = model.getLedgerId();
        this.loyaltyScore = model.getLoyaltyScore();
        this.poId = model.getPoId();
        this.period = model.getPeriod();
        this.qualityScore = model.getQualityScore();
        this.trnNo = model.getTrnNo();
        this.year = model.getYear();
    }

}
