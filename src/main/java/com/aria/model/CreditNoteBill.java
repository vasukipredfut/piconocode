package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.CreditNoteBillDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "credit_bill")
public class CreditNoteBill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "credit_note_number")
	private String creditNoteNumber;

	@Column(name = "vendor_id")
	private Long vendorId;

	@Column(name = "credit_category")
	private Long creditCategory;

	@Column(name = "credit_amount")
	private Double creditAmount;

	@Column(name = "credit_bill_date")
	private Date creditBillDate;

	@Column(name = "inward_company")
	private Long inwardCompany;

	@Column(name = "inward_branch")
	private Long inwardBranch;

	@Column(name = "description")
	private String description;

	@Column(name = "inward_bill_number")
	private String inwardBillNumber;

	@Column(name = "bill_inward_quantity")
	private Double billInwardQuantity;

	@Column(name = "credit_note_quantity")
	private Double creditNoteQuantity;

	@Column(name = "bill_inward_date")
	private String billInwardDate;

	@Column(name = "bill_inward_by")
	private Long billInwardBy;

	@Column(name = "bill_inward_time")
	private String billInwardTime;

	@Column(name = "bill_inward_status")
	private String billInwardStatus;

	@Column(name = "stock_inward_by")
	private Long stockInwardBy;

	@Column(name = "stock_inward_time")
	private String stockInwardTime;

	@Column(name = "stock_inward_status")
	private String stockInwardStatus;

	@Column(name = "bill_verified_by")
	private Long billVerifiedBy;

	@Column(name = "bill_Verified_Time")
	private String billVerifiedTime;

	@Column(name = "bill_verified_Status")
	private String billVerifiedStatus;

	@Column(name = "bill_approved_Time")
	private String billApprovedTime;

	@Column(name = "bill_approved_Status")
	private String billApprovedStatus;

	@Column(name = "bill_approved_By")
	private Long billApprovedBy;

	@Column(name = "bill_authorized_Time")
	private String billAuthorizedTime;

	@Column(name = "bill_authorized_status")
	private String billAuthorizedStatus;

	@Column(name = "bill_authorized_by")
	private Long billAuthorizedBy;

	@Column(name = "round_Off")
	private String roundOff;

	@Column(name = "active_flag")
	private int activeFlag;

	@Column(name = "tcs_percentage")
	private Double tcsPercentage;

	@Column(name = "tcs_amount")
	private Double tcsAmount;

	@Column(name = "billcount")
	private Long billCount;

	@Column(name = "type")
	private String type;

	@Column(name = "status")
	private String status;



	@OneToOne
	@JoinColumn(name = "created_by", referencedColumnName = "id")
	private User createdBy;

	@Column
	private LocalDateTime created_date;


	@OneToOne
	@JoinColumn(name = "verified_by", referencedColumnName = "id")
	private User verifiedBy;

	@Column
	private LocalDateTime verified_date;

	@OneToOne
	@JoinColumn(name = "approved_by", referencedColumnName = "id")
	private User approvedBy;

	@Column
	private LocalDateTime approved_date;
	@OneToOne
	@JoinColumn(name = "authorized_by", referencedColumnName = "id")
	private User authorizedBy;

	@Column
	private LocalDateTime authorized_date;

	@OneToMany(mappedBy = "creditNoteId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CreditNoteItems> creditNoteItems;

	public CreditNoteBill(CreditNoteBillDto creditNoteBillDto) {
		DateUtil dateUtil = new DateUtil();
		this.id = creditNoteBillDto.getId();
		this.creditNoteNumber = creditNoteBillDto.getCreditNoteNumber();
		this.vendorId = creditNoteBillDto.getVendorId();
		this.creditCategory = creditNoteBillDto.getCreditCategory();
		this.creditAmount = creditNoteBillDto.getCreditAmount();
		this.creditBillDate = dateUtil.getStringTODateWk(creditNoteBillDto.getCreditBillDate());
		this.inwardCompany = creditNoteBillDto.getInwardCompany();
		this.inwardBranch = creditNoteBillDto.getInwardBranch();
		this.description = creditNoteBillDto.getDesc();
		this.inwardBillNumber = creditNoteBillDto.getInwardBillNumber();
		this.billInwardQuantity = creditNoteBillDto.getBillInwardQuantity();
		this.creditNoteQuantity = creditNoteBillDto.getCreditNoteQuantity();
		this.billInwardDate = creditNoteBillDto.getBillInwardDate();
		this.billInwardBy = creditNoteBillDto.getBillInwardBy();
		this.billInwardTime = creditNoteBillDto.getBillInwardTime();
		this.billInwardStatus = creditNoteBillDto.getBillInwardStatus();
		this.stockInwardBy = creditNoteBillDto.getStockInwardBy();
		this.stockInwardTime = creditNoteBillDto.getStockInwardTime();
		this.stockInwardStatus = creditNoteBillDto.getStockInwardStatus();
		this.billVerifiedBy = creditNoteBillDto.getBillVerifiedBy();
		this.billVerifiedTime = creditNoteBillDto.getBillVerifiedTime();
		this.billVerifiedStatus = creditNoteBillDto.getBillVerifiedStatus();
		this.billApprovedTime = creditNoteBillDto.getBillApprovedTime();
		this.billApprovedStatus = creditNoteBillDto.getBillApprovedStatus();
		this.billApprovedBy = creditNoteBillDto.getBillApprovedBy();
		this.billAuthorizedTime = creditNoteBillDto.getBillAuthorizedTime();
		this.billAuthorizedStatus = creditNoteBillDto.getBillAuthorizedStatus();
		this.billAuthorizedBy = creditNoteBillDto.getBillAuthorizedBy();
		this.roundOff = creditNoteBillDto.getRoundOff();
		this.activeFlag = creditNoteBillDto.getActiveFlag();
		this.tcsPercentage = creditNoteBillDto.getTcsPercentage();
		this.tcsAmount = creditNoteBillDto.getTcsAmount();
		this.billCount = creditNoteBillDto.getBillCount();
		this.type = creditNoteBillDto.getType();
		this.status = creditNoteBillDto.getStatus();
	}
}
