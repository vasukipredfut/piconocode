package com.aria.dto;


import java.util.List;

import com.aria.model.Proposal;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProposalDto {

	private Long id;

	// 39 - Need Based, 40 - Formula Based, 41 - Project Based, 42 - Session Based

	private Long category;

	private String description;

	private String name;

	private Boolean singlevendor;

	private String status;

	private Long dept;

	private Long branch;

	private String approvedDate;

	private Long approvedBy;

	private String approverName;

	//private LocalDateTime valid_date;
	private String valid_date;

	private Long createdBy;

	private String createdByName;

	private Long active;

	private String authorizerRole;

	private Long activatedBy;

	private String activatedDate;

	private String activatorName;

	private Long verifier1;

	private String verifier1Name;
	private String verifier1Date;

	private Long verifier2;

	private String verifier2Name;
	private String verifier2Date;

	private Long verifier3;

	private String verifier3Name;
	private String verifier3Date;

	private Long validated1;

	private String validator1Name;
	private String validated1Date;

	private Long validated2;
	private String validated2Date;

	private String validator2Name;

	private Long authorizedBy;
	private String authorizedDate;

	private String authorizerName;

	private String created_date;

	private List<ProposalSpecDto> proposalSpecDtos;

	private String type;

	private Boolean flag;

	private String proposalStatus;

	private Long firmId;

	private String path;

	private Long rejectedBy;

	private String rejectedDate;

	private String rejecterName;

	private String po_status;
	private Company companyDetails;

	private Long updatedBy;
	private String updatedByName;

	private String updatedDate;

	private List<ProposalVendorDTO> proposalVendorDtos;

	private Boolean updateProposal;
	public ProposalDto(Proposal proposal) {

		DateUtil dateUtil = new DateUtil();
		this.id = proposal.getId();
		this.firmId = proposal.getFirmId();
		this.name = proposal.getName();
		this.description = proposal.getDescription();
		this.category = proposal.getCategory();
		this.created_date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getCreated_date());
		this.createdBy = proposal.getCreatedBy();
		this.status = proposal.getStatus();
		this.active = proposal.getActive();
		this.po_status = proposal.getPo_status();
		this.singlevendor = proposal.getSinglevendor();
		if (proposal.getApprovedBy() != null) {
			this.approvedBy = proposal.getApprovedBy().getId();
			this.approverName = proposal.getApprovedBy().getEmployeename();
			this.approvedDate = dateUtil.getLocalDateTimeToDateTimeString(proposal.getApproved_date());
		}

		if (proposal.getActivatedBy() != null) {
			this.activatedBy = proposal.getActivatedBy().getId();
			this.activatorName = proposal.getActivatedBy().getEmployeename();
			this.activatedDate = dateUtil.getLocalDateTimeToDateTimeString(proposal.getActivated_date());
		}

		if (proposal.getAuthorizedBy() != null) {
			this.authorizedBy = proposal.getAuthorizedBy().getId();
			this.authorizerName = proposal.getAuthorizedBy().getEmployeename();
			this.authorizedDate = dateUtil.getLocalDateTimeToDateTimeString(proposal.getAuthorized_date());
		}

		if (proposal.getVerifier1() != null) {
			this.verifier1 = proposal.getVerifier1().getId();
			this.verifier1Name = proposal.getVerifier1().getEmployeename();
			this.verifier1Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getVerifier1_date());
		}

		if (proposal.getVerifier2() != null) {
			this.verifier2 = proposal.getVerifier2().getId();
			this.verifier2Name = proposal.getVerifier2().getEmployeename();
			this.verifier2Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getVerifier2_date());
		}

		if (proposal.getVerifier3() != null) {
			this.verifier3 = proposal.getVerifier3().getId();
			this.verifier3Name = proposal.getVerifier3().getEmployeename();
			this.verifier3Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getVerifier3_date());
		}

		if (proposal.getValidated1() != null) {
			this.validated1 = proposal.getValidated1().getId();
			this.validator1Name = proposal.getValidated1().getEmployeename();
			this.validated1Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getValidated1_date());
		}

		if (proposal.getValidated2() != null) {
			this.validated1 = proposal.getValidated1().getId();
			this.validator1Name = proposal.getValidated1().getEmployeename();
			this.validated1Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getValidated1_date());
		}

		if (proposal.getValidated2() != null) {
			this.validated2 = proposal.getValidated2().getId();
			this.validator2Name = proposal.getValidated2().getEmployeename();
			this.validated2Date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getValidated2_date());
		}

		if (proposal.getRejectedBy() != null) {
			this.rejectedBy = proposal.getRejectedBy().getId();
			this.rejecterName = proposal.getRejectedBy().getEmployeename();
			this.rejectedDate = dateUtil.getLocalDateTimeToDateTimeString(proposal.getRejected_date());
		}

		if (proposal.getDept() != null) {
			this.dept = proposal.getDept().getId();
		}

		if (proposal.getBranch() != null) {
			this.branch = proposal.getBranch().getId();
		}

		this.proposalStatus = proposal.getProposalStatus();

		this.valid_date = dateUtil.getLocalDateTimeToDateTimeString(proposal.getValid_date());
		this.path = proposal.getPath();
		if (proposal.getAuthorizerRole() != null) {
			this.authorizerRole = (22 == proposal.getAuthorizerRole() ? "Plant Manager" : 63 == proposal.getAuthorizerRole() ? "Managing Director" : null);
		}
		this.updatedDate = dateUtil.getLocalDateTimeToDateTimeString(proposal.getUpdated_date());
		this.updatedBy = proposal.getUpdatedBy();
	}

}
