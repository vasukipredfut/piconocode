package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinWiseStockListDto {

    private Long binNo;
    private String binId;
    private Double qty;
    private Double cost;
    private Double avgCost;
    private Double lot;
    private Long productId;
    private String productName;
    private String productCategory;

    public BinWiseStockListDto(BinWiseStockList binWiseStockList) {
        this.binNo = binWiseStockList.getBinNo();
        this.binId = binWiseStockList.getBinId();
        this.qty = binWiseStockList.getQty();
        this.cost = binWiseStockList.getCost();
        this.avgCost = binWiseStockList.getAvgCost();
        this.productId = binWiseStockList.getProductId();
        this.productName = binWiseStockList.getProductName();
        this.productCategory = binWiseStockList.getProductCategory();
        this.lot = binWiseStockList.getLot();
    }
}
