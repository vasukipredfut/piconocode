package com.aria.dto;

public interface VendorSpecList {

    Long getVendorId();

    Long getProposalId();

    Long getProposalSpecId();

    boolean isActive();

    String getStatus();

    Long getSpecId();

    Long getSpecType();

    Double getNetApprovedQty();

    Double getNetApprovedPrice();

    String getLot();
}
