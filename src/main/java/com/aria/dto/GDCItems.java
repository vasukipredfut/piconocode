package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GDCItems {

    public Long itemId;
    public String itemName;
    public String allocatedQuantity;
    public String lot;
    public String batch;
    public String binName;

    public String hsnCode;
    public String unit;
    public Double amount;

    public GDCItems(GDCItemsList gdcItemsList) {
        this.itemId = gdcItemsList.getItemId();
        this.itemName = gdcItemsList.getItemName();
        this.allocatedQuantity = gdcItemsList.getAllocated_quantity();
        this.lot = gdcItemsList.getLot();
        this.batch = gdcItemsList.getBatch();
        this.binName = gdcItemsList.getBinName();
    }
}
