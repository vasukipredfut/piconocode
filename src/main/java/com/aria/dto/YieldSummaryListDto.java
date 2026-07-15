package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class YieldSummaryListDto {

    public String batchNo;
    public Double qty;
    public Long itemId;

    public YieldSummaryListDto( YieldSummaryList yieldSummaryList) {
        this.batchNo = yieldSummaryList.getBatchNo();
        this.qty = yieldSummaryList.getQty();
        this.itemId = yieldSummaryList.getItemId();
    }
}
