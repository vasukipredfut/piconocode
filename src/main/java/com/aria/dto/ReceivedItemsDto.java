package com.aria.dto;

import com.aria.model.ReceivedItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReceivedItemsDto {

	private Long id;

	private String itemname;

	private String containertype;

	private String quantity;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public ReceivedItemsDto(ReceivedItems receivedItemsDetails) {
		this.id = receivedItemsDetails.getId();
		this.itemname = receivedItemsDetails.getItemname();
		this.containertype = receivedItemsDetails.getContainertype();
		this.quantity = receivedItemsDetails.getQuantity();
		this.active = receivedItemsDetails.getActive();
		this.createdBy = receivedItemsDetails.getCreatedBy();
		this.updatedBy = receivedItemsDetails.getUpdatedBy();
		this.created_date = receivedItemsDetails.getCreated_date();
		if (receivedItemsDetails.getUpdated_date() != null) {
			this.updated_date = receivedItemsDetails.getUpdated_date();
		} else {
			this.updated_date = receivedItemsDetails.getCreated_date();
		}
	}
}
