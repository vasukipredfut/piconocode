package com.aria.model;

import com.aria.dto.PurchaseClaimPremiumDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ho_purchase_claim_premium")
public class PurchaseClaimPremium {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long cp_id;

    @Column
    private Date lorry_date;

    @Column
    private String lorry_slno;

    @Column
    private String claim;

    @Column
    private String premium;

    @Column
    private Long branch_id;

    @Column
    private Long item_group_id;

    @Column
    private String factor;

    @Column
    private String cgst;

    @Column
    private String sgst;

    @Column
    private String igst;

    @Column
    private String tax_percent;

    @Column
    private String trf_by;

    @Column
    private String trf_on;

    @Column
    private String approved_by;

    @Column
    private String approved_on;

    @Column
    private String remarks;

    @Column
    private String status;

    @Column
    private String ref_no;

    @Column
    private Date ref_date;

    @Column
    private String rounding;

    @Column
    private String net_amount;


    public PurchaseClaimPremium(PurchaseClaimPremiumDto purchaseClaimPremium) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseClaimPremium.getId();
        this.cp_id = purchaseClaimPremium.getCp_id();
        if (purchaseClaimPremium.getLorry_date() != null) {
            this.lorry_date = dateUtil.getStringTODateDDMMYYYY(purchaseClaimPremium.getLorry_date());
        }
        this.lorry_slno = purchaseClaimPremium.getLorry_slno();
        this.claim = purchaseClaimPremium.getClaim();
        this.premium = purchaseClaimPremium.getPremium();
        this.branch_id = purchaseClaimPremium.getBranch_id();
        this.item_group_id = purchaseClaimPremium.getItem_group_id();
        this.factor = purchaseClaimPremium.getFactor();
        this.cgst = purchaseClaimPremium.getCgst();
        this.sgst = purchaseClaimPremium.getSgst();
        this.igst = purchaseClaimPremium.getIgst();
        this.tax_percent = purchaseClaimPremium.getTax_percent();
        this.trf_by = purchaseClaimPremium.getTrf_by();
        this.trf_on = purchaseClaimPremium.getTrf_on();
        this.approved_by = purchaseClaimPremium.getApproved_by();
        this.approved_on = purchaseClaimPremium.getApproved_on();
        this.remarks = purchaseClaimPremium.getRemarks();
        this.status = purchaseClaimPremium.getStatus();
        this.ref_no = purchaseClaimPremium.getRef_no();
        if (purchaseClaimPremium.getRef_date() != null) {
            this.ref_date = dateUtil.getStringTODateDDMMYYYY(purchaseClaimPremium.getRef_date());
        }
        this.rounding = purchaseClaimPremium.getRounding();
        this.net_amount = purchaseClaimPremium.getNet_amount();
    }
}
