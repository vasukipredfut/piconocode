package com.aria.model;

import com.aria.dto.PurchaseBrokerageBreakupDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ho_purchase_brokerage_breakup")
public class PurchaseBrokerageBreakup {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long brokerage_id;

    @Column
    private String conv_factor;

    @Column
    private String calculated_bags;

    @Column
    private String total_bags;

    @Column
    private String brokerage_rate;

    @Column
    private String brokerage_amt;

    @Column
    private Long camp_id;

    @Column
    private Long item_group_id;

    @Column
    private String camp_name;

    @Column
    private String round_off;


    public PurchaseBrokerageBreakup(PurchaseBrokerageBreakupDto purchaseBrokerageBreakup) {
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
