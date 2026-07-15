package com.aria.dto;

import com.aria.model.SalesReturnBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SalesReturnBinDetailsDto {

    private Long id;

    private Long returnProductId;

    private Long itemId;

    private String itemName;

    private Long binNo;

    private String allocatedQty;

    private String accountingType;

    public SalesReturnBinDetailsDto(SalesReturnBinDetails returnBinDetails) {
        this.id = returnBinDetails.getId();
        this.returnProductId = returnBinDetails.getReturnProductId();
        this.itemId = returnBinDetails.getItemId();
        this.itemName = returnBinDetails.getItemName();
        this.binNo = returnBinDetails.getBinNo();
        this.allocatedQty = returnBinDetails.getAllocatedQty();
        this.accountingType = returnBinDetails.getAccountingType();
    }
}
