package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitReportClosingOilDto {

    public Long itemId;

    public String itemName;
    public Double openingQty;
    public Double closingQty;
    public Double crushingQty;
    public Double dryageQty;

    public Double receipt_from_packing_qty;
    public Double receipt_from_other_plant_qty;
    public Double receipt_from_crushing_qty;
    public Double receipt_from_rotary_qty;


    public Double issue_from_packing_qty;
    public Double issue_from_production;
    public Double issue_from_other_plant_qty;
    public Double issue_from_crushing_qty;
    public Double issue_to_packing_qty;
    public Double issue_to_dispatch_qty;
}
