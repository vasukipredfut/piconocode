package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import com.aria.model.MachineWFBreakdownReason;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineWFBreakdownReasonDto {

	private Long id;

	
	
	private String problem;
	
	
	private String action;
	
	
//	private Date completedDate;
	
	
//	private String completedTime;


//	private Long productsId;
//
//	private String productsName;
//
//	private String productsCode;
//
//	private String binName;
//
//	private String batchNo;
//
//	private String zone;
//
//	private Long quantity;
	
	private List<MachineWFBreakdownReasonDetailsDto> breakdownDetails;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public MachineWFBreakdownReasonDto(MachineWFBreakdownReason field) {
		this.id = field.getId();

//		if (field.getProducts() != null) {
//			this.productsId = field.getProducts().getId();
//			this.productsName = field.getProducts().getProductName();
//			this.productsCode = field.getProducts().getProductCode();
//		}
		this.problem = field.getProblem();
		this.action = field.getAction();
//		this.completedTime = field.getCompletedTime();
//		this.machineWorkFlowId = field.getMachineWorkFlowId();
//		this.binName = field.getBinName();
//		this.batchNo = field.getBatchNo();
//		this.quantity = field.getQuantity();
//		this.zone = field.getZone();
		
//		 this.breakdownDetails = field.getBreakdownDetails() != null ? field.getBreakdownDetails().stream()
//					.map(obj -> new MachineWFBreakdownReasonDetailsDto(obj)).collect(Collectors.toList()) : null;

		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
