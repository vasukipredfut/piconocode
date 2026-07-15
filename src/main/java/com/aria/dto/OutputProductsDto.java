package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.aria.model.OutputProducts;

@Getter
@Setter
@NoArgsConstructor
public class OutputProductsDto {

	private Long id;

	private String productname;

	private String godown;

	private String binno;

	private String batchno;

	private String outputquantity;

	private String outputlot;

	private String inventorytype;

	private String totalinputquantity;

	private String totaloutputquantity;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public OutputProductsDto(OutputProducts outputProductsDetails) {
		this.id = outputProductsDetails.getId();
		this.productname = outputProductsDetails.getProductname();
		this.godown = outputProductsDetails.getGodown();
		this.binno = outputProductsDetails.getBinno();
		this.batchno = outputProductsDetails.getBatchno();
		this.outputquantity = outputProductsDetails.getOutputquantity();
		this.outputlot = outputProductsDetails.getOutputlot();
		this.inventorytype = outputProductsDetails.getInventorytype();
		this.totalinputquantity = outputProductsDetails.getTotalinputquantity();
		this.totaloutputquantity = outputProductsDetails.getTotaloutputquantity();
		this.active = outputProductsDetails.getActive();
		this.createdBy = outputProductsDetails.getCreatedBy();
		this.updatedBy = outputProductsDetails.getUpdatedBy();
		this.created_date = outputProductsDetails.getCreated_date();
		if (outputProductsDetails.getUpdated_date() != null) {
			this.updated_date = outputProductsDetails.getUpdated_date();
		} else {
			this.updated_date = outputProductsDetails.getCreated_date();
		}

	}
}
