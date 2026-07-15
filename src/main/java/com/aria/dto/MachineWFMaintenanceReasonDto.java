package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.MachineWFMaintenanceReason;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineWFMaintenanceReasonDto {

	private Long id;
	
	private Long machineWorkFlowId;

	private Long assignAttributesId;

	private String assignAttributesName;

	private Long productsId;

	private String productsName;

	private String productsCode;

	private String valueType;

	private Long quantity;

	private String binName;
	
	private String batchNo;

	private String zone;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public MachineWFMaintenanceReasonDto(MachineWFMaintenanceReason field) {
		this.id = field.getId();
		if (field.getAssignAttributes() != null) {
			this.assignAttributesId = field.getAssignAttributes().getId();
			this.assignAttributesName = field.getAssignAttributes().getScreenName();
		}

		if (field.getProducts() != null) {
			this.productsId = field.getProducts().getId();
			this.productsName = field.getProducts().getProductName();
			this.productsCode = field.getProducts().getProductCode();
		}

		this.machineWorkFlowId = field.getMachineWorkFlowId() ;
		this.binName = field.getBinName();
		this.batchNo = field.getBatchNo();
		this.quantity = field.getQuantity();
		this.valueType = field.getValueType();
		this.zone = field.getZone();

		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
