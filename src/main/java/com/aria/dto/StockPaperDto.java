package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class StockPaperDto {


    private Long binName;
    private String binId;
    private String productName;
    private Long productId;
    private Double qty;
    private Double lot;

    List<StockPaperIssueReceipts> issueList;
    List<StockPaperIssueReceipts> receiptsList;



    public StockPaperDto(StockPaper stockPaper) {
        this.binName = stockPaper.getBinName();
        this.binId = stockPaper.getBinId();
        this.productName = stockPaper.getProductName();
        this.productId = stockPaper.getProductId();
        this.qty = stockPaper.getQty();
        this.lot = stockPaper.getLot();
    }
}
