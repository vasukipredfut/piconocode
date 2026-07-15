package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationReportListDto {

    public Long branchId;
    public String branchName;
    public String trnNo;
    public Long firmId;
    public Long partyId;
    public String partyName;

    public Integer year;
    public String period;
    public double qualityScore = 0.0;
    public double loyaltyScore = 0.0;
    public double deliveryScore = 0.0;

    public String status;

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

    public EvaluationReportListDto(EvaluationReportList evaluationReportList) {
        this.branchId = evaluationReportList.getBranchId();
        this.branchName = evaluationReportList.getBranchName();
        this.trnNo = evaluationReportList.getTrnNo();
        this.firmId = evaluationReportList.getFirmId();
        this.partyId = evaluationReportList.getPartyId();
        this.partyName = evaluationReportList.getPartyName();
        this.year = evaluationReportList.getYear();
        this.period = evaluationReportList.getPeriod();
        this.status = evaluationReportList.getStatus();
        if (evaluationReportList.getQualityScore() != null)
            this.qualityScore = evaluationReportList.getQualityScore();
        if (evaluationReportList.getLoyaltyScore() != null)
            this.loyaltyScore = evaluationReportList.getLoyaltyScore();
        if (evaluationReportList.getDeliveryScore() != null)
            this.deliveryScore = evaluationReportList.getDeliveryScore();
    }
}
