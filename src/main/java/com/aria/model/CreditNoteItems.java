package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.CreditNoteItemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "creditnote_items")
public class CreditNoteItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bill_number")
	private String billNumber;

	@Column(name = "party_id")
	private Long partyId;

	@Column(name = "ref_id")
	private Long refId;
	@Column(name = "credit_quantity")
	private Double creditQuantity;

	@Column(name = "unit_rate")
	private Double unitRate;

	@Column(name = "cgst_percentage")
	private Double cgstPercentage;

	@Column(name = "cgst_amount")
	private Double cgstAmount;

	@Column(name = "sgst_percentage")
	private Double sgstPercentage;

	@Column(name = "sgst_pmount")
	private Double sgstAmount;

	@Column(name = "igst_percentage")
	private Double igstPercentage;

	@Column(name = "igst_amount")
	private Double igstAmount;

	@Column(name = "line_total")
	private Double lineTotal;

	@Column(name = "stock_inward_by")
	private Long stockInwardBy;

	@Column(name = "lot")
	private Double lot;

	@Column(name = "active_flag")
	private int activeFlag;

	@Column(name = "credit_note_id")
	private Long creditNoteId;
    @Column(name = "round_Off")
	private String roundOff;



	public CreditNoteItems(CreditNoteItemsDto creditNoteItems) {
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
	}
}
