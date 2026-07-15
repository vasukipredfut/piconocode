package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.No2ProductsPartyRepository;
import com.aria.dao.No2ProductsRepository;
import com.aria.model.No2ProductsParty;
import com.aria.model.No2bills;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2billsDto {

	private Long id;

	private Long partyId;

	private String partyName;
	private String state;

	private String brokerName;

	private Long quantity;

	private Long kattu;

	private Double totalCost;

	private String verifyStatus;

	private Long verifyBy;

	private String verifyName;

	private String approveStatus;

	private Long approveBy;

	private String approverName;

	private String authorizeStatus;

	private Long authorizeBy;

	private String authorizerName;

	private String vdate;

	private String adate;

	private String aodate;

	private String billStatus;

	private Long billPrepareBy;

	private String billPreparerName;

	private Double perbagAmount;

	private Double tcsPercent;

	private Double tcsAmount;

	private Double netAmount;

	private Double gstAmt;

	private Double roundOff;

	private Long transactionBy;

	private LocalDateTime transactionDate;

	private String trnNo;

	List<No2billitemsDto> no2billitems;

	private String productType;

	public No2billsDto(No2bills no2bills, No2ProductsRepository no2ProductsRepository,
			No2ProductsPartyRepository no2ProductsPartyRepository) {
		DateUtil dateUtil = new DateUtil();
		this.id = no2bills.getId();
		this.adate = dateUtil.localDateTimeToString(no2bills.getAdate()) ;
		this.aodate = dateUtil.localDateTimeToString(no2bills.getAodate());
		this.vdate = dateUtil.localDateTimeToString(no2bills.getVdate());
		this.authorizeStatus = no2bills.getAuthorizeStatus();
		this.billPrepareBy = no2bills.getBillPrepareBy();
		this.billStatus = no2bills.getBillStatus();
		this.gstAmt = no2bills.getGstAmt();
		this.kattu = no2bills.getKattu();
		this.netAmount = no2bills.getNetAmount();
		this.partyId = no2bills.getPartyId();

		if (no2bills.getVerifyBy() != null) {
			this.verifyBy = no2bills.getVerifyBy().getId();
			this.verifyName = no2bills.getVerifyBy().getEmployeename();
		}
		if (no2bills.getApproveBy() != null) {
			this.approveBy = no2bills.getApproveBy().getId();
			this.approverName = no2bills.getApproveBy().getEmployeename();
		}
		if (no2bills.getAuthorizeBy() != null) {
			this.authorizeBy = no2bills.getAuthorizeBy().getId();
			this.authorizerName = no2bills.getAuthorizeBy().getEmployeename();
		}
		if (no2bills.getTransactionBy() != null) {
			this.transactionBy = no2bills.getTransactionBy().getId();
			this.authorizerName = no2bills.getTransactionBy().getEmployeename();
		}

		Optional<No2ProductsParty> party = no2ProductsPartyRepository.findByPartyId(no2bills.getPartyId());
		if (party.isPresent()) {
			this.partyName = party.get().getPartyName();
			this.brokerName = party.get().getBrokerName();
		}
		this.perbagAmount = no2bills.getPerbagAmount();
		this.quantity = no2bills.getQuantity();
		this.roundOff = no2bills.getRoundOff();
		this.tcsAmount = no2bills.getTcsAmount();
		this.tcsPercent = no2bills.getTcsPercent();
		this.totalCost = no2bills.getTotalCost();
		this.transactionDate = no2bills.getTransactionDate();
		this.trnNo = no2bills.getTrnNo();
		this.verifyStatus = no2bills.getVerifyStatus();
		this.approveStatus = no2bills.getApproveStatus();
		this.productType = no2bills.getProductType();
		this.no2billitems = no2bills.getNo2billitems() != null ? no2bills.getNo2billitems().stream()
				.map(obj -> new No2billitemsDto(obj, no2bills.getProductType(), no2ProductsRepository))
				.collect(Collectors.toList()) : null;

	}

	public No2billsDto(No2bills no2bills) {
DateUtil dateUtil = new DateUtil();
		this.id = no2bills.getId();
		this.adate = dateUtil.localDateTimeToString(no2bills.getAdate());
		this.aodate = dateUtil.localDateTimeToString(no2bills.getAodate());
		if (no2bills.getVerifyBy() != null) {
			this.verifyBy = no2bills.getVerifyBy().getId();
			this.verifyName = no2bills.getVerifyBy().getEmployeename();
		}
		if (no2bills.getApproveBy() != null) {
			this.approveBy = no2bills.getApproveBy().getId();
			this.approverName = no2bills.getApproveBy().getEmployeename();
		}
		if (no2bills.getAuthorizeBy() != null) {
			this.authorizeBy = no2bills.getAuthorizeBy().getId();
			this.authorizerName = no2bills.getAuthorizeBy().getEmployeename();
		}
		if (no2bills.getTransactionBy() != null) {
			this.transactionBy = no2bills.getTransactionBy().getId();
			this.authorizerName = no2bills.getTransactionBy().getEmployeename();
		}

		this.vdate = dateUtil.localDateTimeToString(no2bills.getVdate());
		this.authorizeStatus = no2bills.getAuthorizeStatus();
		this.billPrepareBy = no2bills.getBillPrepareBy();
		this.billStatus = no2bills.getBillStatus();
		this.gstAmt = no2bills.getGstAmt();
		this.kattu = no2bills.getKattu();
		this.netAmount = no2bills.getNetAmount();
		this.partyId = no2bills.getPartyId();
		this.perbagAmount = no2bills.getPerbagAmount();
		this.quantity = no2bills.getQuantity();
		this.roundOff = no2bills.getRoundOff();
		this.tcsAmount = no2bills.getTcsAmount();
		this.tcsPercent = no2bills.getTcsPercent();
		this.totalCost = no2bills.getTotalCost();
		this.transactionDate = no2bills.getTransactionDate();
		this.trnNo = no2bills.getTrnNo();
		this.verifyStatus = no2bills.getVerifyStatus();
		this.approveStatus = no2bills.getApproveStatus();
		this.productType = no2bills.getProductType();

	}
}
