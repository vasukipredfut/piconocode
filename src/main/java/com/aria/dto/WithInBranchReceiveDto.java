package com.aria.dto;


import com.aria.model.IssueItems;
import com.aria.model.WithInBranchReceive;
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
public class WithInBranchReceiveDto {

    private Long id;

    private String withInBranchReceiveId;

    private Long requestId;

    private String requestName;

    private Long issueId;

    private String issuePerson;

    private Long issuePersonId;

    private String requestFrom;

    private String requestFromDepartment;

    private Long requestFromDepartmentId;

    private String requestFromBranch;

    private Long requestFromBranchId;

    private String requestFromFirm;

    private Long requestFromFirmId;


    private String issueFromDepartment;

    private Long issueFromDepartmentId;

    private String issueFromBranch;

    private Long issueFromBranchId;

    private String issueFromFirm;

    private Long issueFromFirmId;


    private String requestDate;

    private String requestDateString;


    private String receivedOnDate;

    private String receivedOnDateString;


    private String entryOnDate;

    private String entryOnDateString;


    private String status;

    private String trnNo;

    private String issueDate;

    private String issueDateString;


    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String receiveUserName;
    private Long receiveUserId;

    private List<WithInBranchReceiveItemDto> withInBranchReceiveItems;

	private List<WithInBranchReceiveBriefItemsDto> withInBranchReceiveItemQuantity;


    public WithInBranchReceiveDto(WithInBranchReceive withInBranchReceive) {

        this.id = withInBranchReceive.getId();
        this.trnNo =  withInBranchReceive.getTrnNo();
        this.requestId = withInBranchReceive.getRequestId();
        this.issueId = withInBranchReceive.getIssueId();

        if (withInBranchReceive.getIssueBy() != null) {
            this.issuePerson = withInBranchReceive.getIssueBy().getEmployeename();
            this.issuePersonId = withInBranchReceive.getIssueBy().getId();
        }
        if (withInBranchReceive.getRequestBranch() != null) {
            this.requestFromBranch = withInBranchReceive.getRequestBranch().getBranchName();
            this.requestFromBranchId = withInBranchReceive.getRequestBranch().getId();
        }
        if (withInBranchReceive.getRequestOrg() != null) {
            this.requestFromFirm = withInBranchReceive.getRequestOrg().getName();
            this.requestFromFirmId = withInBranchReceive.getRequestOrg().getId();
        }
        if (withInBranchReceive.getRequestDepartment() != null) {
            this.requestFromDepartment = withInBranchReceive.getRequestDepartment().getName();
            this.requestFromDepartmentId = withInBranchReceive.getRequestDepartment().getId();
        }

        if (withInBranchReceive.getIssueBranch() != null) {
            this.issueFromBranch = withInBranchReceive.getIssueBranch().getBranchName();
            this.issueFromBranchId = withInBranchReceive.getIssueBranch().getId();
        }
        if (withInBranchReceive.getIssueOrg() != null) {
            this.issueFromFirm = withInBranchReceive.getIssueOrg().getName();
            this.issueFromFirmId = withInBranchReceive.getIssueOrg().getId();
        }
        if (withInBranchReceive.getIssueDepartment() != null) {
            this.issueFromDepartment = withInBranchReceive.getIssueDepartment().getName();
            this.issueFromDepartmentId = withInBranchReceive.getIssueDepartment().getId();
        }

        if (withInBranchReceive.getReceivedBy() != null) {
            this.receiveUserName = withInBranchReceive.getReceivedBy().getEmployeename();
            this.receiveUserId = withInBranchReceive.getReceivedBy().getId();
        }

        this.status = withInBranchReceive.getStatus();
        this.createdBy = withInBranchReceive.getCreatedBy();
        this.updatedBy = withInBranchReceive.getUpdatedBy();
        this.created_date = withInBranchReceive.getCreated_date();
        this.updated_date = withInBranchReceive.getUpdated_date();

        this.withInBranchReceiveItems = withInBranchReceive.getWithInBranchReceiveItems() != null ? withInBranchReceive.getWithInBranchReceiveItems()
                .stream().map(obj -> new WithInBranchReceiveItemDto(obj)).collect(Collectors.toList()) : null;

        if(this.withInBranchReceiveItems!=null && !this.withInBranchReceiveItems.isEmpty()){
			List<WithInBranchReceiveBriefItemsDto> withInBranchReceiveItemQuantity = new ArrayList<>();
			Map<Object, List<WithInBranchReceiveItemDto>> issueItemMap =
					this.withInBranchReceiveItems.stream().collect(Collectors.groupingBy(issueItem -> String.valueOf(issueItem.getItemId())+issueItem.getSelectLot()));
			calculateQuantity1(withInBranchReceiveItemQuantity, issueItemMap);
			this.withInBranchReceiveItemQuantity = withInBranchReceiveItemQuantity;
		}
    }
     public WithInBranchReceiveDto(WithInBranchReceive withInBranchReceive, List<IssueItemsValuesDto> issueItemsList) {

        this.id = withInBranchReceive.getId();
        this.trnNo =  withInBranchReceive.getTrnNo();
        this.requestId = withInBranchReceive.getRequestId();
        this.issueId = withInBranchReceive.getIssueId();

        if (withInBranchReceive.getIssueBy() != null) {
            this.issuePerson = withInBranchReceive.getIssueBy().getEmployeename();
            this.issuePersonId = withInBranchReceive.getIssueBy().getId();
        }
        if (withInBranchReceive.getRequestBranch() != null) {
            this.requestFromBranch = withInBranchReceive.getRequestBranch().getBranchName();
            this.requestFromBranchId = withInBranchReceive.getRequestBranch().getId();
        }
        if (withInBranchReceive.getRequestOrg() != null) {
            this.requestFromFirm = withInBranchReceive.getRequestOrg().getName();
            this.requestFromFirmId = withInBranchReceive.getRequestOrg().getId();
        }
        if (withInBranchReceive.getRequestDepartment() != null) {
            this.requestFromDepartment = withInBranchReceive.getRequestDepartment().getName();
            this.requestFromDepartmentId = withInBranchReceive.getRequestDepartment().getId();
        }

        if (withInBranchReceive.getIssueBranch() != null) {
            this.issueFromBranch = withInBranchReceive.getIssueBranch().getBranchName();
            this.issueFromBranchId = withInBranchReceive.getIssueBranch().getId();
        }
        if (withInBranchReceive.getIssueOrg() != null) {
            this.issueFromFirm = withInBranchReceive.getIssueOrg().getName();
            this.issueFromFirmId = withInBranchReceive.getIssueOrg().getId();
        }
        if (withInBranchReceive.getIssueDepartment() != null) {
            this.issueFromDepartment = withInBranchReceive.getIssueDepartment().getName();
            this.issueFromDepartmentId = withInBranchReceive.getIssueDepartment().getId();
        }

        if (withInBranchReceive.getReceivedBy() != null) {
            this.receiveUserName = withInBranchReceive.getReceivedBy().getEmployeename();
            this.receiveUserId = withInBranchReceive.getReceivedBy().getId();
        }

        this.status = withInBranchReceive.getStatus();
        this.createdBy = withInBranchReceive.getCreatedBy();
        this.updatedBy = withInBranchReceive.getUpdatedBy();
        this.created_date = withInBranchReceive.getCreated_date();
        this.updated_date = withInBranchReceive.getUpdated_date();

        this.withInBranchReceiveItems = withInBranchReceive.getWithInBranchReceiveItems() != null ? withInBranchReceive.getWithInBranchReceiveItems()
                .stream().map(obj -> new WithInBranchReceiveItemDto(obj)).collect(Collectors.toList()) : null;

        if(issueItemsList !=null && !issueItemsList.isEmpty()){
			List<WithInBranchReceiveBriefItemsDto> withInBranchReceiveItemQuantity = new ArrayList<>();
			Map<Object, List<IssueItemsValuesDto>> issueItemMap =
                    issueItemsList.stream().collect(Collectors.groupingBy(issueItem -> String.valueOf(issueItem.getItemId())+issueItem.getSelectLot()));
			calculateQuantity(withInBranchReceiveItemQuantity, issueItemMap);
			this.withInBranchReceiveItemQuantity = withInBranchReceiveItemQuantity;
		}
    }

