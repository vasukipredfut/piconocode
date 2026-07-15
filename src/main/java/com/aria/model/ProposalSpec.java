package com.aria.model;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import com.aria.dto.ProposalSpecDto;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "proposal_spec")
public class ProposalSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private Long proposal_ref_id;
	
	@Column
	private Long spectype;
	
	
	@Column
	private Long specid;
	
	@Column
	private Boolean packing_product;

	@Column
	private Double approved_price;


	@Column(name = "created_by", updatable = false)
	private Long createdBy;


	@Column(updatable = false)
	private LocalDateTime created_date;


	@Column
	private Boolean active;


	@Column(name = "update_by")
	private Long updatedBy;


	@Column
	private LocalDateTime updated_date;



	@Column(updatable = false)
	private int active_flag = 1;  // earms active_flag 1 - active 0 - inactive


	public ProposalSpec(ProposalSpecDto proposalSpec) {
	
		this.id = proposalSpec.getId();
		this.proposal_ref_id=proposalSpec.getProposal_ref_id();
		this.spectype=proposalSpec.getSpectype();
		this.specid=proposalSpec.getSpecid();
		this.packing_product=proposalSpec.getPacking_product();
		this.approved_price=proposalSpec.getApproved_price();
		this.active = proposalSpec.getActive();

	}



}
