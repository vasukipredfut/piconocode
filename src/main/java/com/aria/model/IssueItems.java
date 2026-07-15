
package com.aria.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.aria.dto.IssueItemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "issue_items")
public class IssueItems {

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

	@Column(name = "created_By", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private Date created_date;

	@Column
	private Date updated_date;

	@Column(name = "select_Lot")
	private String selectLot;
	
	@Transient
	private List<BinDetails> binDetails;

	@Column(name = "bin_stock_id")
	private Long binStockId;

	@Column(name = "receive_bin_stock_id")
	private Long receiveBinStockId;

	@Column(name = "stock_Quantity")
	private String stockQuantity;

	public IssueItems(IssueItemsDto issueItemsDetails) {
		this.id = issueItemsDetails.getId();
		this.issueItemId = issueItemsDetails.getIssueItemId();
		this.itemName = issueItemsDetails.getItemName();
		this.itemId = issueItemsDetails.getItemId();
		this.issuedFromBin = issueItemsDetails.getIssuedFromBin();
		this.issuedFromBatch = issueItemsDetails.getIssuedFromBatch();
		this.requestedQuantity = issueItemsDetails.getRequestedQuantity();
        if(issueItemsDetails.getAllocatedQuantity() != null && !issueItemsDetails.getAllocatedQuantity().isEmpty()) {
            this.allocatedQuantity = issueItemsDetails.getAllocatedQuantity();
        }else {
            this.allocatedQuantity = "0";
        }
		this.pendingQuantity = issueItemsDetails.getPendingQuantity();
		this.preAllottedQuantity = issueItemsDetails.getPreAllottedQuantity();
		this.selectLot = issueItemsDetails.getSelectLot();
		this.issue = issueItemsDetails.getIssue();
		this.createdBy = issueItemsDetails.getCreatedBy();
		this.updatedBy = issueItemsDetails.getUpdatedBy();
		this.created_date = issueItemsDetails.getCreated_date();
		this.updated_date = issueItemsDetails.getUpdated_date();
		this.active = issueItemsDetails.getActive();
		this.binStockId = issueItemsDetails.getBinStockId();
		this.receiveBinStockId = issueItemsDetails.getReceiveBinStockId();
		this.stockQuantity = issueItemsDetails.getStockQuantity();
	}

	
}