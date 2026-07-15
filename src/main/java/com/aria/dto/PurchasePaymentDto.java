package com.aria.dto;

import com.aria.model.PurchasePayment;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchasePaymentDto {

	private Long id;

	private Long purchase_order_id;

	public Long paymentCategoryId;

	public String paymentCategoryName;

	public Double percentage;

	public Double amount;

	public Integer payment_status;

	public String status;

	public String payment_date;

	public Long payment_by;

	public Long firmId;

	private Long dept;

	private Long branch;

	public PurchasePaymentDto(PurchasePayment purchasePayment) {
		DateUtil dateUtil = new DateUtil();

		this.id = purchasePayment.getId();
		this.purchase_order_id = purchasePayment.getPurchaseOrderId();
		this.paymentCategoryId = purchasePayment.getPaymentCategoryId();
		this.paymentCategoryName = purchasePayment.getPaymentCategoryName();
		this.percentage = purchasePayment.getPercentage();
		this.amount = purchasePayment.getAmount();
		this.firmId = purchasePayment.getFirmId();
		this.payment_status = purchasePayment.getPayment_status();
		this.status = purchasePayment.getStatus();
		if (purchasePayment.getPayment_date() != null) {
			this.payment_date = dateUtil.getDateToStringYYYYMMDD(purchasePayment.getPayment_date());
		}
		this.payment_by = purchasePayment.getPayment_by();

		if (purchasePayment.getDept() != null) {
			this.dept = purchasePayment.getDept().getId();
		}
		if (purchasePayment.getBranch() != null) {
			this.branch = purchasePayment.getBranch().getId();
		}

	}

}
