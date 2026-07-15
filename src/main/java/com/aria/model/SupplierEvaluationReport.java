package com.aria.model;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.SupplierEvaluationReportDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "supplierevaluationreport")
public class SupplierEvaluationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch")
    private Long branch;

    @Column(name = "billid")
    private String billId;

    @Column(name = "billno")
    private String billNo;

    @Column(name = "company")
    private Long company;

    @Column(name = "deliveryduedate")
    private Date deliveryDueDate;

    @Column(name = "deliveryscore")
    private Integer deliveryScore;

    @Column(name = "inwarddate")
    private Date inwardDate;

    @Column(name = "ledgerid")
    private Long ledgerId;

    @Column(name = "loyaltyscore")
    private Integer loyaltyScore;

    @Column(name = "poid")
    private Long poId;

    @Column(name = "period")
    private String period;

    @Column(name = "qualityscore")
    private Integer qualityScore;

    @Column(name = "trnno")
    private String trnNo;

    @Column(name = "year")
    private Integer year;


    public SupplierEvaluationReport(SupplierEvaluationReportDTO model) {
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
