package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

import com.aria.dto.No2billsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2_bills")
public class No2bills {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "party_id")
	private Long partyId;

	@Column(name = "quantity")
	private Long quantity;

	@Column(name = "kattu", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
	private Long kattu;

	@Column(name = "total_cost")
	private Double totalCost;

	@Column(name = "verify_status")
	private String verifyStatus;

	@OneToOne
	@JoinColumn(name = "verify_by", referencedColumnName = "id")
	private User verifyBy;

	@Column(name = "approve_status")
	private String approveStatus;

	@OneToOne
	@JoinColumn(name = "approve_by", referencedColumnName = "id")
	private User approveBy;

	@Column(name = "authorize_status")
	private String authorizeStatus;

	@OneToOne
	@JoinColumn(name = "authorize_by", referencedColumnName = "id")
	private User authorizeBy;

	@Column(name = "vdate")
	private LocalDateTime vdate;

	@Column(name = "adate")
	private LocalDateTime adate;

	@Column(name = "aodate")
	private LocalDateTime aodate;

	@Column(name = "bill_status")
	private String billStatus;

	@Column(name = "bill_prepare_by")
	private Long billPrepareBy;

	@Column(name = "perbag_amount")
	private Double perbagAmount;

	@Column(name = "tcs_percent")
	private Double tcsPercent;

	@Column(name = "tcs_amount")
	private Double tcsAmount;

	@Column(name = "net_amount")
	private Double netAmount;

	@Column(name = "gstamt")
	private Double gstAmt;

	@Column(name = "roundoff")
	private Double roundOff;

	@OneToOne
	@JoinColumn(name = "transaction_by", referencedColumnName = "id")
	private User transactionBy;

	@Column(name = "transaction_date")
	private LocalDateTime transactionDate;

	@Column(name = "trnno")
	private String trnNo;

	@Column(name = "product_type")
	private String productType;

	@OneToMany(mappedBy = "billId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	List<No2billitems> no2billitems;

	public No2bills(No2billsDto no2bills) {

		this.id = no2bills.getId();

		this.authorizeStatus = no2bills.getAuthorizeStatus();
		this.billPrepareBy = no2bills.getBillPrepareBy();
		this.billStatus = no2bills.getBillStatus();
		if (Objects.nonNull(no2bills.getKattu())) {
			this.kattu = no2bills.getKattu();
		} else {
			this.kattu = 0L;
		}
		if (Objects.isNull(no2bills.getQuantity())) {
			if (Objects.nonNull(no2bills.getKattu())) {
				this.quantity = no2bills.getKattu() * 50;
			} else {
				this.quantity = 0L;
			}
		} else {
			this.quantity = 0L;
		}
		if (Objects.nonNull(this.netAmount) && this.netAmount > 0) {
			this.perbagAmount = this.netAmount / this.quantity;
			this.tcsAmount = this.netAmount * 0.1;
			this.tcsPercent = 0.1;
			this.gstAmt = this.netAmount * 0.05;
			this.totalCost = (double) Math.round(this.netAmount + this.gstAmt + this.tcsAmount);

			this.roundOff = this.totalCost - this.netAmount;

		} else {
			this.netAmount = 0D;
			this.perbagAmount = 0D;
			this.tcsAmount = this.netAmount * 0.1;
			this.tcsPercent = 0.1;
			this.gstAmt = this.netAmount * 0.05;
			this.totalCost = 0D;

			this.roundOff = this.totalCost - this.netAmount;
		}
		this.partyId = no2bills.getPartyId();

		this.transactionDate = no2bills.getTransactionDate();
		this.trnNo = no2bills.getTrnNo();

		this.verifyStatus = no2bills.getVerifyStatus();
		this.approveStatus = no2bills.getApproveStatus();
		this.productType = no2bills.getProductType();

	}
}
