package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RawMaterialMovementQtyWiseSummaryDto {

    public String trnNo;

    public String billNo;
    public String lorrySerialNo;
    public String batchNo;
    public Double qty;

    List<RawMaterialMovementBinQtyDto> trnBinWiseQty;

    public RawMaterialMovementQtyWiseSummaryDto(RawMaterialMovementQtyWiseSummary qtyWiseSummary) {
        this.billNo = qtyWiseSummary.getBillNo();
        this.lorrySerialNo = qtyWiseSummary.getLorrySerialNo();
        this.batchNo = qtyWiseSummary.getBatchNo();
        this.qty = qtyWiseSummary.getQty();
        this.trnNo = qtyWiseSummary.getTrnNo();
    }
}
