package com.aria.dto;

import com.aria.model.MaterialWithinBranchReceiptStoredBinDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class WithInBranchRecieveBinDetailsDto {

    private Long id;

    private Long itemId;
    
    private Long withinItemId;

    private String binNo;

    private Long binId;

    private Double allottedQuantity;

    public WithInBranchRecieveBinDetailsDto(MaterialWithinBranchReceiptStoredBinDetails dpEntryStoredBinDetails) {
        this.id = dpEntryStoredBinDetails.getId();
        this.itemId = dpEntryStoredBinDetails.getItemId();
        this.binNo = dpEntryStoredBinDetails.getBinNo();
        this.binId = dpEntryStoredBinDetails.getBinId();
        this.allottedQuantity = dpEntryStoredBinDetails.getAllottedQuantity();
        this.withinItemId = dpEntryStoredBinDetails.getWithinItemId();
    }
}
