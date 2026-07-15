package com.aria.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class VendorProposalSpecDto {


    private Long proposalId;
    private Long proposalSpecId;
    private Double netApprovedQty;
    private Double netApprovedPrice;
    private String lot ;


}
