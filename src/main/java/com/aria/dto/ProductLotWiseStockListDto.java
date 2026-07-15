package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductLotWiseStockListDto {

    private Double lot;
    private Long productId;

    public ProductLotWiseStockListDto(ProductLotWiseStockList stockList) {
        this.lot = stockList.getLot();
        this.productId = stockList.getProductId();
    }
}
