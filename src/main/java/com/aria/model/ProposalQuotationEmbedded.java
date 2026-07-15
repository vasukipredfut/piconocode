package com.aria.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ProposalQuotationEmbedded implements Serializable {

	@Column(name = "proposal_id")
	private Long proposal_id;

	@Column(name = "vendor_id")
	private Long vendor_id;

	public ProposalQuotationEmbedded(Long proposal_id, Long vendor_id) {

		this.proposal_id = proposal_id;
		this.vendor_id = vendor_id;
	}

}
