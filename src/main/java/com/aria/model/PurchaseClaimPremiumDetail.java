package com.aria.model;

import com.aria.dto.PurchaseClaimPremiumDetailDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ho_purchase_claim_detail")
public class PurchaseClaimPremiumDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String cp_id;

    @Column
    private String grn_id;

    @Column
    private String item_id;

    @Column
    private String factor;

    @Column
    private String expected;

    @Column
    private String actual;

    @Column
    private String uom;

    @Column
    private String ea_difference;

    @Column
    private String difference_type;

    @Column
    private String created_by;

    @Column
    private String created_on;

    @Column
    private String rate;

    @Column
    private String calculated_amount;

    @Column
    private String entered_amount;

    @Column
    private String shortage_per_bag;

    @Column
    private String received_qty;

    @Column
    private String spl_ffa;


    public PurchaseClaimPremiumDetail(PurchaseClaimPremiumDetailDto purchaseClaimPremiumDetail) {
        this.id = purchaseClaimPremiumDetail.getId();
        this.cp_id = purchaseClaimPremiumDetail.getCp_id();
        this.grn_id = purchaseClaimPremiumDetail.getGrn_id();
        this.item_id = purchaseClaimPremiumDetail.getItem_id();
        this.factor = purchaseClaimPremiumDetail.getFactor();
        this.expected = purchaseClaimPremiumDetail.getExpected();
        this.actual = purchaseClaimPremiumDetail.getActual();
        this.uom = purchaseClaimPremiumDetail.getUom();
        this.ea_difference = purchaseClaimPremiumDetail.getEa_difference();
        this.difference_type = purchaseClaimPremiumDetail.getDifference_type();
        this.created_by = purchaseClaimPremiumDetail.getCreated_by();
        this.created_on = purchaseClaimPremiumDetail.getCreated_on();
        this.rate = purchaseClaimPremiumDetail.getRate();
        this.calculated_amount = purchaseClaimPremiumDetail.getCalculated_amount();
        this.entered_amount = purchaseClaimPremiumDetail.getEntered_amount();
        this.shortage_per_bag = purchaseClaimPremiumDetail.getShortage_per_bag();
        this.received_qty = purchaseClaimPremiumDetail.getReceived_qty();
        this.spl_ffa = purchaseClaimPremiumDetail.getSpl_ffa();
    }
}
