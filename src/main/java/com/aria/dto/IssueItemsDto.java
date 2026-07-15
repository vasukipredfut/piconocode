package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.IssueItems;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor

public class IssueItemsDto {

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
	private Long binStockId;
	private Long receiveBinStockId;
	private Boolean issueStatus;

	public IssueItemsDto(IssueItems issueItemsDetails) {
		this.id = issueItemsDetails.getId();
		this.issueItemId = issueItemsDetails.getIssueItemId();
		this.active = issueItemsDetails.getActive();
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
		this.branchId = issueItemsDetails.getBranchId();
		this.deptId = issueItemsDetails.getDeptId();
		this.binStockId = issueItemsDetails.getBinStockId();
		this.receiveBinStockId = issueItemsDetails.getReceiveBinStockId();
		this.stockQuantity = issueItemsDetails.getStockQuantity();
		this.createdBy = issueItemsDetails.getCreatedBy();
		this.updatedBy = issueItemsDetails.getUpdatedBy();
		this.created_date = issueItemsDetails.getCreated_date();
		if (issueItemsDetails.getUpdated_date() != null) {
			this.updated_date = issueItemsDetails.getUpdated_date();
		} else {
			this.updated_date = issueItemsDetails.getCreated_date();
		}

	}
}