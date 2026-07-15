package com.aria.dto;

public interface IssueItemsBrief {

    Long getIssueItemId();

    String getItemName();

    Long getItemId();

    String getLot();

    Double getRequestedQuantity();

    Double getAllocatedQuantity();

    Double getPendingQuantity();
}
