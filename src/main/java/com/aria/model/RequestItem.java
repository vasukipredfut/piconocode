package com.aria.model;

import com.aria.dto.RequestItemDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "request_Item")
public class RequestItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "request_id")
	private Long requestId;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "item_Name")
	private String itemName;

	@Column(name = "select_Lot")
	private String selectLot;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails issueDepartment;
	
	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch issueBranch;

	@Column(name = "stock_Quantity")
	private String stockQuantity;

	@Column(name = "stock_Required")
	private String stockRequired;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private Date created_date;

	@Column
	private Date updated_date;

	@Column
	private String status;

	public RequestItem(RequestItemDto requestItem) {
		this.id = requestItem.getId();
		this.requestId = requestItem.getRequestId();
		this.itemName = requestItem.getItemName();
		this.itemId = requestItem.getItemId();
		this.selectLot = requestItem.getSelectLot();
		if(requestItem.getIssueDepartmentId() != null) {
			DepartmentDetails dept = new DepartmentDetails();
			dept.setId(requestItem.getIssueDepartmentId());
			this.issueDepartment = dept;
		}
		if(requestItem.getIssueBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(requestItem.getIssueBranchId());
			this.issueBranch = branch;
		}
		this.stockQuantity = requestItem.getStockQuantity();
		this.stockRequired = requestItem.getStockRequired();
		this.active = requestItem.getActive();
		this.createdBy = requestItem.getCreatedBy();
		this.updatedBy = requestItem.getUpdatedBy();
		this.created_date = requestItem.getCreated_date();
		this.updated_date = requestItem.getUpdated_date();
		this.status = requestItem.getStatus();
	}
}
