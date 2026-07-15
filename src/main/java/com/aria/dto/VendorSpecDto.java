package com.aria.dto;

import com.aria.model.ProposalVendorSpec;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VendorSpecDto {


    private Long proposalId;
    private Long vendorId;
    private String expiryDate;
    private String poValidityRequest;

    List<VendorProposalSpecDto> vendorProposalSpec;


}
