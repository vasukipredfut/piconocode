package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VendorSpecListDTO {

    private Long vendorId;
    private Long proposalId;
    private Long proposalSpecId;
    private boolean active;
    private String status;
    private Long specId;
    private Long specType;
    private Double netApprovedQty;
    private Double netApprovedPrice;
    private String lot;
    private String poCreatedQty ;
    private SpecificationDto specificationDto;
    public VendorSpecListDTO( VendorSpecList vendorSpecList) {
        this.vendorId = vendorSpecList.getVendorId();
        this.proposalId = vendorSpecList.getProposalId();
        this.proposalSpecId = vendorSpecList.getProposalSpecId();
        this.active = vendorSpecList.isActive();
        this.status = vendorSpecList.getStatus();
        this.specId = vendorSpecList.getSpecId();
        this.specType = vendorSpecList.getSpecType();
        this.netApprovedQty = vendorSpecList.getNetApprovedQty();
        this.netApprovedPrice = vendorSpecList.getNetApprovedPrice();
        this.lot = vendorSpecList.getLot();
    }
}
