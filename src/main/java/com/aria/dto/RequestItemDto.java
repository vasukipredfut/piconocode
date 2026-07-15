package com.aria.dto;

import com.aria.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RequestItemDto {

	private Long id;
	
	private Long requestId;
	
	private Long itemId;

	private String itemName;

	private String selectLot;
	
	private Long issueDepartmentId;

	private String issueDepartment;

	private Long issueBranchId;

	private String issueBranch;

	private String stockQuantity;
	
	private String stockRequired;
	
	private int active;
	
	private Long createdBy;
	
	private Long updatedBy;
	
	private Date created_date;
	
	private Date updated_date;

	private Double orderQtyKg;

	private String status;

	public RequestItemDto(RequestItem requestItem) {
		this.id = requestItem.getId();
		this.itemId = requestItem.getItemId();
		this.itemName = requestItem.getItemName();
		this.selectLot = requestItem.getSelectLot();
		this.requestId = requestItem.getRequestId();
		if(requestItem.getIssueBranch() != null) {
			this.issueBranchId = requestItem.getIssueBranch().getId();
			this.issueBranch = requestItem.getIssueBranch().getBranchName();
		}
		if(requestItem.getIssueDepartment() != null) {
			this.issueDepartmentId = requestItem.getIssueDepartment().getId();
			this.issueDepartment = requestItem.getIssueDepartment().getName();
		}
		this.stockQuantity = requestItem.getStockQuantity();
		this.stockRequired = requestItem.getStockRequired();
		this.active = requestItem.getActive();
		this.createdBy = requestItem.getCreatedBy();
		this.updatedBy = requestItem.getUpdatedBy();
		this.created_date = requestItem.getCreated_date();
		if (requestItem.getUpdated_date() != null) {
			this.updated_date = requestItem.getUpdated_date();
		} else {
			this.updated_date = requestItem.getCreated_date();
		}
		this.status = requestItem.getStatus();
	}
}
