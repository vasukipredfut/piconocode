package com.aria.dto;


import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.CreditNoteBill;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreditNoteBillDto {

	private Long id;

	private String creditNoteNumber;

	private Long vendorId;

	private Long creditCategory;

	private Double creditAmount;

	private String creditBillDate;

	private Long inwardCompany;

	private Long inwardBranch;

	private String desc;

	private String inwardBillNumber;

	private Double billInwardQuantity;

	private Double creditNoteQuantity;

	private String billInwardDate;

	private Long billInwardBy;

	private String billInwardTime;

	private String billInwardStatus;

	private Long stockInwardBy;

	private String stockInwardTime;

	private String stockInwardStatus;

	private Long billVerifiedBy;

	private String billVerifiedTime;

	private String billVerifiedStatus;

	private String billApprovedTime;

	private String billApprovedStatus;

	private Long billApprovedBy;

	private String billAuthorizedTime;

	private String billAuthorizedStatus;

	private Long billAuthorizedBy;

	private String roundOff;

	private int activeFlag;

	private Double tcsPercentage;

	private Double tcsAmount;

	private Long billCount;

	private String type;

	private String status;

	private Long createdBy;
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

	private String billDate;

	List<CreditNoteItemsDto> creditNoteItems;

	public CreditNoteBillDto(CreditNoteBill creditNoteBillDto) {
		DateUtil dateUtil = new DateUtil();
		this.id = creditNoteBillDto.getId();
		this.creditNoteNumber = creditNoteBillDto.getCreditNoteNumber();
		this.vendorId = creditNoteBillDto.getVendorId();
		this.creditCategory = creditNoteBillDto.getCreditCategory();
		this.creditAmount = creditNoteBillDto.getCreditAmount();
		this.creditBillDate = dateUtil.getDateTOStringWk(creditNoteBillDto.getCreditBillDate());
		this.inwardCompany = creditNoteBillDto.getInwardCompany();
		this.inwardBranch = creditNoteBillDto.getInwardBranch();
		this.desc = creditNoteBillDto.getDescription();
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

		if (creditNoteBillDto.getCreatedBy() != null) {
			this.createdBy = creditNoteBillDto.getCreatedBy().getId();
			this.createdByName = creditNoteBillDto.getCreatedBy().getEmployeename();
			this.createdDate = dateUtil.getLocalDateTimeToDateString(creditNoteBillDto.getCreated_date());
			this.createdDateTime =  dateUtil.getLocalDateTimeToDateTimeString(creditNoteBillDto.getCreated_date());
		}

		if (creditNoteBillDto.getApprovedBy() != null) {
			this.approvedBy = creditNoteBillDto.getApprovedBy().getId();
			this.approverName = creditNoteBillDto.getApprovedBy().getEmployeename();
			this.approvedDate = dateUtil.getLocalDateTimeToDateString(creditNoteBillDto.getApproved_date());
			this.approvedDateTime =  dateUtil.getLocalDateTimeToDateTimeString(creditNoteBillDto.getApproved_date());
		}

		if (creditNoteBillDto.getAuthorizedBy() != null) {
			this.authorizedBy = creditNoteBillDto.getAuthorizedBy().getId();
			this.authorizerName = creditNoteBillDto.getAuthorizedBy().getEmployeename();
			this.authorizedDate = dateUtil.getLocalDateTimeToDateString(creditNoteBillDto.getAuthorized_date());
			this.authorizedDateTime =  dateUtil.getLocalDateTimeToDateTimeString(creditNoteBillDto.getAuthorized_date());
		}

		if (creditNoteBillDto.getVerifiedBy() != null) {
			this.verifiedBy = creditNoteBillDto.getVerifiedBy().getId();
			this.verifierName = creditNoteBillDto.getVerifiedBy().getEmployeename();
			this.verifiedDate = dateUtil.getLocalDateTimeToDateString(creditNoteBillDto.getVerified_date());
			this.verifiedDateTime =  dateUtil.getLocalDateTimeToDateTimeString(creditNoteBillDto.getVerified_date());
		}
		this.creditNoteItems = creditNoteBillDto.getCreditNoteItems() != null ? creditNoteBillDto.getCreditNoteItems()
				.stream().map(field -> new CreditNoteItemsDto(field)).collect(Collectors.toList()) : null;

	}
}
