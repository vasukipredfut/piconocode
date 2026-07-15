package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitReportClosingSummaryDto {


    public Long itemId;

    public String itemName;
    public Double crushing_issue_Qty;
    public Double crushing_receive_Qty;
    public Double rotary_issue_Qty;
    public Double rotary_receive_Qty;


}
