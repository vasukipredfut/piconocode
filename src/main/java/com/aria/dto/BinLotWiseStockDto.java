package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class BinLotWiseStockDto {

    private Long binNo;
    private Long productId;
    private Double qty;
    private Double lot;

    public BinLotWiseStockDto(BinLotWiseStock binLotWiseStock) {
        this.binNo = binLotWiseStock.getBinNo();
        this.productId = binLotWiseStock.getProductId();
        this.qty = binLotWiseStock.getQty();
        this.lot =binLotWiseStock.getLot();
    }


}
