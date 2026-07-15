package com.aria.dto;

import com.aria.model.PurchaseBrokerageBreakup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseBrokerageBreakupDto {
    private Long id;
    private Long brokerage_id;
    private String conv_factor;
    private String calculated_bags;
    private String total_bags;
    private String brokerage_rate;
    private String brokerage_amt;
    private Long camp_id;
    private Long item_group_id;
    private String camp_name;
    private String round_off;

    public PurchaseBrokerageBreakupDto(PurchaseBrokerageBreakup purchaseBrokerageBreakup) {
        this.id = purchaseBrokerageBreakup.getId();
        this.brokerage_id = purchaseBrokerageBreakup.getBrokerage_id();
        this.conv_factor = purchaseBrokerageBreakup.getConv_factor();
        this.calculated_bags = purchaseBrokerageBreakup.getCalculated_bags();
        this.total_bags = purchaseBrokerageBreakup.getTotal_bags();
        this.brokerage_rate = purchaseBrokerageBreakup.getBrokerage_rate();
        this.brokerage_amt = purchaseBrokerageBreakup.getBrokerage_amt();
        this.camp_id = purchaseBrokerageBreakup.getCamp_id();
        this.item_group_id = purchaseBrokerageBreakup.getItem_group_id();
        this.camp_name = purchaseBrokerageBreakup.getCamp_name();
        this.round_off = purchaseBrokerageBreakup.getRound_off();
    }
}
