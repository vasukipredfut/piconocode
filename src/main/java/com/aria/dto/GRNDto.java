package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GRNDto {

    private Long id;
    private Long hoBillId;
    private Long vendorId;
    private String vendorName;
    private Long firmOrgId;
    private String firmOrgName;
    private String billNo;
    private String billDate;
    private double billAmount;
    private Long deliverBranchId;
    private String deliverBranchName;
    private String ewayBill;
    private String productCategory;
    private String camp;
    private String cessTax;
    private double insurance;
    private double cgst;
    private double igst;
    private double sgst;
    private double cgstper;
    private double igstper;
    private double sgstper;
    private String tcs;
    private double tcsper;
    private double lorryfreight;
    private double dedLorryfreight;
    private double otherAmount;
    private double dedOtherAmount;
    private double roundOff;
    private double dedRoundOff;
    private double totBags;
    private String perBagCost;
    private String purpose;
    private String lorryNo;
    private boolean lorry_freight;
    private String payableFreightAmount;

    private List<GRNProducts> itemReceives;

    private List<BillInwardLorryDto> lorryDetails;



}
