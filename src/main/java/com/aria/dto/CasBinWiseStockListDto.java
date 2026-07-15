package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CasBinWiseStockListDto {


    public Long binNo;
    public Double stkQty;
    public Double lot;
    public String blendingStockType;


    public CasBinWiseStockListDto(CasBinWiseStockList wiseStockList) {
        this.binNo = wiseStockList.getBinNo();
        this.stkQty = wiseStockList.getStkQty();
        this.lot = wiseStockList.getLot();

    }

    public CasBinWiseStockListDto(CasBinWiseStockList wiseStockList, String blendingType) {
        this.binNo = wiseStockList.getBinNo();
        this.stkQty = wiseStockList.getStkQty();
        this.lot = wiseStockList.getLot();
        this.blendingStockType = blendingType;

    }
}
