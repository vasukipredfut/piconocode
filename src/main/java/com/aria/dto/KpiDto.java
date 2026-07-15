package com.aria.dto;


import java.util.List;

import com.aria.model.Kpi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KpiDto {

	private Long priid;

	private Long id;

	private Long refid;

	private Long tgid;

	private Long dispord;

	private String skip;

	private String active;

	private int stepNumber;
	private Long kpiNameId;
	private Long productMasterId;

	private String kpiName;
	private String productMasterName;

	private Long taskNameId;
	private Long taskCategoryId;
	private Long taskGroupId;

	private Long taskGroupMapId;

	private String taskName;
	private String taskCategory;
	private String taskGroup;
	
	private List<AttributesDto> attributeFieldsProcessType;
	
	//private List<AttributeFieldsMaintenanceTypeDto> attributeFieldsMaintenanceType;

	private List<PatlogDto> completedData;

	public KpiDto(Kpi model) {
		this.id = model.getId();
		this.priid = model.getPriid();
		this.refid = model.getRefid();
		this.tgid = model.getTgid();
		this.dispord = model.getDispord();
		this.skip = model.getSkip();
	}
}
