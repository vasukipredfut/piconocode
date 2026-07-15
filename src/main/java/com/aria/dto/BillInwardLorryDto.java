package com.aria.dto;

import com.aria.model.BillInwardLorryDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillInwardLorryDto {

    private Long id;

    private Long inwardId;

    private String lorryNo;

    private String payableFreightAmount;

    private String panNo;

    private String payeeName;

    private String contactNumber;

    public BillInwardLorryDto(BillInwardLorryDetails lorryDetails) {
        this.id = lorryDetails.getId();
        this.inwardId = lorryDetails.getInwardId();
        this.lorryNo = lorryDetails.getLorryNo();
        this.payableFreightAmount = lorryDetails.getPayableFreightAmount();
        this.panNo = lorryDetails.getPanNo();
        this.payeeName = lorryDetails.getPayeeName();
        this.contactNumber = lorryDetails.getContactNumber();
    }
}
