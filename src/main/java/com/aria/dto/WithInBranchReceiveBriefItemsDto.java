package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithInBranchReceiveBriefItemsDto {
	

	private String itemName;
	
	private String lot;

	private Long itemId;

	private Double requestedQuantity;

	private Double allocatedQuantity;

	private Double pendingQuantity;

}
