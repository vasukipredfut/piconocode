package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class YieldSummaryDto {



    private Long itemId;
    private String itemName;
    private Double todayProduction;
    private String todayYield;
    private String yesterdayClosing;
    private String consumption;


    public YieldSummaryDto(YieldSummary yieldSummary) {
        this.itemId = yieldSummary.getItemId();
        this.itemName = yieldSummary.getItemName();
        this.todayProduction = yieldSummary.getTodayProduction();
        //this.yesterdayClosing = yieldSummary.getYesterdayClosing();
    }
}
