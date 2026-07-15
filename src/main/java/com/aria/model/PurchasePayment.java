package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.PurchasePaymentDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_order_payment")
public class PurchasePayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "purchase_Order_Id")
	private Long purchaseOrderId;

	@Column(name = "payment_Category_Id")
	public Long paymentCategoryId;

	@Column(name = "payment_Category_Name")
	public String paymentCategoryName;

	@Column
	public Double percentage;

	@Column
	public Double amount;

	@Column
	public Integer payment_status;

	public String status;

	@Column
	public Date payment_date;

	@Column
	public Long payment_by;

	@Column(name = "firm_id")
	public Long firmId;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails dept;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	public PurchasePayment(PurchasePaymentDto purchasePayment) {
		DateUtil dateUtil = new DateUtil();
		this.id = purchasePayment.getId();
		this.purchaseOrderId = purchasePayment.getPurchase_order_id();
		if (purchasePayment.getPaymentCategoryId() != null) {
			this.paymentCategoryId = purchasePayment.getPaymentCategoryId();
		}
		this.firmId = purchasePayment.getFirmId();
		this.paymentCategoryName = purchasePayment.getPaymentCategoryName();
		this.percentage = purchasePayment.getPercentage();
		this.amount = purchasePayment.getAmount();
		this.payment_status = purchasePayment.getPayment_status();
		this.status = purchasePayment.getStatus();
		if (purchasePayment.getPayment_date() != null) {
			this.payment_date = dateUtil.getStringTODateYYYYMMDD(purchasePayment.getPayment_date());
		}

		if (purchasePayment.getDept() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(purchasePayment.getDept());
			this.dept = departmentDetails;
		}
		if (purchasePayment.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(purchasePayment.getBranch());
			this.branch = branch;
		}

		this.payment_by = purchasePayment.getPayment_by();
	}
}
