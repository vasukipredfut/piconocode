package com.aria.dto;


import com.aria.model.BillForwardProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillForwardProductDto {

    private Long id;

    private Long forwardId;

    public Long itemId;
    
    private Long itemType;
    
    private String itemName;

    private String batchNo;

    private Long batchId;

    private String lot;

   // private String lorrySerialNo;

    private String quantity;
    private String poNumber;
    private Long poId;
    private Long billRefId;

    public BillForwardProductDto(BillForwardProduct billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.forwardId = billInwardProduct.getForwardId();
        this.itemId=billInwardProduct.getItemId();
        this.itemName=billInwardProduct.getItemName();
        this.itemType=billInwardProduct.getItemType();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
        this.poId = billInwardProduct.getPo_id();
        this.poNumber = billInwardProduct.getPo_number();
        this.billRefId = billInwardProduct.getBill_ref_id();

    }
}
