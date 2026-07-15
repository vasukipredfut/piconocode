package com.aria.dto;

import java.util.List;

import com.aria.model.BinDetails;
import com.aria.model.ExternalGodownBinDetails;
import com.aria.model.RequestGodownItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestGodownItemDto {

	private Long id;
	private Long goDownExternalId;
	private Long itemId;
	private String itemName;
	private String unit;
	private String selectLot;
	private String quantity;
	private String stockQuantity;
	private String allocatedQuantity;
	private String hsnNo;
	private String materialReturn;
	private List<BinDetails> bindetail;
	private List<BinStockDto> binStock;

	public RequestGodownItemDto(RequestGodownItem requestGodownItem) {
		this.id = requestGodownItem.getId();
		this.goDownExternalId = requestGodownItem.getGoDownExternalId();
		this.itemId = requestGodownItem.getItemId();
		this.itemName = requestGodownItem.getItemName();
		this.selectLot = requestGodownItem.getSelectLot();
		this.quantity = requestGodownItem.getQuantity();
		this.stockQuantity = requestGodownItem.getStockQuantity();
		this.allocatedQuantity = requestGodownItem.getAllocatedQuantity();
		this.materialReturn = requestGodownItem.getMaterial_return();

	}

}
