package com.aria.dto;

import com.aria.model.DpEntryStoredBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
@Setter
@Getter
@NoArgsConstructor
public class DpEntryStoredBinDetailsDto {

    private Long id;

    private Long entryProductId;
  
    private String binNo;

    private Long binId;
    private Long dept;

    private Double allottedQuantity;

    public String conceptionStock;

    public DpEntryStoredBinDetailsDto(DpEntryStoredBinDetails dpEntryStoredBinDetails) {
        this.id = dpEntryStoredBinDetails.getId();
        this.entryProductId = dpEntryStoredBinDetails.getEntryProductId();
        this.binNo = dpEntryStoredBinDetails.getBinNo();
        this.dept = dpEntryStoredBinDetails.getDept();
        this.binId = dpEntryStoredBinDetails.getBinId();
        this.allottedQuantity = dpEntryStoredBinDetails.getAllottedQuantity();
    }
}
