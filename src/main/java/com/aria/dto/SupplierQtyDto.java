package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierQtyDto {

    private Long poId;
    private String poNo;
    private String poDate;
    private Long itemId;
    private String itemName;
    private String lot;
    private Double orderQty;
    private Double pendingQty;
    private Double unitRate;
    public String typeFlag;


    public SupplierQtyDto(SupplierQty supplierQty) {
        this.poId = supplierQty.getPoId();
        this.poNo = supplierQty.getPoNo();
        this.poDate = supplierQty.getPoDate();
        this.itemId = supplierQty.getItemId();
        this.itemName = supplierQty.getItemName();
        this.lot = supplierQty.getLot();
        this.orderQty = supplierQty.getOrderQty();
        this.unitRate = supplierQty.getUnitRate();
        this.typeFlag = supplierQty.getTypeFlag();
    }
}
