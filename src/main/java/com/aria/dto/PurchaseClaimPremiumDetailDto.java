package com.aria.dto;

import com.aria.model.PurchaseClaimPremiumDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseClaimPremiumDetailDto {

    private Long id;
    private String cp_id;
    private String grn_id;
    private String item_id;
    private String factor;
    private String expected;
    private String actual;
    private String uom;
    private String ea_difference;
    private String difference_type;
    private String created_by;
    private String created_on;
    private String rate;
    private String calculated_amount;
    private String entered_amount;
    private String shortage_per_bag;
    private String received_qty;
    private String spl_ffa;

    public PurchaseClaimPremiumDetailDto(PurchaseClaimPremiumDetail purchaseClaimPremiumDetail) {
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
