package com.aria.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.IssueItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


import com.aria.model.IntraBranchReceipt;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class IntraBranchReceiptDto {

	private Long id;

	private String intraBranchReceiptId;

	private Long requestId;

	private Long issueId;


	private String issuePerson;

	private Long issuePersonId;

	private String docketNo;

	private String hupId;

	//private String requestFrom;

	private String requestFromDepartment;

	private Long requestFromDepartmentId;

	private String requestFromBranch;

	private Long requestFromBranchId;

	//private String requestFromFirm;

	//private Long requestFromFirmId;


	private String issueFromDepartment;

	private Long issueFromDepartmentId;

	private String issueFromBranch;

	private Long issueFromBranchId;

	private String issueFromFirm;

	private Long issueFromFirmId;


	//private String requestDate;

	//private String requestDateString;


	private String receivedOnDate;

	private String receivedOnDateString;


	private String entryOnDate;

	private String entryOnDateString;


	private String status;


	private String trnNo;

	private String issueDate;

	private String issueDateString;

	private String receivedVehicleNo;


	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<IssueItems> intraBranchReceiptValue;


	public IntraBranchReceiptDto(IntraBranchReceipt intraBranchReceipt) {

		this.id = intraBranchReceipt.getId();
		this.trnNo =  intraBranchReceipt.getTrnNo();
		this.requestId = intraBranchReceipt.getRequestId();
		this.issueId = intraBranchReceipt.getIssueId();

		if (intraBranchReceipt.getIssueBy() != null) {
			this.issuePerson = intraBranchReceipt.getIssueBy().getEmployeename();
			this.issuePersonId = intraBranchReceipt.getIssueBy().getId();
		}
		if (intraBranchReceipt.getRequestBranch() != null) {
			this.requestFromBranch = intraBranchReceipt.getRequestBranch().getBranchName();
			this.requestFromBranchId = intraBranchReceipt.getRequestBranch().getId();
		}
		/*if (intraBranchReceipt.getRequestOrg() != null) {
			this.requestFromFirm = intraBranchReceipt.getRequestOrg().getName();
			this.requestFromFirmId = intraBranchReceipt.getRequestOrg().getId();
		}*/
		if (intraBranchReceipt.getRequestDepartment() != null) {
			this.requestFromDepartment = intraBranchReceipt.getRequestDepartment().getName();
			this.requestFromDepartmentId = intraBranchReceipt.getRequestDepartment().getId();
		}

		if (intraBranchReceipt.getIssueBranch() != null) {
			this.issueFromBranch = intraBranchReceipt.getIssueBranch().getBranchName();
			this.issueFromBranchId = intraBranchReceipt.getIssueBranch().getId();
		}
		if (intraBranchReceipt.getIssueOrg() != null) {
			this.issueFromFirm = intraBranchReceipt.getIssueOrg().getName();
			this.issueFromFirmId = intraBranchReceipt.getIssueOrg().getId();
		}
		if (intraBranchReceipt.getIssueDepartment() != null) {
			this.issueFromDepartment = intraBranchReceipt.getIssueDepartment().getName();
			this.issueFromDepartmentId = intraBranchReceipt.getIssueDepartment().getId();
		}

		this.status = intraBranchReceipt.getStatus();
		this.docketNo = intraBranchReceipt.getDocketNo();
		this.hupId = intraBranchReceipt.getHupId();
		this.receivedVehicleNo = intraBranchReceipt.getReceivedVehicleNo();
		this.createdBy = intraBranchReceipt.getCreatedBy();
		this.updatedBy = intraBranchReceipt.getUpdatedBy();
		this.created_date = intraBranchReceipt.getCreated_date();
		if (intraBranchReceipt.getUpdated_date() != null) {
			this.updated_date = intraBranchReceipt.getUpdated_date();
		} else {
			this.updated_date = intraBranchReceipt.getCreated_date();
		}
		
     
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.intraBranchReceiptValue = mapper.readValue(intraBranchReceipt.getIntraBranchReceiptValue(), new TypeReference<List<IssueItems>>(){});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
}
