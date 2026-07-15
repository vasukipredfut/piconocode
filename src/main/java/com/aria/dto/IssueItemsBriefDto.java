package com.aria.dto;

import com.aria.model.IssueItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class IssueItemsBriefDto {

	private Long issueItemId;

	private String itemName;

	private Long itemId;

	private String lot;

	private Double requestedQuantity;

	private Double allocatedQuantity;

	private Double pendingQuantity;

	private Double orderQtyKg;


	public IssueItemsBriefDto( IssueItemsBrief issueItemsBrief) {
		this.issueItemId = issueItemsBrief.getIssueItemId();
		this.itemName = issueItemsBrief.getItemName();
		this.itemId = issueItemsBrief.getItemId();
		this.lot = issueItemsBrief.getLot();
		this.requestedQuantity = issueItemsBrief.getRequestedQuantity();
		this.allocatedQuantity = issueItemsBrief.getAllocatedQuantity();
		this.pendingQuantity = issueItemsBrief.getPendingQuantity();

	}
}