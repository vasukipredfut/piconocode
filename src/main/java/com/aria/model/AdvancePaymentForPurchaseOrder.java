package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.AdvancePaymentForPurchaseOrderDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_advance_payment")
public class AdvancePaymentForPurchaseOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long purchase_advance_payment_id;

	@Column(name = "active_flag")
	private Integer activeFlag;

	@Column(name = "acount_number")
	private String acountNumber;

	@Column(name = "advance_amount")
	private Double advanceAmount;

	@Column(name = "advance_payment_company_id")
	private Long advancePaymentCompanyId;

	@Column(name = "advance_payment_date")
	private Date advancePaymentDate;

	@Column(name = "po_amount")
	private Double po_amount;

	@Column(name = "created_time_stamp", updatable = false)
	private LocalDateTime createdTimeStamp;

	@Column(name = "ddfavourof")
	private String ddfavourof;

	@Column(name = "ifsc")
	private String ifsc;

	@Column(name = "number_of_pay")
	private Integer numberOfPay;

	@Column(name = "payment_mode")
	private String paymentMode;

	@Column(name = "payment_type")
	private String paymentType;

	@Column(name = "pinfo")
	private Integer pinfo;

	@Column(name = "percentage")
	private Double percentage;

	@Column(name = "purchase_order_ref_id")
	private Long purchaseOrderRefId;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "quotation_date")
	private Date quotationDate;

	@Column(name = "quotation_number")
	private String quotationNumber;

	@Column(name = "status")
	private String status;

	@Column(name = "tds")
	private String tds;

	@Column(name = "trnno")
	private String trnno;

	@Column(name = "vendor_address")
	private String vendorAddress;

	@Column(name = "vendor_ref_id")
	private Long vendorRefId;

	@Column(name = "pending_amount")
	private Double pendingAmount;

	@Column(name = "purchase_payment_id", updatable = false)
	private Long purchasePaymentId;

	@Column(name = "payment_category_id")
	public Long paymentCategoryId;

	@Column(name = "payment_category_name")
	public String paymentCategoryName;

	@OneToOne
	@JoinColumn(name = "verified_by", referencedColumnName = "id")
	private User verifiedBy;

	@Column
	private Date verified_date;

	@OneToOne
	@JoinColumn(name = "approved_by", referencedColumnName = "id")
	private User approvedBy;

	@Column
	private Date approved_date;

	@OneToOne
	@JoinColumn(name = "authorized_by", referencedColumnName = "id")
	private User authorizedBy;

	@Column
	private Date authorized_date;

	@OneToOne
	@JoinColumn(name = "create_by_id", referencedColumnName = "id", updatable = false)
	private User createById;

	@Column(name = "created_date", updatable = false)
	private Date created_date;

	@OneToOne
	@JoinColumn(name = "rejected_by", referencedColumnName = "id")
	private User rejectedBy;

	@Column
	private Date rejected_date;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails dept;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@OneToOne
	@JoinColumn(name = "firm_id", referencedColumnName = "id")
	private Organisation firmId;


	@OneToOne
	@JoinColumn(name = "activate_by_id")
	private User activateBy;

	@Column(name = "activate_date")
	private Date activate_date;

	public AdvancePaymentForPurchaseOrder(AdvancePaymentForPurchaseOrderDto payment) {
		DateUtil dateUtil = new DateUtil();
		this.purchase_advance_payment_id = payment.getPurchase_advance_payment_id();
		this.acountNumber = payment.getAcountNumber();
		this.advanceAmount = payment.getAdvanceAmount();
		this.advancePaymentCompanyId = payment.getAdvancePaymentCompanyId();
		if (payment.getAdvancePaymentDate() != null) {
			this.advancePaymentDate = dateUtil.getStringTODateYYYYMMDD(payment.getAdvancePaymentDate());
		} else {
			this.advancePaymentDate = dateUtil.getISTDate();
		}

		this.createdTimeStamp = LocalDateTime.now();
		this.ddfavourof = payment.getDdfavourof();
		this.ifsc = payment.getIfsc();
		this.numberOfPay = payment.getNumberOfPay();
		this.paymentMode = payment.getPaymentMode();
		this.paymentType = payment.getPaymentType();
		this.pinfo = payment.getPinfo();
		this.purchaseOrderRefId = payment.getPurchaseOrderRefId();
		this.percentage = payment.getPercentage();
		this.purpose = payment.getPurpose();
		this.po_amount = payment.getPo_amount();
		this.pendingAmount = payment.getPendingAmount();
		if (payment.getQuotationDate() != null) {
			this.quotationDate = dateUtil.getStringTODateYYYYMMDD(payment.getQuotationDate());
		}
		this.quotationNumber = payment.getQuotationNumber();
		this.tds = payment.getTds();
		this.trnno = payment.getTrnno();
		this.vendorAddress = payment.getVendorAddress();
		this.vendorRefId = payment.getVendorRefId();
		this.paymentCategoryId = payment.getPaymentCategoryId();
		this.paymentCategoryName = payment.getPaymentCategoryName();
		this.purchasePaymentId = payment.getPurchasePaymentId();
		if (payment.getDept() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(payment.getDept());
			this.dept = departmentDetails;
		}
		if (payment.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(payment.getBranch());
			this.branch = branch;
		}
		if (payment.getFirmId() != null) {
			Organisation organisation = new Organisation();
			organisation.setId(payment.getFirmId());
			this.firmId = organisation;
		}
	}

}
