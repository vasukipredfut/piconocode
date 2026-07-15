package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExcessStoredBinDetailsReceiptDto {
	private Long id;

	private Long excessOrReturnProductId;

	private Long itemId;

	private String itemName;

	private Long binNo;

	private String batchNo;

	private Double currentStock;

	private Double excessOrShortage;
}
