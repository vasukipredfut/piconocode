package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.AdvancePaymentForPurchaseOrder;
import com.aria.model.User;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class AdvancePaymentForPurchaseOrderDto {

	private Long purchase_advance_payment_id;

	private Integer activeFlag;

	private String acountNumber;

	private Double advanceAmount;
	private Double paidAmount;

	private Long advancePaymentCompanyId;

	private String advancePaymentDate;

	private Long createById;
	private String createdDate;

	private String createByName;

	private String poNumber;

	public String poDate;

	private Double po_amount;

	private LocalDateTime createdTimeStamp;

	private String ddfavourof;

	private String ifsc;

	private Long firmId;

	private String firmName;

	private Integer numberOfPay;

	private String paymentMode;

	private String paymentType;

	private Integer pinfo;

	private Long dept;

	private Long branch;

	private Long purchaseOrderRefId;

	private String purpose;

	private String quotationDate;

	private String quotationNumber;

	private Double percentage;

	private String status;

	private String tds;

	private String trnno;

	private Long vendorRefId;

	private String vendorName;

	private String vendorAddress;

	private String vendorCity;

	private Double pendingAmount;

	public Long paymentCategoryId;

	public String paymentCategoryName;

	private Long purchasePaymentId;

	private Long verifiedBy;
	private String verifiedDate;

	private String verifierName;

	private Long authorizedBy;

	private String authorizerName;
	private String authorizedDate;

	private Long approvedBy;

	private String approverName;
	private String approvedDate;

	private Long rejectedBy;

	private String rejecterName;
	private String rejectedDate;
	private String bankName;
	private String bankBranchName;

	private Long activateBy;
	private String activateByName;
	private String activateDate;


	public AdvancePaymentForPurchaseOrderDto(AdvancePaymentForPurchaseOrder model) {

		DateUtil dateUtil = new DateUtil();
		this.purchase_advance_payment_id = model.getPurchase_advance_payment_id();
		this.acountNumber = model.getAcountNumber();
		this.advanceAmount = model.getAdvanceAmount();
		this.advancePaymentCompanyId = model.getAdvancePaymentCompanyId();
		if (model.getAdvancePaymentDate() != null)
			this.advancePaymentDate = dateUtil.getDateToStringDDMMYYYY(model.getAdvancePaymentDate());
		this.createdTimeStamp = model.getCreatedTimeStamp();
		this.ddfavourof = model.getDdfavourof();
		this.ifsc = model.getIfsc();
		this.numberOfPay = model.getNumberOfPay();
		this.paymentMode = model.getPaymentMode();
		this.paymentType = model.getPaymentType();
		this.pinfo = model.getPinfo();
		this.purchaseOrderRefId = model.getPurchaseOrderRefId();
		this.percentage = model.getPercentage();
		this.purpose = model.getPurpose();
		this.po_amount = model.getPo_amount();
		this.pendingAmount = model.getPendingAmount();
		if (model.getQuotationDate() != null) {
			this.quotationDate = dateUtil.getDateToStringDDMMYYYY(model.getQuotationDate());
		}
		this.quotationNumber = model.getQuotationNumber();
		this.tds = model.getTds();
		this.status = model.getStatus();
		this.trnno = model.getTrnno();
		this.vendorAddress = model.getVendorAddress();
		this.vendorRefId = model.getVendorRefId();
		this.paymentCategoryId = model.getPaymentCategoryId();
		this.paymentCategoryName = model.getPaymentCategoryName();
		this.purchasePaymentId = model.getPurchasePaymentId();
		if (model.getApprovedBy() != null) {
			this.approvedBy = model.getApprovedBy().getId();
			this.approverName = model.getApprovedBy().getEmployeename();
			this.approvedDate = dateUtil.getDateTime(model.getVerified_date());
		}

		if (model.getAuthorizedBy() != null) {
			this.authorizedBy = model.getAuthorizedBy().getId();
			this.authorizerName = model.getAuthorizedBy().getEmployeename();
			this.authorizedDate = dateUtil.getDateTime(model.getVerified_date());
		}

		if (model.getVerifiedBy() != null) {
			this.verifiedBy = model.getVerifiedBy().getId();
			this.verifierName = model.getVerifiedBy().getEmployeename();
			this.verifiedDate = dateUtil.getDateTime(model.getVerified_date());
		}

		if (model.getFirmId() != null) {
			this.firmId = model.getFirmId().getId();
			this.firmName = model.getFirmId().getName();
		}

		if (model.getCreateById() != null) {
			this.createById = model.getCreateById().getId();
			this.createByName = model.getCreateById().getEmployeename();
			this.createdDate = dateUtil.getLocalDateTimeToDateTimeString(model.getCreatedTimeStamp());
		}

		if (model.getRejectedBy() != null) {
			this.rejectedBy = model.getRejectedBy().getId();
			this.rejecterName = model.getRejectedBy().getEmployeename();
			this.rejectedDate = dateUtil.getDateTime(model.getRejected_date());
		}

		if (model.getDept() != null) {
			this.dept = model.getDept().getId();
		}
		if (model.getBranch() != null) {
			this.branch = model.getBranch().getId();
		}

		if (model.getActivateBy() != null) {
			this.activateBy = model.getActivateBy().getId();
			this.activateByName = model.getActivateBy().getEmployeename();
			this.activateDate = dateUtil.getDateTime(model.getActivate_date());
		}
	}
}
