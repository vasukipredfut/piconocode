package com.aria.model;

import com.aria.dto.OtherBranchIssueItemsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "other_branch_issue_items")
public class OtherBranchIssueItems {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_Item_Id")
    private Long issueItemId;

    @Column(name = "item_Id")
    private Long itemId;

    @Column(name = "item_Name")
    private String itemName;

    @Column(name = "issued_From_Bin")
    private String issuedFromBin;

    @Column(name = "issued_From_Batch")
    private String issuedFromBatch;

    @Column(name = "requested_Quantity")
    private String requestedQuantity;

    @Column(name = "allocated_Quantity")
    private String allocatedQuantity;

    @Column(name = "pending_Quantity")
    private String pendingQuantity;

    @Column(name = "pre_allocated_Quantity")
    private String preAllottedQuantity;

    @Column(name = "branch_Id")
    private Long branchId;

    @Column(name = "dept_Id")
    private Long deptId;

    @Column
    private String issue;

    @Column
    private int active;

    @Column(name = "select_Lot")
    private String selectLot;

    @Column(name = "created_By", updatable = false)
    private Long createdBy;

    @Column(name = "updated_By")
    private Long updatedBy;

    @Column(updatable = false)
    private Date created_date;

    @Column
    private Date updated_date;

    @Column
    private String material_return;

    @Transient
    private List<BinDetails> binDetails;

    @Column(name = "bin_stock_id",updatable = false)
    private Long binStockId;

    public OtherBranchIssueItems(OtherBranchIssueItemsDTO issueItemsDetails) {
        this.id = issueItemsDetails.getId();
        this.issueItemId = issueItemsDetails.getIssueItemId();
        this.itemName = issueItemsDetails.getItemName();
        this.itemId = issueItemsDetails.getItemId();
        this.issuedFromBin = issueItemsDetails.getIssuedFromBin();
        this.issuedFromBatch = issueItemsDetails.getIssuedFromBatch();
        this.requestedQuantity = issueItemsDetails.getRequestedQuantity();
        this.allocatedQuantity = issueItemsDetails.getAllocatedQuantity();
        this.pendingQuantity = issueItemsDetails.getPendingQuantity();
        this.preAllottedQuantity = issueItemsDetails.getPreAllottedQuantity();
        this.branchId = issueItemsDetails.getBranchId();
        this.deptId = issueItemsDetails.getDeptId();
        this.selectLot = issueItemsDetails.getSelectLot();
        this.issue = issueItemsDetails.getIssue();
        this.createdBy = issueItemsDetails.getCreatedBy();
        this.updatedBy = issueItemsDetails.getUpdatedBy();
        this.created_date = issueItemsDetails.getCreated_date();
        this.updated_date = issueItemsDetails.getUpdated_date();
        this.material_return = issueItemsDetails.getMaterialReturn();
        this.binStockId = issueItemsDetails.getBinStockId();
    }

    public OtherBranchIssueItems(IssueItems issueItemsDetails) {
        this.itemName = issueItemsDetails.getItemName();
        this.itemId = issueItemsDetails.getItemId();
        this.issuedFromBin = issueItemsDetails.getIssuedFromBin();
        this.issuedFromBatch = issueItemsDetails.getIssuedFromBatch();
        this.requestedQuantity = issueItemsDetails.getRequestedQuantity();
        this.allocatedQuantity = issueItemsDetails.getAllocatedQuantity();
        this.pendingQuantity = issueItemsDetails.getPendingQuantity();
        this.preAllottedQuantity = issueItemsDetails.getPreAllottedQuantity();
        this.branchId = issueItemsDetails.getBranchId();
        this.deptId = issueItemsDetails.getDeptId();
        this.selectLot = issueItemsDetails.getSelectLot();
        this.issue = issueItemsDetails.getIssue();
        this.createdBy = issueItemsDetails.getCreatedBy();
        this.updatedBy = issueItemsDetails.getUpdatedBy();
        this.created_date = issueItemsDetails.getCreated_date();
        this.updated_date = issueItemsDetails.getUpdated_date();
        this.binDetails = issueItemsDetails.getBinDetails();
        this.binStockId = issueItemsDetails.getReceiveBinStockId();
    }



}
