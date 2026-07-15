package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.MachineWFBreakdownReasonDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineWFBreakdownReasonDetailsDto {

	private Long id;

	private Long machineWorkFlowId;
	
	private Long breakdownReasonId;

	private Long productsId;

	private String productsName;

	private String productsCode;

	private String binName;

	private String batchNo;

	private String zone;

	private Long quantity;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public MachineWFBreakdownReasonDetailsDto(MachineWFBreakdownReasonDetails field) {
		this.id = field.getId();

		if (field.getProducts() != null) {
			this.productsId = field.getProducts().getId();
			this.productsName = field.getProducts().getProductName();
			this.productsCode = field.getProducts().getProductCode();
		}

		this.breakdownReasonId = field.getBreakdownReasonId();
		this.machineWorkFlowId = field.getMachineWorkFlowId();
		this.binName = field.getBinName();
		this.batchNo = field.getBatchNo();
		this.quantity = field.getQuantity();
		this.zone = field.getZone();

		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
