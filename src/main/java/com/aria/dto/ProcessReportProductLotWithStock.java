package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProcessReportProductLotWithStock {

    public Double lot;
    public Double qty;
    public Long branch;

    public ProcessReportProductLotWithStock(ProcessReportProductLotWithStockList stockList) {
        this.lot = stockList.getLot();
        this.qty = stockList.getQty();
        this.branch=stockList.getBranch();
    }
}
