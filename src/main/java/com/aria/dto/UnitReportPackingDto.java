package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitReportPackingDto {

    public Long itemId;

    public String itemName;
    
    public String conversion;
    
    public Double qty;

    public UnitReportPackingDto(PackingList packingList) {
        this.itemId = packingList.getItemId();
        this.itemName = packingList.getItemName();
        this.qty = packingList.getQty();
       
    }
    public UnitReportPackingDto(ByeProductDtoList list ) {
        this.itemId = list.getItemId();
        this.itemName = list.getName();
        this.qty = list.getStockQty();
        this.conversion=list.getConversion();
       
    }
}
