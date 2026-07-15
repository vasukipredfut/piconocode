package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StockPaperIssueReceipts {

    private Double qty;
    private String entryType;
    private String trnType;

    public StockPaperIssueReceipts(StockPaper stockPaper) {
        this.qty = stockPaper.getQty();
        this.entryType = stockPaper.getEntryType();
        this.trnType = stockPaper.getTrnType();
    }
}
