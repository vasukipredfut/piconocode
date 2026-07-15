package com.aria.dto;

import com.aria.model.PurchaseClaimPremiumBreakup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseClaimPremiumBreakupDto {


    private Long id;
    private String cpb_id;
    private String cpd_id;
    private String qlty_id;
    private String factor;
    private String expected;
    private String actual;
    private String qty;
    private String rate;
    private String type;

    public PurchaseClaimPremiumBreakupDto(PurchaseClaimPremiumBreakup purchaseClaimPremiumBreakup) {
        this.id = purchaseClaimPremiumBreakup.getId();
        this.cpb_id = purchaseClaimPremiumBreakup.getCpb_id();
        this.cpd_id = purchaseClaimPremiumBreakup.getCpd_id();
        this.qlty_id = purchaseClaimPremiumBreakup.getQlty_id();
        this.factor = purchaseClaimPremiumBreakup.getFactor();
        this.expected = purchaseClaimPremiumBreakup.getExpected();
        this.actual = purchaseClaimPremiumBreakup.getActual();
        this.qty = purchaseClaimPremiumBreakup.getQty();
        this.rate = purchaseClaimPremiumBreakup.getRate();
        this.type = purchaseClaimPremiumBreakup.getType();
    }
}