    private void calculateQuantity1(List<WithInBranchReceiveBriefItemsDto> withInBranchReceiveItemQuantity,
			Map<Object, List<WithInBranchReceiveItemDto>> issueItemMap) {
    	issueItemMap.forEach((k, value) -> {
			WithInBranchReceiveItemDto issueItems = value.get(0);
			WithInBranchReceiveBriefItemsDto issueItemsBriefDto = new WithInBranchReceiveBriefItemsDto();
			issueItemsBriefDto.setItemId(issueItems.getItemId());
			issueItemsBriefDto.setItemName(issueItems.getItemName());
			issueItemsBriefDto.setLot(issueItems.getSelectLot());
			double allocatedQuantity=0.0;
			double pendingQuantity=0.0;
			double requestedQuantity = 0.0;
			for(WithInBranchReceiveItemDto item : value){
				allocatedQuantity = allocatedQuantity + Double.parseDouble(item.getQuantity());
				requestedQuantity = requestedQuantity + Double.parseDouble(item.getQuantity());
			}
			issueItemsBriefDto.setAllocatedQuantity(allocatedQuantity);
			issueItemsBriefDto.setPendingQuantity(pendingQuantity);
			issueItemsBriefDto.setRequestedQuantity(requestedQuantity);
			withInBranchReceiveItemQuantity.add(issueItemsBriefDto);
		});
	}

    private void calculateQuantity(List<WithInBranchReceiveBriefItemsDto> withInBranchReceiveItemQuantity,
                                   Map<Object, List<IssueItemsValuesDto>> issueItemMap) {
        issueItemMap.forEach((k, value) -> {
            IssueItemsValuesDto issueItems = value.get(0);
            WithInBranchReceiveBriefItemsDto issueItemsBriefDto = new WithInBranchReceiveBriefItemsDto();
            issueItemsBriefDto.setItemId(issueItems.getItemId());
            issueItemsBriefDto.setItemName(issueItems.getItemName());
            issueItemsBriefDto.setLot(issueItems.getSelectLot());
            double allocatedQuantity = 0.0;
            double pendingQuantity = 0.0;
            double requestedQuantity = 0.0;
            if(issueItems.getPendingQuantity() != null) {
                pendingQuantity = Double.valueOf(issueItems.getPendingQuantity());
            }
            if(issueItems.getRequestedQuantity() != null) {
                requestedQuantity = Double.valueOf(issueItems.getRequestedQuantity());
            }
            for (IssueItemsValuesDto item : value) {
                allocatedQuantity = allocatedQuantity + Double.parseDouble(item.getAllocatedQuantity());
            }
            issueItemsBriefDto.setAllocatedQuantity(allocatedQuantity);
            issueItemsBriefDto.setPendingQuantity(pendingQuantity);
            issueItemsBriefDto.setRequestedQuantity(requestedQuantity);
            withInBranchReceiveItemQuantity.add(issueItemsBriefDto);
        });
    }

}
