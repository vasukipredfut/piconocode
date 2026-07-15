package com.aria.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.OtherBranchIssueItems;
import com.aria.model.WithInBranchDistribution;
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
public class WithInBranchDistributionDto {


    private Long id;

    private Long requestId;

    private Long issueId;


    private String issuePerson;

    private Long issuePersonId;

    private String docketNo;

    private String hupId;


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

    private String issueDate;

    private String issueDateString;


    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String trnNo;

    private List<OtherBranchIssueItems> withInBranchDistributionValue;
    
    private List<IssueItemsBriefDto> withInBranchIssueItemQuantity;
    
    private List<IssueItemsBriefDto> withInBranchIssueBriefValue;


    public WithInBranchDistributionDto(WithInBranchDistribution withInBranchDistribution) {

        this.id = withInBranchDistribution.getId();
        this.requestId = withInBranchDistribution.getRequestId();
        this.issueId = withInBranchDistribution.getIssueId();
        this.trnNo = withInBranchDistribution.getTrnNo();

        if (withInBranchDistribution.getIssueBy() != null) {
            this.issuePerson = withInBranchDistribution.getIssueBy().getEmployeename();
            this.issuePersonId = withInBranchDistribution.getIssueBy().getId();
        }
        if (withInBranchDistribution.getRequestBranch() != null) {
            this.requestFromBranch = withInBranchDistribution.getRequestBranch().getBranchName();
            this.requestFromBranchId = withInBranchDistribution.getRequestBranch().getId();
        }
		
        if (withInBranchDistribution.getRequestDepartment() != null) {
            this.requestFromDepartment = withInBranchDistribution.getRequestDepartment().getName();
            this.requestFromDepartmentId = withInBranchDistribution.getRequestDepartment().getId();
        }

        if (withInBranchDistribution.getIssueBranch() != null) {
            this.issueFromBranch = withInBranchDistribution.getIssueBranch().getBranchName();
            this.issueFromBranchId = withInBranchDistribution.getIssueBranch().getId();
        }
       
        if (withInBranchDistribution.getIssueDepartment() != null) {
            this.issueFromDepartment = withInBranchDistribution.getIssueDepartment().getName();
            this.issueFromDepartmentId = withInBranchDistribution.getIssueDepartment().getId();
        }
        if (withInBranchDistribution.getRequestOrg() != null) {
            this.requestFirmName = withInBranchDistribution.getRequestOrg().getName();
            this.requestFirmId = withInBranchDistribution.getRequestOrg().getId();
        }
        this.status = withInBranchDistribution.getStatus();
        this.docketNo = withInBranchDistribution.getDocketNo();
        this.hupId = withInBranchDistribution.getHupId();
        this.createdBy = withInBranchDistribution.getCreatedBy();
        this.updatedBy = withInBranchDistribution.getUpdatedBy();
        this.created_date = withInBranchDistribution.getCreated_date();
        if (withInBranchDistribution.getUpdated_date() != null) {
            this.updated_date = withInBranchDistribution.getUpdated_date();
        } else {
            this.updated_date = withInBranchDistribution.getCreated_date();
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.withInBranchDistributionValue = mapper.readValue(withInBranchDistribution.getWithinBranchDistributionValue(), new TypeReference<List<OtherBranchIssueItems>>(){});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(this.withInBranchDistributionValue!=null && !this.withInBranchDistributionValue.isEmpty()){
			List<IssueItemsBriefDto> issueItemsBriefQuantity = new ArrayList<>();
			Map<String, List<OtherBranchIssueItems>> issueItemMap =
					this.withInBranchDistributionValue.stream().collect(Collectors.groupingBy(issueItem -> String.valueOf(issueItem.getItemId())+issueItem.getSelectLot()));
			calculateQuantity(issueItemsBriefQuantity, issueItemMap);
			this.withInBranchIssueItemQuantity = issueItemsBriefQuantity;
		}
    }
        
    	private void calculateQuantity(List<IssueItemsBriefDto> issueItemsBriefQuantity, Map<String, List<OtherBranchIssueItems>> issueItemMap) {
    		issueItemMap.forEach((k, value) -> {
                OtherBranchIssueItems issueItems = value.get(0);
    			IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
    			issueItemsBriefDto.setIssueItemId(issueItems.getIssueItemId());
    			issueItemsBriefDto.setItemId(issueItems.getItemId());
    			issueItemsBriefDto.setItemName(issueItems.getItemName());
    			issueItemsBriefDto.setLot(issueItems.getSelectLot());
    			issueItemsBriefDto.setRequestedQuantity(Double.parseDouble(issueItems.getRequestedQuantity()));
    			double allocatedQuantity=0.0;
    			double pendingQuantity=0.0;
    			for(OtherBranchIssueItems item : value){
    				allocatedQuantity = allocatedQuantity + Double.parseDouble(item.getAllocatedQuantity());
    				pendingQuantity = pendingQuantity + Double.parseDouble(item.getPendingQuantity());
    			}
    			issueItemsBriefDto.setAllocatedQuantity(allocatedQuantity);
    			issueItemsBriefDto.setPendingQuantity(pendingQuantity);
    			issueItemsBriefQuantity.add(issueItemsBriefDto);
    		});
    }
    
}
