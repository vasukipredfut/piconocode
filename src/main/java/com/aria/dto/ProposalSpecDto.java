package com.aria.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.ProposalSpec;

import jakarta.persistence.Column;


@Getter
@Setter
@NoArgsConstructor
public class ProposalSpecDto {

	
	private Long id;
	
	
	private Long proposal_ref_id;
	
	
	private Long spectype;
	
		
	private Long specid;

	private SpecificationDto specificationDto;
	
	
	private Boolean packing_product;

	
	private Double approved_price;
	

	private Long createdBy;

	
	private LocalDateTime created_date;

	private List<ProposalVendorSpecDto> proposalVendorSpec;

	private Boolean active;


	public ProposalSpecDto(ProposalSpec proposalspec) {
	
		
		this.id=proposalspec.getId();
		this.approved_price=proposalspec.getApproved_price();
		this.packing_product=proposalspec.getPacking_product();
		this.proposal_ref_id=proposalspec.getProposal_ref_id();
		this.specid=proposalspec.getSpecid();
		this.spectype=proposalspec.getSpectype();
		this.created_date=proposalspec.getCreated_date();
		this.createdBy=proposalspec.getCreatedBy();
		this.active=proposalspec.getActive();

	}

}
