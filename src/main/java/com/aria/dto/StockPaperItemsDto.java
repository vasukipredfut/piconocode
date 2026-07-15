package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StockPaperItemsDto {

    private String productName;
    private Long productId;
    private Double lot;

    public StockPaperItemsDto(StockPaperItems stockPaper) {
        this.productName = stockPaper.getProductName();
        this.productId = stockPaper.getProductId();
        this.lot = stockPaper.getLot();
    }
}
