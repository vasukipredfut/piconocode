package com.aria.dto;

import com.aria.model.BillInward;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class HoForwardDto {

    private Long factBillId;
    private Long hoBillId;
    private String billNo;
    private String billDate;
    private String billAmt;
    private String arraivalDate;
    private String lorrySerialNo;
    private String ewayBillNo;
    private Long frmId;
    private Long plantId;
    private String plantName;
    private Double invQty;
    private String lorryNo;
    private String lorryWeight;
    private String lorryWithWeight;
    private String lorryWithoutWeight;
    private Boolean lorry_freight;
    private double weightShortage;

    private String shexType;

    private String billInwardBy;


    List<HoBillReceiveItem> poRecItem;

    List<BillInwardLorryDto> lorryDetails;

    public HoForwardDto(BillInward billInward) {

        DateUtil dateUtil = new DateUtil();
        this.factBillId = billInward.getId();
        this.hoBillId = billInward.getHoBillId();
        this.billNo = billInward.getBillNo();
        this.billDate = dateUtil.getDateTOStringWk(billInward.getBillDate());

        this.arraivalDate = dateUtil.getDateTOStringWk(billInward.getReceivedDate());
        this.lorrySerialNo = billInward.getLorrySerialNo();
        this.ewayBillNo = billInward.getEwayBill();
        if(billInward.getTotBags() != null) {
            this.invQty = billInward.getTotBags();
        }else {
            this.invQty = 0.0;
        }
        if(billInward.getDeliverBranch() != null) {
            this.plantId = billInward.getDeliverBranch().getId();
            this.plantName = billInward.getDeliverBranch().getBranchName();
        }

        this.lorryWeight = "0.0";
        this.lorryNo = billInward.getLorryno().toUpperCase();

        this.lorryWithWeight = billInward.getLorry_with_Weight();
        this.lorryWithoutWeight = billInward.getLorry_without_Weight();
        this.lorry_freight = billInward.getLorry_freight();

        this.weightShortage = 0.0;



        this.lorryDetails = billInward.getBillInwardLorryDetails() != null
                ? billInward.getBillInwardLorryDetails().stream().map(field -> new BillInwardLorryDto(field)).collect(Collectors.toList()) : null;

    }
}
