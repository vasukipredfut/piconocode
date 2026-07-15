package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import com.aria.dao.No2ProductsRepository;
import com.aria.model.No2Products;
import com.aria.model.No2billitems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2billitemsDto {

	private Long id;

	private Long productId;

	private String productName;

	private Long billId;

	private Long quantity;

	private Double rate;

	private Long kattu;

	private Double netAmount;

	private Long transactionBy;

	private LocalDateTime transactionDate;

	private String trnNo;

	private Double igstAmt;

	private Double igstPercent;

	private Double cgstAmt;

	private Double cgstPercent;

	private Double sgstAmt;

	private Double sgstPercent;

	public No2billitemsDto(No2billitems no2billitems, String product_type,
			No2ProductsRepository no2ProductsRepository) {

		this.netAmount = no2billitems.getAmount();
		this.id = no2billitems.getId();
		this.billId = no2billitems.getBillId();
		this.kattu = no2billitems.getKattu();
		this.quantity = no2billitems.getQuantity();
		this.productId = no2billitems.getProductId();
		System.out.println(no2billitems.getProductId() + " == " + product_type);
		Optional<No2Products> products = no2ProductsRepository.findByProductIdANDType(no2billitems.getProductId(),
				product_type);
		if (products.isPresent()) {
			this.productName = products.get().getName();
		}

		this.rate = no2billitems.getRate();
		this.transactionBy = no2billitems.getTransactionBy();
		this.transactionDate = no2billitems.getTransactionDate();
		this.trnNo = no2billitems.getTrnNo();
		this.igstAmt = no2billitems.getIgstAmt();
		this.sgstAmt = no2billitems.getSgstAmt();
		this.cgstAmt = no2billitems.getCgstAmt();
	}

	public No2billitemsDto(No2billitems no2billitems) {

		this.netAmount = no2billitems.getAmount();
		this.id = no2billitems.getId();
		this.billId = no2billitems.getBillId();
		this.kattu = no2billitems.getKattu();
		this.quantity = no2billitems.getQuantity();
		this.productId = no2billitems.getProductId();
		this.rate = no2billitems.getRate();
		this.transactionBy = no2billitems.getTransactionBy();
		this.transactionDate = no2billitems.getTransactionDate();
		this.trnNo = no2billitems.getTrnNo();
		this.igstAmt = no2billitems.getIgstAmt();
		this.sgstAmt = no2billitems.getSgstAmt();
		this.cgstAmt = no2billitems.getCgstAmt();

	}
}
