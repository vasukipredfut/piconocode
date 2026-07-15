package com.aria.dto;

import com.aria.model.CreditNoteItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreditNoteItemsListDTO {


    private Long id;

    private String billNumber;

    private Long partyId;

    private Long refId;

    private Double creditQuantity;

    private Double unitRate;

    private String itemName;

    private Double cgstPercentage;

    private Double cgstAmount;

    private Double sgstPercentage;

    private Double sgstAmount;

    private Double igstPercentage;

    private Double igstAmount;

    private Double lineTotal;

    private Long stockInwardBy;

    private Double lot;

    private int activeFlag;

    private Long creditNoteId;

    private String roundOff;

    private String createdByName;
    private String createdDate;
    private String createdDateTime;

    private Long verifiedBy;

    private String verifierName;
    private String verifiedDate;
    private String verifiedDateTime;
    private Long authorizedBy;

    private String authorizerName;
    private String authorizedDate;
    private String authorizedDateTime;
    private Long approvedBy;

    private String approverName;
    private String approvedDate;
    private String approvedDateTime;

    public CreditNoteItemsListDTO(CreditNoteItems creditNoteItems, CreditNoteBillDto creditNoteBill) {


        this.id = creditNoteItems.getId();
        this.billNumber = creditNoteItems.getBillNumber();
        this.partyId = creditNoteItems.getPartyId();
        this.refId = creditNoteItems.getRefId();
        this.creditQuantity = creditNoteItems.getCreditQuantity();
        this.unitRate = creditNoteItems.getUnitRate();
        this.cgstPercentage = creditNoteItems.getCgstPercentage();
        this.cgstAmount = creditNoteItems.getCgstAmount();
        this.sgstPercentage = creditNoteItems.getSgstPercentage();
        this.sgstAmount = creditNoteItems.getSgstAmount();
        this.igstPercentage = creditNoteItems.getIgstPercentage();
        this.igstAmount = creditNoteItems.getIgstAmount();
        this.lineTotal = creditNoteItems.getLineTotal();
        this.stockInwardBy = creditNoteItems.getStockInwardBy();
        this.lot = creditNoteItems.getLot();
        this.activeFlag = creditNoteItems.getActiveFlag();
        this.creditNoteId = creditNoteItems.getCreditNoteId();
        this.roundOff = creditNoteItems.getRoundOff();

        this.createdByName = creditNoteBill.getCreatedByName();
        this.createdDate = creditNoteBill.getCreatedByName();
        this.createdDateTime = creditNoteBill.getCreatedByName();

        this.approverName = creditNoteBill.getApproverName();
        this.approvedDate = creditNoteBill.getApprovedDate();
        this.approvedDateTime = creditNoteBill.getApprovedDateTime();

        this.verifierName = creditNoteBill.getVerifierName();
        this.verifiedDate = creditNoteBill.getVerifiedDate();
        this.verifiedDateTime = creditNoteBill.getVerifiedDateTime();

        this.authorizerName = creditNoteBill.getAuthorizerName();
        this.authorizedDate = creditNoteBill.getAuthorizedDate();
        this.authorizedDateTime = creditNoteBill.getAuthorizedDateTime();

    }


}
