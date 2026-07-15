package com.aria.model;

import com.aria.dto.DeliteStockMoveRequestBinDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "delitestock_moverequest_bin")
public class DeliteStockMoveRequestBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delitestock_moverequest_id")
    private Long delitestockMoverequestId;

    @Column(name = "binstock_id")
    private Long binstockId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "cas_id")
    private Long casId;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "ref_id")
    private Long refId;

    @Column
    private String status;
    @Column
    private Double lot;


    public DeliteStockMoveRequestBin(DeliteStockMoveRequestBinDto deliteStockMoveRequestBin) {
        this.id = deliteStockMoveRequestBin.getId();
        this.delitestockMoverequestId = deliteStockMoveRequestBin.getDeliteStockMoveRequestId();
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
