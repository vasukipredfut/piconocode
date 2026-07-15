package com.aria.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.PurchaseOrder;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDto {

	public Long id;

	public String poNumber;

	public String po_date;

	public Long company_branch_id;

	public Long item_group_id;

	public String item_group;

	private Long dept;

	private Long branch;

	public Long vendorId;

	public Double po_total_amount;

	public Double roundOff_amount;

	public String roundOffType;

	public String vendorName;

	public Long city_id;

	public String city;

	public Long address_id;

	public Long state_id;

	public String state_name;

	public String gst_no;

	public Long broker_id;

	public String broker_name;

	public Long sub_broker_id;

	public String sub_broker_name;

	public Double total_initial_price;

	public Double total_final_price;

	public Long camp_id;

	public String camp;

	public String po_notes;

	public String created_by;

	public String created_on;

	public String po_transfered_by;

	public String po_transfered_on;

	public String reference_no;

	public String po_mode;

	public String proposalName;

	public String status;

	public String received_by;

	public String receiver_designation;

	public String received_on;

	private Long approvedBy;

	private String approverName;

	public String approved_on;

	public String remarks;

	public String approval_status;

	public String cess_date;

	public String cess_no;

	public Double cess_amount;

	public String po_no_original;

	public String cess_type;

	public String typeflag;

	private List<PurchaseOrderItemsDto> poOrderItems;

	private List<PurchasePaymentDto> purchasePaymentOption;

	private String deletePurchasePaymentIds;

	private Long proposalId;

	private Long deliveryplant;

	private String deliveryplantName;

	private String deliveryduedate;

	private Long partnercontribution;

	private boolean advanceType;

	private Long verifiedBy;

	private String verifierName;

	private Long authorizedBy;

	private String authorizerName;

	private Long rejectedBy;

	private String rejecterName;

	private Long firmId;
	private boolean updatePurchaseOrder;
	private boolean amendmentPo;

	public PurchaseOrderDto(PurchaseOrder purchaseorder) {
		DateUtil dateutil = new DateUtil();
		this.id = purchaseorder.getId();
		this.poNumber = purchaseorder.getPoNumber();
		this.firmId = purchaseorder.getFirmId();
		if (purchaseorder.getPoDate() != null && purchaseorder.getPoDate() != null) {
			this.po_date = dateutil.getDateToStringYYYYMMDD(purchaseorder.getPoDate());
		}
		this.company_branch_id = purchaseorder.getCompanyBranchId();
		this.item_group_id = purchaseorder.getItemGroupId();
		this.item_group = purchaseorder.getItemGroup();
		this.vendorId = purchaseorder.getSupplierId();
		this.po_total_amount = purchaseorder.getPoTotalAmount();
		this.roundOff_amount = purchaseorder.getRoundOff_amount();
		this.roundOffType = purchaseorder.getRoundOffType();
		this.vendorName = purchaseorder.getSupplierName();
		this.city_id = purchaseorder.getCityId();
		this.city = purchaseorder.getCity();
		this.address_id = purchaseorder.getAddressId();
		this.state_id = purchaseorder.getStateId();
		this.state_name = purchaseorder.getStateName();
		this.gst_no = purchaseorder.getGstNO();
		this.broker_id = purchaseorder.getBrokerId();
		this.broker_name = purchaseorder.getBrokerName();
		this.sub_broker_id = purchaseorder.getSubBrokerId();
		this.sub_broker_name = purchaseorder.getSubBrokerName();
		this.total_initial_price = purchaseorder.getTotalInitialPrice();
		this.total_final_price = purchaseorder.getTotalFinalPrice();
		this.camp_id = purchaseorder.getCampId();
		this.camp = purchaseorder.getCamp();
		this.po_notes = purchaseorder.getPoNotes();
		if (purchaseorder.getCreatedON() != null) {
			this.created_on = dateutil.getDateToStringYYYYMMDD(purchaseorder.getCreatedON());
		}
		this.po_transfered_by = purchaseorder.getPoTransferedBy();
		if (purchaseorder.getPoTransferedON() != null)
			this.po_transfered_on = dateutil.getDateToStringYYYYMMDD(purchaseorder.getPoTransferedON());
		this.reference_no = purchaseorder.getReferenceNo();
		this.po_mode = purchaseorder.getPoMode();
		this.status = purchaseorder.getStatus();
		this.received_by = purchaseorder.getReceivedBy();
		this.receiver_designation = purchaseorder.getReceiverDesignation();
		if (purchaseorder.getReceivedON() != null)
			this.received_on = dateutil.getDateToStringYYYYMMDD(purchaseorder.getReceivedON());
		if (purchaseorder.getApprovedBy() != null) {
			this.approvedBy = purchaseorder.getApprovedBy().getId();
			this.approverName = purchaseorder.getApprovedBy().getEmployeename();
		}

		if (purchaseorder.getAuthorizedBy() != null) {
			this.authorizedBy = purchaseorder.getAuthorizedBy().getId();
			this.authorizerName = purchaseorder.getAuthorizedBy().getEmployeename();
		}

		if (purchaseorder.getRejectedBy() != null) {
			this.rejectedBy = purchaseorder.getRejectedBy().getId();
			this.rejecterName = purchaseorder.getRejectedBy().getEmployeename();
		}

		if (purchaseorder.getVerifiedBy() != null) {
			this.verifiedBy = purchaseorder.getVerifiedBy().getId();
			this.verifierName = purchaseorder.getVerifiedBy().getEmployeename();
		}
		if (purchaseorder.getApprovedOn() != null)
			this.approved_on = dateutil.getDateToStringYYYYMMDD(purchaseorder.getApprovedOn());
		this.remarks = purchaseorder.getRemarks();
		this.approval_status = purchaseorder.getApprovalStatus();
		if (purchaseorder.getCessDate() != null)
			this.cess_date = dateutil.getDateToStringYYYYMMDD(purchaseorder.getCessDate());
		this.cess_no = purchaseorder.getCessNo();
		this.cess_amount = purchaseorder.getCessAmount();
		this.po_no_original = purchaseorder.getPoNoOriginal();
		this.cess_type = purchaseorder.getCessType();
		this.typeflag = purchaseorder.getTypeFlag();
		this.poOrderItems = purchaseorder.getPurchaseOrderItems() != null ? purchaseorder.getPurchaseOrderItems()
				.stream().map(field -> new PurchaseOrderItemsDto(field)).collect(Collectors.toList()) : null;

		this.purchasePaymentOption = purchaseorder.getPurchasePaymentOption() != null
				? purchaseorder.getPurchasePaymentOption().stream().map(field -> new PurchasePaymentDto(field)).collect(
						Collectors.toList())
				: null;

		this.proposalId = purchaseorder.getProposalId();
		if (purchaseorder.getDeliveryduedate() != null && purchaseorder.getDeliveryduedate() != null) {
			this.deliveryduedate = dateutil.getDateToStringYYYYMMDD(purchaseorder.getDeliveryduedate());
		}
		if (purchaseorder.getDept() != null) {
			this.dept = purchaseorder.getDept().getId();
		}
		if (purchaseorder.getBranch() != null) {
			this.branch = purchaseorder.getBranch().getId();
		}
		this.deliveryplant = purchaseorder.getDeliveryplant();
		this.partnercontribution = purchaseorder.getPartnercontribution();
		this.advanceType = purchaseorder.isAdvanceType();
	}
}
