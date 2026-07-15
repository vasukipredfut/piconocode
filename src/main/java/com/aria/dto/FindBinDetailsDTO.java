package com.aria.dto;

import com.aria.model.FindBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindBinDetailsDTO {


    private String binId;

    private String binNo;

    private Double currentStock;

    private String binCapacity;

    private String capacityLeft;

    public FindBinDetailsDTO(FindBinDetails findBinDetails) {
        this.binId = findBinDetails.getBinId();
        this.binNo = findBinDetails.getBinNo();
        this.currentStock = findBinDetails.getCurrentStock();
        this.binCapacity = findBinDetails.getBinCapacity();
        this.capacityLeft = findBinDetails.getCapacityLeft();
    }
}
