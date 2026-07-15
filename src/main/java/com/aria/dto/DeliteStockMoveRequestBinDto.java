package com.aria.dto;

import com.aria.model.DeliteStockMoveRequestBin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliteStockMoveRequestBinDto {

    private Long id;

    private Long deliteStockMoveRequestId;

    private Long binstockId;

    private Long batchId;

    private String batchNo;

    private Long casId;

    private Long binNo;

    private Double quantity;

    private Long refId;

    private String status;

    private Double lot;

    public DeliteStockMoveRequestBinDto(DeliteStockMoveRequestBin deliteStockMoveRequestBin) {
        this.id = deliteStockMoveRequestBin.getId();
        this.deliteStockMoveRequestId = deliteStockMoveRequestBin.getDelitestockMoverequestId();
        this.binstockId = deliteStockMoveRequestBin.getBinstockId();
        this.batchId = deliteStockMoveRequestBin.getBatchId();
        this.batchNo = deliteStockMoveRequestBin.getBatchNo();
        this.casId = deliteStockMoveRequestBin.getCasId();
        this.binNo = deliteStockMoveRequestBin.getBinNo();
        this.quantity = deliteStockMoveRequestBin.getQuantity();
        this.refId = deliteStockMoveRequestBin.getRefId();
        this.status = deliteStockMoveRequestBin.getStatus();
        this.lot = deliteStockMoveRequestBin.getLot();
    }
}
