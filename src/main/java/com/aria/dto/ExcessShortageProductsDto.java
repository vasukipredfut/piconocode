package com.aria.dto;

import com.aria.model.DpEntryStoredBinDetails;
import com.aria.model.ExcessShortageProducts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ExcessShortageProductsDto {

	private Long id;

	private Long excessStorageId;

	private Long productId;

	private String productCode;

	private String productName;

	private String productType;

	private String bin;

	private String batchNo;

	private String trnType;

	private String lot;

	private String type;

	private String binQuantity;

	private String quantity;

	private String excessOrShortage;
	
    private String isSelect;

	private List<ExcessStoredBinDetailsDto> binDetails;

	public ExcessShortageProductsDto(ExcessShortageProducts excessShortageProducts) {
		this.id = excessShortageProducts.getId();
		this.excessStorageId = excessShortageProducts.getId();
		if (excessShortageProducts.getProduct() != null) {
			this.productId = excessShortageProducts.getProduct().getId();
			this.productName = excessShortageProducts.getProduct().getProductName();
		}
		this.productCode = excessShortageProducts.getProductCode(); 
		this.productType = excessShortageProducts.getProductType();
		this.batchNo = excessShortageProducts.getBatchNo();
		this.lot = excessShortageProducts.getLot();
		this.bin = excessShortageProducts.getBin();
		this.trnType = excessShortageProducts.getTrnType();
		this.type = excessShortageProducts.getType();
		this.quantity = excessShortageProducts.getQuantity();
		this.binQuantity = excessShortageProducts.getBinQuantity();
		this.excessOrShortage = excessShortageProducts.getExcessOrShortage();
		this.isSelect = excessShortageProducts.getIsSelect();
		this.binDetails = excessShortageProducts.getBinDetails() != null
				? excessShortageProducts.getBinDetails().stream().map(field -> new ExcessStoredBinDetailsDto(field)).collect(Collectors.toList()) : null;

	}

}
