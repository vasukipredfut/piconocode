package com.aria.dto;

public class BlendingBatchBinDTO {

    public String batchNo;
    public Long productId;
    public Long binNo;
    public Double lot;
    public Double qty;

    public BlendingBatchBinDTO(BlendingBatchBin batchBin) {
        this.batchNo = batchBin.getBatchNo();
        this.productId = batchBin.getProductId();
        this.binNo = batchBin.getBinNo();
        this.lot = batchBin.getLot();
        this.qty = batchBin.getQty();
    }
}
