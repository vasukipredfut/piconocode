
package com.aria.dto;


import com.aria.dao.BinStockRepository;
import com.aria.dao.BranchRepository;
import com.aria.dao.IssueItemsRepository;
import com.aria.dao.ProductMasterRepository;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class MaterialReciptDto {

    private Long id;

    private String requestId;

    private String requestName;

    private String requestDescription;

    private Long requestById;

    private String requestBy;

    private String requestFrom;

    private String requestFromDepartment;

    private Long requestFromDepartmentId;

    private String requestFromBranch;

    private Long requestFromBranchId;

    private String requestFromBranchAddress;

    private String requestFromBranchPincode;
    private String requestFromBranchState;

    private String requestToBranch;

    private Long requestToBranchId;

    private String requestToBranchAddress;
    private String requestToBranchState;

    private String requestToBranchPincode;

    private String requestFromFirm;
    private String requestFromFirmAddress;
    private String requestFromFirmState;
    private String requestFromFirmCity;
    private String requestFromFirmGSTNo;

    private Long requestFromFirmId;

    private String requestDate;

    private String status;

    private int active;

    private String vehicleNumber;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Double totalAmount;
    private String gdcNo;

    private List<ReceiptItemDto> requestItems;

    private List<OtherBranchIssueItems> otherBranchIssueValue;

    private List<IssueItemsBriefDto> otherBranchIssueItemQuantity;

    private List<IssueItemsBriefDto> otherBranchIssueBriefValue;

    public MaterialReciptDto(OtherBranchIssue otherBranchIssue, MaterialRequest materialRequest, Branch branch, BranchRepository branchRepository, ProductMasterRepository productMasterRepository, IssueItemsRepository issueItemsRepository, BinStockRepository binStockRepository, Long issueId) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = "RQ - " + materialRequest.getId();
        this.requestName = materialRequest.getRequestDescription();

        if (materialRequest.getRequestBy() != null) {
            this.requestById = materialRequest.getRequestBy().getId();
            this.requestBy = materialRequest.getRequestBy().getEmployeename();
            if (branch != null) {
                this.requestFrom = branch.getBranchName();
            }
        }
        if (materialRequest.getBranch() != null) {
            this.requestFromBranch = materialRequest.getBranch().getBranchName();
            this.requestFromBranchId = materialRequest.getBranch().getId();
            this.requestFromBranchAddress = materialRequest.getBranch().getAddress();
            this.requestFromBranchPincode = materialRequest.getBranch().getPincode();
            this.requestFromBranchState = materialRequest.getBranch().getState();

        }
        if (materialRequest.getOrganisation() != null) {
            this.requestFromFirm = materialRequest.getOrganisation().getName();
            this.requestFromFirmAddress = materialRequest.getOrganisation().getAddress();
            this.requestFromFirmState = materialRequest.getOrganisation().getState();
            this.requestFromFirmCity = materialRequest.getOrganisation().getCity();
            this.requestFromFirmId = materialRequest.getOrganisation().getId();
            this.requestFromFirmGSTNo = materialRequest.getOrganisation().getGST();
        }
        if (materialRequest.getDepartment() != null) {
            this.requestFromDepartment = materialRequest.getDepartment().getName();
            this.requestFromDepartmentId = materialRequest.getDepartment().getId();
        }
        this.status = materialRequest.getStatus();
        this.requestDescription = materialRequest.getRequestDescription();
        this.createdBy = materialRequest.getCreatedBy();
        this.updatedBy = materialRequest.getUpdatedBy();
        this.created_date = materialRequest.getCreated_date();
        this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());
        if (materialRequest.getUpdated_date() != null) {
            this.updated_date = materialRequest.getUpdated_date();
        } else {
            this.updated_date = materialRequest.getCreated_date();
        }
        this.requestItems = materialRequest.getRequestItems() != null ? materialRequest
                .getRequestItems().stream().map(obj -> new ReceiptItemDto(obj, productMasterRepository, issueItemsRepository, issueId, binStockRepository)).collect(Collectors.toList()) : null;

        if (this.requestItems != null && !this.requestItems.isEmpty()) {
            this.totalAmount = this.requestItems.stream().mapToDouble(ReceiptItemDto::getAmount).sum();
        } else {
            this.totalAmount = 0.0;
        }

        if (this.requestItems.size() > 0) {
            Optional<Branch> optional = branchRepository.findById(requestItems.get(0).getIssueBranchId());
            if (optional.isPresent()) {
                Branch branch1 = optional.get();
                this.requestToBranch = branch1.getBranchName();
                this.requestToBranchAddress = branch1.getAddress();
                this.requestToBranchState = branch1.getState();
                this.requestToBranchId = branch1.getId();
                this.requestToBranchPincode = branch1.getPincode();
            }
        }

        if (otherBranchIssue.getOtherBranchIssueItems() != null) {

            this.otherBranchIssueValue = otherBranchIssue.getOtherBranchIssueItems();

        }
        if (this.otherBranchIssueValue != null && !this.otherBranchIssueValue.isEmpty()) {
            List<IssueItemsBriefDto> issueItemsBriefQuantity = new ArrayList<>();
            Map<String, List<OtherBranchIssueItems>> issueItemMap =
                    this.otherBranchIssueValue.stream().collect(Collectors.groupingBy(issueItem -> String.valueOf(issueItem.getItemId()) + issueItem.getSelectLot()));
            calculateQuantity(issueItemsBriefQuantity, issueItemMap);
            this.otherBranchIssueItemQuantity = issueItemsBriefQuantity;
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

    public MaterialReciptDto(MaterialRequestList materialRequest) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = "RQ - " + materialRequest.getId();
        this.requestName = materialRequest.getRequestDescription();
        this.requestFromBranch = materialRequest.getRequestFromBranch();
        this.requestFromBranchId = materialRequest.getRequestFromBranchId();
        this.requestFromDepartment = materialRequest.getRequestFromDepartment();
        this.requestFromDepartmentId = materialRequest.getRequestFromDepartmentId();
        this.status = materialRequest.getStatus();
        this.requestDescription = materialRequest.getRequestDescription();
        this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());


    }
}