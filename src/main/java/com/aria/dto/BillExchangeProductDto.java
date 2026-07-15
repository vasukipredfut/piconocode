package com.aria.dto;


import com.aria.model.BillExchangeProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BillExchangeProductDto {



    private Long id;

    private Long exchangeId;

    public Long productId;

    public String productName;

    private String batchNo;

    private Long batchId;

    private String lot;

    private String lorrySerialNo;

    private String quantity;

    private Long itemId;
    
    private Long itemType;
    
    private String itemName;

    private String poNumber;
    private Long poId;

    public BillExchangeProductDto(BillExchangeProduct billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.exchangeId = billInwardProduct.getExchangeId();
        this.itemId=billInwardProduct.getItemId();
        this.itemType=billInwardProduct.getItemType();
        this.itemName=billInwardProduct.getItemName();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
        this.poId = billInwardProduct.getPo_id();
        this.poNumber = billInwardProduct.getPo_number();

    }
}
