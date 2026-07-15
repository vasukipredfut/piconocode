package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.No2billitemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2_bill_items")
public class No2billitems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "bill_id")
	private Long billId;

	@Column(name = "quantity")
	private Long quantity;

	@Column(name = "rate")
	private Double rate;

	@Column(name = "kattu", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
	private Long kattu;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "transaction_by")
	private Long transactionBy;

	@Column(name = "transaction_date")
	private LocalDateTime transactionDate;

	@Column(name = "trnno")
	private String trnNo;

	@Column(name = "igst_amt")
	private Double igstAmt;

	@Column(name = "sgst_amt")
	private Double sgstAmt;

	@Column(name = "cgst_amt")
	private Double cgstAmt;

	public No2billitems(No2billitemsDto no2billitems) {

		this.amount = no2billitems.getNetAmount();
		this.id = no2billitems.getId();
		this.billId = no2billitems.getBillId();
		this.quantity = no2billitems.getQuantity();
		this.kattu = no2billitems.getKattu();
		if (no2billitems.getKattu() != null && no2billitems.getKattu() > 0) {
			this.quantity = no2billitems.getKattu() * 50;
		} else {
			this.quantity = no2billitems.getQuantity();
		}
		this.amount = no2billitems.getNetAmount();

		this.productId = no2billitems.getProductId();
		this.rate = no2billitems.getRate();
		this.transactionBy = no2billitems.getTransactionBy();
		this.transactionDate = no2billitems.getTransactionDate();
		this.trnNo = no2billitems.getTrnNo();

	}
}
