package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.SupplierEvaluationAttributes;
import com.aria.model.SupplierEvaluationReport;
import com.aria.model.SupplierEvaluationReportVendorScore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierEvaluationVendorScoreDTO {


    private Integer deliveryScore;
    private Long ledgerId;
    private Integer loyaltyScore;
    private Integer qualityScore;
    private Integer year;

   

    public SupplierEvaluationVendorScoreDTO(SupplierEvaluationReportVendorScore model) {
   
        this.deliveryScore = model.getDeliveryScore();
        this.ledgerId = model.getLedgerId();
        this.loyaltyScore = model.getLoyaltyScore();
        this.qualityScore = model.getQualityScore();
        this.year = model.getYear();
    }

}
