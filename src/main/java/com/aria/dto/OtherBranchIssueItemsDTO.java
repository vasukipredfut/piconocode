package com.aria.dto;


import com.aria.model.OtherBranchIssueItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OtherBranchIssueItemsDTO {


    private Long id;

    private Long issueItemId;

    private String itemName;

    private Long itemId;

    private String issuedFromBin;

    private String issuedFromBatch;

    private String requestedQuantity;

    private String allocatedQuantity;

    private String pendingQuantity;

    private String preAllottedQuantity;

    private String issue;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private Date created_date;

    private Date updated_date;

    private String selectLot;
    private String stockQuantity;

    private Double orderQtyKg;

    private Long branchId;

    private Long deptId;

    private String materialReturn;
    private Long binStockId;


    public OtherBranchIssueItemsDTO(OtherBranchIssueItems issueItemsDetails) {
        this.id = issueItemsDetails.getId();
        this.issueItemId = issueItemsDetails.getIssueItemId();
        this.itemId = issueItemsDetails.getItemId();
        this.itemName = issueItemsDetails.getItemName();
        this.issuedFromBin = issueItemsDetails.getIssuedFromBin();
        this.issuedFromBatch = issueItemsDetails.getIssuedFromBatch();
        this.requestedQuantity = issueItemsDetails.getRequestedQuantity();
        this.allocatedQuantity = issueItemsDetails.getAllocatedQuantity();
        this.pendingQuantity = issueItemsDetails.getPendingQuantity();
        this.preAllottedQuantity = issueItemsDetails.getPreAllottedQuantity();
        this.selectLot = issueItemsDetails.getSelectLot();
        this.issue=issueItemsDetails.getIssue();
        this.createdBy = issueItemsDetails.getCreatedBy();
        this.branchId = issueItemsDetails.getBranchId();
        this.deptId = issueItemsDetails.getDeptId();
        this.created_date = issueItemsDetails.getCreated_date();
        this.materialReturn = issueItemsDetails.getMaterial_return();
        this.binStockId = issueItemsDetails.getBinStockId();
        if (issueItemsDetails.getUpdated_date() != null) {
            this.updated_date = issueItemsDetails.getUpdated_date();
        } else {
            this.updated_date = issueItemsDetails.getCreated_date();
        }

    }

}
