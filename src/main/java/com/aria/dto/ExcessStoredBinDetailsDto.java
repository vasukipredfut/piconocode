package com.aria.dto;

import com.aria.model.ExcessStoredBinDetails;
import com.aria.model.PurchaseReturnStoredBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExcessStoredBinDetailsDto {

    private Long id;

    private Long excessOrReturnProductId;

    private Long itemId;

    private String itemName;

    private Long binNo;

    private String batchNo;

    private Double currentStock;

    private Double excessOrShortage;

    public ExcessStoredBinDetailsDto(ExcessStoredBinDetails binDetails) {
        this.id = binDetails.getId();
        this.excessOrReturnProductId = binDetails.getExcessProductId();
        this.itemId = binDetails.getItemId();
        this.itemName = binDetails.getItemName();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.currentStock = binDetails.getCurrentStock();
        this.excessOrShortage = binDetails.getExcessOrShortage();
    }

    public ExcessStoredBinDetailsDto(PurchaseReturnStoredBinDetails binDetails) {
        this.id = binDetails.getId();
        this.excessOrReturnProductId = binDetails.getReturnProductId();
        this.itemId = binDetails.getItemId();
        this.itemName = binDetails.getItemName();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.currentStock = binDetails.getCurrentStock();
        this.excessOrShortage = binDetails.getExcessOrShortage();
    }
}
