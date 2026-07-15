package com.aria.dto;


import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.ProposalVendorSpec;



@Getter
@Setter
@NoArgsConstructor
public class ProposalVendorSpecDto {

	
	private Long id;
	

	private Long proposal_ref_id;
	
	
	private Long proposal_spec_id;
	
	
	
	private Long vendor_id;

	private String vendor_name;

	private PartyDetails vendorDetails;

	
	private String quotation_no;

	
	private Long delivery_in_days;
	

	
	private Double basic_price;

	
	private Double proposal_tax;
	
	
	private Double cgst;
	
	private String path;
	
	private Double sgst;
	
	
	private Double igst;
	
	
	private String packing_frieght;
	
	
	private Double percentage_split;

	private Long createdBy;

	
	private LocalDateTime created_date;
	
	private String quotation_date;

	private Double packing_forward;

	private String status;
	private boolean recommended_vendor;
	private boolean active;

	private String expiryDate;

	private String poValidityRequest;

	private Double netApprovedQty;

	private Double netApprovedPrice;

	private String lot ;

	private String poCreatedQty ;

	public ProposalVendorSpecDto(ProposalVendorSpec proposalVendorSpec) {
		DateUtil dateUtil = new DateUtil();
		this.id=proposalVendorSpec.getId();
		this.basic_price=proposalVendorSpec.getBasic_price();
		this.cgst=proposalVendorSpec.getCgst();
		this.delivery_in_days=proposalVendorSpec.getDelivery_in_days();
		this.igst=proposalVendorSpec.getIgst();
		this.sgst=proposalVendorSpec.getSgst();
		this.packing_frieght=proposalVendorSpec.getPacking_frieght();
		this.path=proposalVendorSpec.getPath();
		this.percentage_split=proposalVendorSpec.getPercentage_split();
		this.proposal_ref_id=proposalVendorSpec.getProposal_ref_id();
		this.proposal_spec_id=proposalVendorSpec.getProposal_spec_id();
		this.proposal_tax=proposalVendorSpec.getProposal_tax();
		this.quotation_no=proposalVendorSpec.getQuotation_no();
		this.vendor_id=proposalVendorSpec.getVendor_id();
		this.created_date=proposalVendorSpec.getCreated_date();
		this.createdBy=proposalVendorSpec.getCreatedBy();
		this.path=proposalVendorSpec.getPath();
		if(proposalVendorSpec.getQuotation_date() != null) {
			this.quotation_date = dateUtil.getDateTOStringWk(proposalVendorSpec.getQuotation_date());
		}
		this.packing_forward = proposalVendorSpec.getPacking_forward();
		this.status = proposalVendorSpec.getStatus();
		this.recommended_vendor = proposalVendorSpec.isRecommended_vendor();
		this.active = proposalVendorSpec.isActive();
		if(proposalVendorSpec.getExpiry_date() != null) {
			this.expiryDate = dateUtil.getDateTOStringWk(proposalVendorSpec.getExpiry_date());
		}
		this.poValidityRequest = proposalVendorSpec.getPo_validity_request();
		this.netApprovedPrice = proposalVendorSpec.getNet_approved_price();
		this.netApprovedQty = proposalVendorSpec.getNet_approved_qty();
		this.lot = proposalVendorSpec.getLot();
	}

}
