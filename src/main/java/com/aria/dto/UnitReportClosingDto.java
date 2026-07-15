package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitReportClosingDto {


    public Long itemId;

    public String itemName;
    public Double openingQty;
    public Double closingQty;
    public Double crushingQty;

    public Double receipt_party_qty;
    public Double receipt_other_plant_qty;
    public Double receipt_qc_qty;
    public Double receipt_excess_qty;
    public Double shortage_qty;
    public Double  receipt_rotary_qty;

    public Double issue_return_qty;
    public Double issue_other_plant_qty;
    public Double issue_qc_qty;
    public Double  issue_rotary_qty;
    public Double  issue_sales_qty;







}
