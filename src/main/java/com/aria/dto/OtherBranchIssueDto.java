package com.aria.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.IssueItems;
import com.aria.model.OtherBranchIssue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OtherBranchIssueDto {

	private Long id;

	private String gdcNo;
	private String docketNo;

	private String withInBranchReceiptIdString;

	private Long withInBranchReceiptId;

	private Long requestId;

	private Long issueId;


	private String requestFirmName;
	private Long requestFirmId;

	private String requestFrom;

	private String requestFromDepartment;

	private Long requestFromDepartmentId;

	private String requestFromBranch;

	private Long requestFromBranchId;

	private String issueFromDepartment;

	private Long issueFromDepartmentId;

	private String issueFromBranch;

	private Long issueFromBranchId;

	private String status;

	private String trnNo;

	private String issueReference;

	private String issueMode;

	private String deliveryByStaff;

	private Long deliveryByStaffId;

	private String transportMode;

	private String transportVehicleType;

	private String transportVehicleNo;

	private String dispatchOn;

	private String materialReturn;

	private String generateEWayBill;

	private Long createdBy;

	private Long updatedBy;
	
	private String updatedByUser;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<OtherBranchIssueItemsDTO> otherBranchIssueValue;

	private List<IssueItemsBriefDto> otherBranchIssueItemQuantity;

	private List<IssueItemsBriefDto> otherBranchIssueBriefValue;

	private String hubNo;

	private String issuedBy;

	public OtherBranchIssueDto(OtherBranchIssue otherBranchIssue) {
		this.id = otherBranchIssue.getId();
		this.withInBranchReceiptId = otherBranchIssue.getMaterialWithInBranchId();
		this.requestId = otherBranchIssue.getRequestId();
		this.issueId = otherBranchIssue.getIssueId();
		this.status = otherBranchIssue.getStatus();
		this.trnNo = otherBranchIssue.getTrnNo();
		if (otherBranchIssue.getStatus() != null && otherBranchIssue.getStatus().equalsIgnoreCase("Completed")) {
			this.docketNo = "DKT-" + otherBranchIssue.getId();
		}
		if (otherBranchIssue.getRequestBranch() != null) {
			this.requestFromBranch = otherBranchIssue.getRequestBranch().getBranchName();
			this.requestFromBranchId = otherBranchIssue.getRequestBranch().getId();
		}
		if (otherBranchIssue.getRequestDepartment() != null) {
			this.requestFromDepartment = otherBranchIssue.getRequestDepartment().getName();
			this.requestFromDepartmentId = otherBranchIssue.getRequestDepartment().getId();
		}
		if (otherBranchIssue.getIssueBranch() != null) {
			this.issueFromBranch = otherBranchIssue.getIssueBranch().getBranchName();
			this.issueFromBranchId = otherBranchIssue.getIssueBranch().getId();
		}
		if (otherBranchIssue.getIssueDepartment() != null) {
			this.issueFromDepartment = otherBranchIssue.getIssueDepartment().getName();
			this.issueFromDepartmentId = otherBranchIssue.getIssueDepartment().getId();
		}
		if (otherBranchIssue.getOrganisation() != null) {
			this.requestFirmName = otherBranchIssue.getOrganisation().getName();
			this.requestFirmId = otherBranchIssue.getOrganisation().getId();
		}
		this.issueReference = otherBranchIssue.getIssueReference();
		this.issueMode = otherBranchIssue.getIssueMode();
		this.transportMode = otherBranchIssue.getTransportMode();
		this.transportVehicleType = otherBranchIssue.getTransportVehicleType();
		this.transportVehicleNo = otherBranchIssue.getTransportVehicleNo();
		this.deliveryByStaffId = otherBranchIssue.getDelivery_user_id();
		this.generateEWayBill = otherBranchIssue.getGenerateEWayBill();
		this.createdBy = otherBranchIssue.getCreatedBy();
		this.updatedBy = otherBranchIssue.getUpdatedBy();
		this.created_date = otherBranchIssue.getCreated_date();
		this.updated_date = otherBranchIssue.getUpdated_date();
		
		this.otherBranchIssueValue  = otherBranchIssue.getOtherBranchIssueItems() != null ? otherBranchIssue.getOtherBranchIssueItems().stream().map(obj -> new OtherBranchIssueItemsDTO(obj)).collect(Collectors.toList()) : null;

		if(this.otherBranchIssueValue!=null && !this.otherBranchIssueValue.isEmpty()){
			List<IssueItemsBriefDto> issueItemsBriefQuantity = new ArrayList<>();
			Map<String, List<OtherBranchIssueItemsDTO>> issueItemMap =
					this.otherBranchIssueValue.stream().collect(Collectors.groupingBy(issueItem -> String.valueOf(issueItem.getItemId())+issueItem.getSelectLot()));
			calculateQuantity(issueItemsBriefQuantity, issueItemMap);
			this.otherBranchIssueItemQuantity = issueItemsBriefQuantity;
		}
	}

	private void calculateQuantity(List<IssueItemsBriefDto> issueItemsBriefQuantity, Map<String, List<OtherBranchIssueItemsDTO>> issueItemMap) {
		issueItemMap.forEach((k, value) -> {
			OtherBranchIssueItemsDTO issueItems = value.get(0);
			IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
			issueItemsBriefDto.setIssueItemId(issueItems.getIssueItemId());
			issueItemsBriefDto.setItemId(issueItems.getItemId());
			issueItemsBriefDto.setItemName(issueItems.getItemName());
			issueItemsBriefDto.setLot(issueItems.getSelectLot());
			issueItemsBriefDto.setRequestedQuantity(Double.parseDouble(issueItems.getRequestedQuantity()));
			double allocatedQuantity=0.0;
			double pendingQuantity=0.0;
			for(OtherBranchIssueItemsDTO item : value){

				double allottedQty = 0.0;
				double pendingQty = 0.0;
				if(item.getAllocatedQuantity() != null) {
					allottedQty = Double.parseDouble(item.getAllocatedQuantity());
				}
				if(item.getPendingQuantity() != null) {
					pendingQty = Double.parseDouble(item.getPendingQuantity());
				}
				allocatedQuantity = allocatedQuantity + allottedQty;
				pendingQuantity = pendingQuantity + pendingQty;
			}
			issueItemsBriefDto.setAllocatedQuantity(allocatedQuantity);
			issueItemsBriefDto.setPendingQuantity(pendingQuantity);
			issueItemsBriefQuantity.add(issueItemsBriefDto);
		});
	}
}
