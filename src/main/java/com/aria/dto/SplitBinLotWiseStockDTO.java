package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SplitBinLotWiseStockDTO {

    private Long binNo;
    private Long productId;
    private Double qty;
    private Double lot;
    private String batchNo;
    private String productName;
    private String type;

    public SplitBinLotWiseStockDTO(SplitBinLotWiseStock lotWiseStock) {
        this.binNo = lotWiseStock.getBinNo();
        this.productId = lotWiseStock.getProductId();
        this.qty = lotWiseStock.getQty();
        this.lot = lotWiseStock.getLot();
        this.batchNo = lotWiseStock.getBatchNo();
        this.productName = lotWiseStock.getProductName();
        this.type = lotWiseStock.getType();
    }
}
