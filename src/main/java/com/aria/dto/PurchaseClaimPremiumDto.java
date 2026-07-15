package com.aria.dto;


import com.aria.model.PurchaseClaimPremium;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseClaimPremiumDto {


    private Long id;

    private Long cp_id;
    private String lorry_date;
    private String lorry_slno;
    private String claim;
    private String premium;
    private Long branch_id;
    private Long item_group_id;
    private String factor;
    private String cgst;
    private String sgst;
    private String igst;
    private String tax_percent;
    private String trf_by;
    private String trf_on;
    private String approved_by;
    private String approved_on;
    private String remarks;
    private String status;
    private String ref_no;
    private String ref_date;
    private String rounding;
    private String net_amount;

    public PurchaseClaimPremiumDto(PurchaseClaimPremium purchaseClaimPremium) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseClaimPremium.getId();
        this.cp_id = purchaseClaimPremium.getCp_id();
        if (purchaseClaimPremium.getLorry_date() != null) {
            this.lorry_date = dateUtil.getDateTOStringDDMMMYYYY(purchaseClaimPremium.getLorry_date());
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
            this.ref_date = dateUtil.getDateTOStringDDMMMYYYY(purchaseClaimPremium.getRef_date());
        }
        this.rounding = purchaseClaimPremium.getRounding();
        this.net_amount = purchaseClaimPremium.getNet_amount();
    }
}
