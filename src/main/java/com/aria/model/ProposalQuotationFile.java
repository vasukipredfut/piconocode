package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "proposal_quotation_file")
public class ProposalQuotationFile {

//	@EmbeddedId
//	private ProposalQuotationEmbedded proposalQuotationEmbedded = new ProposalQuotationEmbedded();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "proposal_id")
	private Long proposalId;

	@Column
	private String filename;

}
