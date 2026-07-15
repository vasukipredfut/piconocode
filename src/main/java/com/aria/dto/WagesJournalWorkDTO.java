package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WagesJournalWorkDTO {

    private Long productId;
    private String productName;
    private Long batchId;
    private String batchNo;
    private String type;
    private Double qty;
    private Double lot;
    private String trnNo;
    private String totalInputQty;
    private String totalOutputQty;

    private Double finishedQty;

    public WagesJournalWorkDTO(WagesJournalWork wagesJournalWork) {
        this.productId = wagesJournalWork.getProductId();
        this.productName = wagesJournalWork.getProductName();
        this.batchId = wagesJournalWork.getBatchId();
        this.batchNo = wagesJournalWork.getBatchNo();
        this.type = wagesJournalWork.getType();
        this.qty = wagesJournalWork.getQty();
        this.lot = wagesJournalWork.getLot();
        this.trnNo = wagesJournalWork.getTrnNo();
        this.totalInputQty = wagesJournalWork.getTotalInputQty();
        this.totalOutputQty = wagesJournalWork.getTotalOutputQty();
    }
}
