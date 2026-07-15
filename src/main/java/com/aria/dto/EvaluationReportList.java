package com.aria.dto;

public interface EvaluationReportList {

    Long getBranchId();
    String getBranchName();
    String getTrnNo();

    Long getFirmId();

    Long getPartyId();

    String getPartyName();

    Integer getYear();

    String getPeriod();
    String getStatus();

    Double getQualityScore();

    Double getLoyaltyScore();

    Double getDeliveryScore();

}
