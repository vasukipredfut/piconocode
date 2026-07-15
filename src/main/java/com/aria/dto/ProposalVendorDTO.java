package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProposalVendorDTO {

    private Long vendorId;
    private Long proposalId;
    private String vendorName;
    private String poValidityRequest;
    private String expiryDate;

    List<VendorSpecListDTO> vendorProposalSpec;

    public ProposalVendorDTO(ProposalVendorList proposalVendorList) {
        DateUtil dateUtil = new DateUtil();
        this.vendorId = proposalVendorList.getVendorId();
        this.proposalId = proposalVendorList.getProposalId();
        this.vendorName = proposalVendorList.getVendorName();
        this.poValidityRequest = proposalVendorList.getPoValidityRequest();
        this.vendorName = proposalVendorList.getVendorName();
        this.expiryDate = dateUtil.getDateTOStringWk(proposalVendorList.getExpiryDate());
    }
}
