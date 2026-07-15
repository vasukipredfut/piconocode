package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.ProposalVendorSpecDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

@Table(name = "proposal_vendor_spec")
public class ProposalVendorSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long proposal_ref_id;

    @Column
    private Long proposal_spec_id;

    @Column
    private Long vendor_id;

    @Column
    private String quotation_no;

    @Column
    private Long delivery_in_days;

    @Column
    private Double basic_price;

    @Column
    private Double proposal_tax;

    @Column
    private Double cgst;

    @Column
    private Double sgst;

    @Column
    private Double igst;

    @Column
    private String packing_frieght;

    @Column
    private Double percentage_split;

    @Column
    private String path;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private Date quotation_date;

    @Column
    private Double packing_forward;

    @Column
    private String status;

    @Column
    private boolean recommended_vendor;

    @Column
    private boolean active;

    @Column
    @Temporal(TemporalType.DATE)
    private Date expiry_date;


    @Column
    private String po_validity_request;


    @Column
    private Double net_approved_qty;

    @Column
    private Double net_approved_price;

    @Column
    private String lot ;

	@Column(name = "update_by")
	private Long updatedBy;

	@Column
	private LocalDateTime updated_date;

    @Column(updatable = false)
    private int active_flag = 1;  // earms active_flag 1 - active 0 - inactive

    public ProposalVendorSpec(ProposalVendorSpecDto proposalVendorSpec) {
        DateUtil dateUtil = new DateUtil();
        this.id = proposalVendorSpec.getId();
        this.basic_price = proposalVendorSpec.getBasic_price();
        this.cgst = proposalVendorSpec.getCgst();
        this.igst = proposalVendorSpec.getIgst();
        this.sgst = proposalVendorSpec.getSgst();
        this.packing_frieght = proposalVendorSpec.getPacking_frieght();
        this.delivery_in_days = proposalVendorSpec.getDelivery_in_days();
        this.percentage_split = proposalVendorSpec.getPercentage_split();
        this.proposal_ref_id = proposalVendorSpec.getProposal_ref_id();
        this.proposal_spec_id = proposalVendorSpec.getProposal_spec_id();
        this.vendor_id = proposalVendorSpec.getVendor_id();
        this.proposal_tax = proposalVendorSpec.getProposal_tax();
        this.quotation_no = proposalVendorSpec.getQuotation_no();
        this.path = proposalVendorSpec.getPath();
        if (proposalVendorSpec.getQuotation_date() != null) {
            this.quotation_date = dateUtil.getStringTODateWk(proposalVendorSpec.getQuotation_date());
        }
        this.packing_forward = proposalVendorSpec.getPacking_forward();
        this.status = proposalVendorSpec.getStatus();
        this.recommended_vendor = proposalVendorSpec.isRecommended_vendor();
        this.active = proposalVendorSpec.isActive();
        if (proposalVendorSpec.getExpiryDate() != null) {
            this.expiry_date = dateUtil.getStringTODateWk(proposalVendorSpec.getExpiryDate());
        }
        this.po_validity_request = proposalVendorSpec.getPoValidityRequest();
        this.net_approved_qty = proposalVendorSpec.getNetApprovedQty();
        this.net_approved_price = proposalVendorSpec.getNetApprovedPrice();
        this.lot = proposalVendorSpec.getLot();
    }




}
