package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.ProposalDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

@Table(name = "proposal")
public class Proposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 39 - Need Based, 40 - Formula Based, 41 - Project Based, 42 - Session Based
	@Column
	private Long category;

	@Column
	private String description;

	@Column
	private String name;

	@Column(name = "path")
	private String path;

	@Column
	private Boolean singlevendor;

	// 1- Pending, 2 - Complete, 3-Approved, 4-Expired, 5-Closed
	@Column
	private String status;

	@Column(name = "proposal_status")
	private String proposalStatus;

	// 0 - not active, 1 - active
	@Column
	private Long active;

	@Column
	private LocalDateTime approved_date;

	@Column
	private LocalDateTime valid_date;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column(name = "po_status")
	private String po_status;

	@Column(name = "authorizer_role")
	private Long authorizerRole;

	@OneToOne
	@JoinColumn(name = "approved_by", referencedColumnName = "id")
	private User approvedBy;

	@OneToOne
	@JoinColumn(name = "activated_by", referencedColumnName = "id")
	private User activatedBy;

	@Column
	private LocalDateTime activated_date;

	@OneToOne
	@JoinColumn(name = "verified_by1", referencedColumnName = "id")
	private User verifier1;

	@Column
	private LocalDateTime verifier1_date;
	@OneToOne
	@JoinColumn(name = "verified_by2", referencedColumnName = "id")
	private User verifier2;

	@Column
	private LocalDateTime verifier2_date;

	@OneToOne
	@JoinColumn(name = "verified_by3", referencedColumnName = "id")
	private User verifier3;

	@Column
	private LocalDateTime verifier3_date;

	@OneToOne
	@JoinColumn(name = "verified_by4", referencedColumnName = "id")
	private User validated1;

	@Column
	private LocalDateTime validated1_date;

	@OneToOne
	@JoinColumn(name = "verified_by5", referencedColumnName = "id")
	private User validated2;

	@Column
	private LocalDateTime validated2_date;

	@OneToOne
	@JoinColumn(name = "authorized_by", referencedColumnName = "id")
	private User authorizedBy;

	@Column
	private LocalDateTime authorized_date;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails dept;

	@OneToOne
	@JoinColumn(name = "rejected_by", referencedColumnName = "id")
	private User rejectedBy;

	@Column
	private LocalDateTime rejected_date;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@Column(name = "firm_id")
	private Long firmId;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column
	private LocalDateTime updated_date;


	public Proposal(ProposalDto proposal) {

		this.id = proposal.getId();
		this.name = proposal.getName();
		this.singlevendor = proposal.getSinglevendor();
		this.description = proposal.getDescription();
		this.category = proposal.getCategory();
		this.status = proposal.getStatus();
		this.firmId = proposal.getFirmId();
		if (proposal.getDept() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(proposal.getDept());
			this.dept = departmentDetails;
		}
		if (proposal.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(proposal.getBranch());
			this.branch = branch;
		}
		this.path = proposal.getPath();

	}



}
