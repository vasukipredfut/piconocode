package com.aria.dto;


import com.aria.model.BillReceiveProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillReceiveProductDto {

    private Long id;

    private Long receiveId;

    public Long productId;

    public String productName;

    private String batchNo;

    private String lot;

    private String lorrySerialNo;

    private String quantity;


    public BillReceiveProductDto(BillReceiveProduct billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.receiveId = billInwardProduct.getReceiveId();
        if (billInwardProduct.getProduct() != null) {
            this.productId = billInwardProduct.getProduct().getId();
            this.productName = billInwardProduct.getProduct().getProductName();
        }
        this.batchNo = billInwardProduct.getBatchNo();
        this.lot = billInwardProduct.getLot();
        this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();

    }
}
