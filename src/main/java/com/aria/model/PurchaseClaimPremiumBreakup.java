package com.aria.model;

import com.aria.dto.PurchaseClaimPremiumBreakupDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ho_purchase_claim_premium_breakup")
public class PurchaseClaimPremiumBreakup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String cpb_id;

    @Column
    private String cpd_id;

    @Column
    private String qlty_id;

    @Column
    private String factor;

    @Column
    private String expected;

    @Column
    private String actual;

    @Column
    private String qty;

    @Column
    private String rate;

    @Column
    private String type;


    public PurchaseClaimPremiumBreakup(PurchaseClaimPremiumBreakupDto purchaseClaimPremiumBreakup) {
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
