package com.aria.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.OtherBranchIssueItems;
import com.aria.model.OtherBranchReceipt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OtherBranchReceiptDto {

	private Long id;

	private Long requestId;

	private String requestName;

	private String requestIdString;

	private Long issueId;

	private String docketNo;

	private String hupId;

	private String issuePerson;

	private Long issuePersonId;

	private String requestFromDepartment;

	private Long requestFromDepartmentId;

	private String requestFromBranch;

	private Long requestFromBranchId;

	private String requestFirmName;

	private Long requestFirmId;

	private String issueFromDepartment;

	private Long issueFromDepartmentId;

	private String issueFromBranch;

	private Long issueFromBranchId;

	private String receivedOnDate;

	private String receivedOnDateString;

	private String status;

	private String trnNo;
	private String issueDate;

	private String issueDateString;

	private Long createdBy;

	private Long receivedBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<OtherBranchIssueItems> otherBranchReceiptValue;

	private List<IssueItemsBriefDto> otherBranchReceiptQuantity;

	private List<IssueItemsBriefDto> otherBranchReceiptBriefValue;

	public OtherBranchReceiptDto(OtherBranchReceipt otherBranchReceipt) {

		this.id = otherBranchReceipt.getId();
		this.requestId = otherBranchReceipt.getRequestId();
		this.requestIdString = "RQ-" + otherBranchReceipt.getRequestId();
		this.issueId = otherBranchReceipt.getIssueId();

		if (otherBranchReceipt.getIssueBy() != null) {
			this.issuePerson = otherBranchReceipt.getIssueBy().getEmployeename();
			this.issuePersonId = otherBranchReceipt.getIssueBy().getId();
		}
		if (otherBranchReceipt.getRequestBranch() != null) {
			this.requestFromBranch = otherBranchReceipt.getRequestBranch().getBranchName();
			this.requestFromBranchId = otherBranchReceipt.getRequestBranch().getId();
		}
		if (otherBranchReceipt.getRequestOrg() != null) {
			this.requestFirmName = otherBranchReceipt.getRequestOrg().getName();
			this.requestFirmId = otherBranchReceipt.getRequestOrg().getId();
		}
		if (otherBranchReceipt.getRequestDepartment() != null) {
			this.requestFromDepartment = otherBranchReceipt.getRequestDepartment().getName();
			this.requestFromDepartmentId = otherBranchReceipt.getRequestDepartment().getId();
		}
		if (otherBranchReceipt.getIssueBranch() != null) {
			this.issueFromBranch = otherBranchReceipt.getIssueBranch().getBranchName();
			this.issueFromBranchId = otherBranchReceipt.getIssueBranch().getId();
		}
		if (otherBranchReceipt.getIssueDepartment() != null) {
			this.issueFromDepartment = otherBranchReceipt.getIssueDepartment().getName();
			this.issueFromDepartmentId = otherBranchReceipt.getIssueDepartment().getId();
		}
		if (otherBranchReceipt.getReceivedBy() != null) {
			this.receivedBy = otherBranchReceipt.getReceivedBy().getId();
		}

		this.status = otherBranchReceipt.getStatus();
		this.trnNo = otherBranchReceipt.getTrnNo();
		this.docketNo = otherBranchReceipt.getDocketNo();
		this.hupId = otherBranchReceipt.getHupId();
		this.createdBy = otherBranchReceipt.getCreatedBy();
		this.updatedBy = otherBranchReceipt.getUpdatedBy();
		this.created_date = otherBranchReceipt.getCreated_date();
		if (otherBranchReceipt.getUpdated_date() != null) {
			this.updated_date = otherBranchReceipt.getUpdated_date();
		} else {
			this.updated_date = otherBranchReceipt.getCreated_date();
		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			this.otherBranchReceiptValue = mapper.readValue(otherBranchReceipt.getOtherBranchReceiptValue(), new TypeReference<List<OtherBranchIssueItems>>() {});

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (this.otherBranchReceiptValue != null && !this.otherBranchReceiptValue.isEmpty()) {
			List<IssueItemsBriefDto> issueItemsBriefQuantity = new ArrayList<>();
			Map<String, List<OtherBranchIssueItems>> issueItemMap = this.otherBranchReceiptValue.stream().collect(Collectors
					.groupingBy(issueItem -> String.valueOf(issueItem.getItemId()) + issueItem.getSelectLot()));
			calculateQuantity(issueItemsBriefQuantity, issueItemMap);
			this.otherBranchReceiptQuantity = issueItemsBriefQuantity;
		}
	}

	private void calculateQuantity(List<IssueItemsBriefDto> issueItemsBriefQuantity,
			Map<String, List<OtherBranchIssueItems>> issueItemMap) {
		issueItemMap.forEach((k, value) -> {
			OtherBranchIssueItems issueItems = value.get(0);
			IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
			issueItemsBriefDto.setIssueItemId(issueItems.getIssueItemId());
			issueItemsBriefDto.setItemId(issueItems.getItemId());
			issueItemsBriefDto.setItemName(issueItems.getItemName());
			issueItemsBriefDto.setLot(issueItems.getSelectLot());
			issueItemsBriefDto.setRequestedQuantity(Double.parseDouble(issueItems.getRequestedQuantity()));
			double allocatedQuantity = 0.0;
			double pendingQuantity = 0.0;
			for (OtherBranchIssueItems item : value) {
				allocatedQuantity = allocatedQuantity + Double.parseDouble(item.getAllocatedQuantity());
				pendingQuantity = pendingQuantity + Double.parseDouble(item.getPendingQuantity());
			}
			issueItemsBriefDto.setAllocatedQuantity(allocatedQuantity);
			issueItemsBriefDto.setPendingQuantity(pendingQuantity);
			issueItemsBriefQuantity.add(issueItemsBriefDto);
		});
	}
}