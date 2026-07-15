package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Panel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PanelDto {

	private Long id;
	private Long panelId;
	private String panelName;
	private String mf;
	
	private Long unitId;
	private String unitName;
	
	private Long machineId;
	private String machineName;
	
	private Long godownId;
	private String goDownName;
	
	private Long ebServiceId;
	private String ebServiceName;
	
	private String purposeBenefit;
	private String workSafetyInstructions;

	private Boolean machineOnOff;

	private double maxUnits;
	
	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public PanelDto(Panel field) {
		this.id = field.getId();
		this.panelId = field.getPanelId();
		this.panelName = field.getPanelName();
		
		if (field.getUnits() != null) {
			this.unitId = field.getUnits().getId();
			this.unitName = field.getUnits().getName();
		}
		
		if (field.getMachine() != null) {
			this.machineId = field.getMachine().getId();
			this.machineName = field.getMachine().getName();
		}
		
		if (field.getGodown() != null) {
			this.godownId = field.getGodown().getId();
			this.goDownName = field.getGodown().getName();
		}
		
		if (field.getEb() != null) {
			this.ebServiceId = field.getEb().getId();
			this.ebServiceName = field.getEb().getServiceName();
		}
		
		this.mf = field.getMf();
		this.purposeBenefit = field.getPurposeBenefit();
		this.workSafetyInstructions = field.getWorkSafetyInstructions();
		this.machineOnOff = field.getMachineOnOff();
		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();

		this.maxUnits = field.getMaxUnits();
	}

}
