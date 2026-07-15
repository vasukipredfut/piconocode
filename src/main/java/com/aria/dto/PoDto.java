package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.PurchaseOrder;
import com.aria.model.PurchasePayment;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PoDto {

	private Long id;

	private String poNumber;
	private String poDate;

	private Long companyBranchId;

	private Long itemGroupId;

	private String itemGroup;

	private Long dept;

	private Long branch;

	private Long supplierId;

	private Double poTotalAmount;
	private Double roundOff_amount;
	private String roundOffType;
	private String poTotalAmount_value;

	private String supplierName;

	private Long cityId;

	private String city;

	private Long addressId;

	private Long stateId;

	private String stateName;

	private String gstNO;

	private Long brokerId;

	private String brokerName;

	private Long subBrokerId;

	private String subBrokerName;

	private Double totalInitialPrice;

	private Double totalFinalPrice;

	private Long campId;

	private String camp;

	private String poNotes;

	private String createdBy;

	private String createdON;

	private String poTransferedBy;

	private String poTransferedON;

	private String referenceNo;

	private String poMode;

	private String status;

	private String receivedBy;

	private String receiverDesignation;

	private String receivedON;

	private Long approvedBy;

	private String approvedDate;

	private String approverName;

	private String approvedOn;

	private String remarks;

	private String approvalStatus;

	private String cessDate;

	private String cessNo;

	private Double cessAmount;
	private String poNoOriginal;

	private String cessType;

	private String typeFlag;

	private Long vendorId;

	private String vendorName;
	private String vendorAddress;
	private String vendorMobileNo;
	private String vendorCity;
	private String vendorState;
	private String vendorEmail;
	private String vendorGST;
	private String vendorPinCode;

	private String created_date;

	private LocalDateTime updated_date;
	private String updatedDate;

	private Long updatedBy;
	private String updatedByName;

	private List<PoItemsDto> purchaseOrderItems;

	private List<PurchasePaymentDto> purchasePaymentOption;

	private Long proposalId;
	private String proposalName;
	private String quotationNo;
	private String quotationDate;
	private String packingFreightAmount;

	private Long deliveryplant;

	private String deliveryplantName;

	private String deliveryPlantAddress;
	private String deliveryPlantState;

	private String deliveryPlantCity;
	private String deliveryPlantEmail;
	private String deliveryPlantPhone;
	private String deliveryPlantGST;
	private String deliveryPlantPincode;

	private String deliveryPlantFirmName;
	private String deliveryPlantFirmAddress;
	private String deliveryPlantFirmState;
	private String deliveryPlantFirmCity;
	private String deliveryPlantFirmEmail;
	private String deliveryPlantFirmPhone;
	private String deliveryPlantFirmGST;
	private String deliveryPlantFirmPinCode;

	private String deliveryduedate;
	private Long partnercontribution;

	private String poStatus;

	private boolean advanceType;

	private Long verifiedBy;
	private String verifiedDate;

	private String verifierName;

	private Long authorizedBy;

	private String authorizedDate;

	private String authorizerName;

	private Long amendmentBy;

	private String amendmentDate;

	private String amendmentByName;

	private Long firmId;

	private Long rejectedBy;
	private String rejectedDate;

	private String rejecterName;

	public PoDto(PurchaseOrder purchaseOrder) {
		DateUtil dateUtil = new DateUtil();
		this.id = purchaseOrder.getId();
		this.poNumber = purchaseOrder.getPoNumber();
		if (purchaseOrder.getPoDate() != null && purchaseOrder.getPoDate() != null) {
			this.poDate = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getPoDate());
		}
		this.companyBranchId = purchaseOrder.getCompanyBranchId();
		this.itemGroupId = purchaseOrder.getItemGroupId();
		this.itemGroup = purchaseOrder.getItemGroup();
		this.supplierId = purchaseOrder.getSupplierId();
		this.poTotalAmount = purchaseOrder.getPoTotalAmount();
		this.roundOff_amount = purchaseOrder.getRoundOff_amount();
		this.roundOffType = purchaseOrder.getRoundOffType();
		this.supplierName = purchaseOrder.getSupplierName();
		this.cityId = purchaseOrder.getCityId();
		this.city = purchaseOrder.getCity();
		this.addressId = purchaseOrder.getAddressId();
		this.stateId = purchaseOrder.getStateId();
		this.stateName = purchaseOrder.getStateName();
		this.gstNO = purchaseOrder.getGstNO();
		this.brokerId = purchaseOrder.getBrokerId();
		this.brokerName = purchaseOrder.getBrokerName();
		this.subBrokerId = purchaseOrder.getSubBrokerId();
		this.subBrokerName = purchaseOrder.getSubBrokerName();
		this.totalInitialPrice = purchaseOrder.getTotalInitialPrice();
		this.totalFinalPrice = purchaseOrder.getTotalFinalPrice();
		this.campId = purchaseOrder.getCampId();
		this.camp = purchaseOrder.getCamp();
		this.poNotes = purchaseOrder.getPoNotes();
		if (purchaseOrder.getCreatedBy() != null) {
			this.createdBy = purchaseOrder.getCreatedBy().getEmployeename();
			this.created_date = dateUtil.getLocalDateTimeToDateTimeString(purchaseOrder.getCreated_date());
		}
		this.firmId = purchaseOrder.getFirmId();
		if (purchaseOrder.getCreatedON() != null) {
			this.createdON = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getCreatedON());

		}
		this.poTransferedBy = purchaseOrder.getPoTransferedBy();
		if (purchaseOrder.getPoTransferedON() != null)
			this.poTransferedON = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getPoTransferedON());
		this.referenceNo = purchaseOrder.getReferenceNo();
		this.poMode = purchaseOrder.getPoMode();
		this.status = purchaseOrder.getStatus();
		this.receivedBy = purchaseOrder.getReceivedBy();
		this.receiverDesignation = purchaseOrder.getReceiverDesignation();
		if (purchaseOrder.getReceivedON() != null)
			this.receivedON = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getReceivedON());
		if (purchaseOrder.getApprovedBy() != null) {
			this.approvedBy = purchaseOrder.getApprovedBy().getId();
			this.approverName = purchaseOrder.getApprovedBy().getEmployeename();
			this.approvedDate = dateUtil.getDateTime(purchaseOrder.getApprovedOn());
		}

		if (purchaseOrder.getAuthorizedBy() != null) {
			this.authorizedBy = purchaseOrder.getAuthorizedBy().getId();
			this.authorizerName = purchaseOrder.getAuthorizedBy().getEmployeename();
			this.authorizedDate = dateUtil.getDateTime(purchaseOrder.getAuthorizedOn());
		}

		if (purchaseOrder.getVerifiedBy() != null) {
			this.verifiedBy = purchaseOrder.getVerifiedBy().getId();
			this.verifierName = purchaseOrder.getVerifiedBy().getEmployeename();
			this.verifiedDate = dateUtil.getDateTime(purchaseOrder.getVerifiedOn());
		}

		if (purchaseOrder.getRejectedBy() != null) {
			this.rejectedBy = purchaseOrder.getRejectedBy().getId();
			this.rejecterName = purchaseOrder.getRejectedBy().getEmployeename();
			this.rejectedDate = dateUtil.getDateTime(purchaseOrder.getRejectedOn());
		}

		if (purchaseOrder.getUpdatedBy() != null) {
			this.updatedBy = purchaseOrder.getUpdatedBy().getId();
			this.updatedByName = purchaseOrder.getUpdatedBy().getEmployeename();
			this.updatedDate = dateUtil.getLocalDateTimeToDateTimeString(purchaseOrder.getUpdated_date());
		}
		if (purchaseOrder.getAmendmentBy() != null) {
			this.amendmentBy = purchaseOrder.getAmendmentBy().getId();
			this.amendmentByName = purchaseOrder.getAmendmentBy().getEmployeename();
			this.amendmentDate = dateUtil.getLocalDateTimeToDateTimeString(purchaseOrder.getAmendment_date());
		}

		if (purchaseOrder.getDept() != null) {
			this.dept = purchaseOrder.getDept().getId();
		}
		if (purchaseOrder.getBranch() != null) {
			this.branch = purchaseOrder.getBranch().getId();
		}

		if (purchaseOrder.getApprovedOn() != null)
			this.approvedOn = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getApprovedOn());
		this.remarks = purchaseOrder.getRemarks();
		this.approvalStatus = purchaseOrder.getApprovalStatus();
		if (purchaseOrder.getCessDate() != null)
			this.cessDate = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getCessDate());
		this.cessNo = purchaseOrder.getCessNo();
		this.cessAmount = purchaseOrder.getCessAmount();
		this.poNoOriginal = purchaseOrder.getPoNoOriginal();
		this.cessType = purchaseOrder.getCessType();
		this.typeFlag = purchaseOrder.getTypeFlag();
		this.proposalId = purchaseOrder.getProposalId();
		if (purchaseOrder.getDeliveryduedate() != null)
			this.deliveryduedate = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getDeliveryduedate());
		this.deliveryplant = purchaseOrder.getDeliveryplant();
		this.partnercontribution = purchaseOrder.getPartnercontribution();
		this.poStatus = purchaseOrder.getPo_status();
		this.advanceType = purchaseOrder.isAdvanceType();
		this.advanceType = purchaseOrder.isAdvanceType();
		if (purchaseOrder.getVendor() != null) {
			this.vendorId = purchaseOrder.getVendor().getId();
			this.vendorName = purchaseOrder.getVendor().getName();
			this.vendorAddress = purchaseOrder.getVendor().getAddress();
			this.vendorMobileNo = purchaseOrder.getVendor().getMobile();
			this.vendorCity = purchaseOrder.getVendor().getCity();
			this.vendorState = purchaseOrder.getVendor().getState();
			this.vendorEmail = purchaseOrder.getVendor().getEmail();
			this.vendorGST = purchaseOrder.getVendor().getGstNumber();
			this.vendorPinCode = purchaseOrder.getVendor().getPincode();
		}
		if(purchaseOrder.getPurchaseOrderItems() != null && !purchaseOrder.getPurchaseOrderItems().isEmpty()) {
		this.purchaseOrderItems = purchaseOrder.getPurchaseOrderItems() != null ? purchaseOrder.getPurchaseOrderItems()
				.stream().map(field -> new PoItemsDto(field)).collect(Collectors.toList()) : null;
		}
		this.purchasePaymentOption = purchaseOrder.getPurchasePaymentOption() != null
				? purchaseOrder.getPurchasePaymentOption().stream().sorted(Comparator.comparing(PurchasePayment::getId))
						.map(field -> new PurchasePaymentDto(field)).collect(Collectors.toList())
				: null;

	}

	public PoDto(POList poList) {
		this.id = poList.getId();
		this.poNumber = poList.getPoNumber();
	}


	public PoDto(PurchaseOrder purchaseOrder, boolean status) {
		DateUtil dateUtil = new DateUtil();
		this.id = purchaseOrder.getId();
		this.poNumber = purchaseOrder.getPoNumber();
		if (purchaseOrder.getPoDate() != null && purchaseOrder.getPoDate() != null) {
			this.poDate = dateUtil.getDateToStringYYYYMMDD(purchaseOrder.getPoDate());
		}
		this.companyBranchId = purchaseOrder.getCompanyBranchId();
		this.firmId = purchaseOrder.getFirmId();
		this.status = purchaseOrder.getStatus();
		this.typeFlag = purchaseOrder.getTypeFlag();
		this.proposalId = purchaseOrder.getProposalId();
		this.poStatus = purchaseOrder.getPo_status();
		this.advanceType = purchaseOrder.isAdvanceType();
		if (purchaseOrder.getVendor() != null) {
			this.vendorId = purchaseOrder.getVendor().getId();
			this.vendorName = purchaseOrder.getVendor().getName();
		}
		this.purchasePaymentOption = purchaseOrder.getPurchasePaymentOption() != null
				? purchaseOrder.getPurchasePaymentOption().stream().sorted(Comparator.comparing(PurchasePayment::getId))
				.map(field -> new PurchasePaymentDto(field)).collect(Collectors.toList())
				: null;

	}


}
