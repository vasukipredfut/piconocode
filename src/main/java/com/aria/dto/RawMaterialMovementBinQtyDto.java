package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RawMaterialMovementBinQtyDto {

    public String binId;
    public String batchNo;
    public Long binName;
    public String godownName;
    public Double qty;

    public RawMaterialMovementBinQtyDto(RawMaterialMovementQtyWiseSummary qtyWiseSummary) {
        this.batchNo = qtyWiseSummary.getBatchNo();
        this.binId = qtyWiseSummary.getBinId();
        this.binName = qtyWiseSummary.getBinName();
        this.godownName = qtyWiseSummary.getGodownName();
        this.qty = qtyWiseSummary.getQty();
    }
}
