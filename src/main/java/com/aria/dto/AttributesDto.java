package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Attributes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttributesDto {

	private Long id;
	private Long proreportDbId;

	private String name;

	private Long taskNameId;
	private Long taskCategoryId;

	private String taskName;
	private String taskCategory;
	//private String purpose;

	//private String benefit;

	private Long positionNumber;
	private String attributeType;
	private String inputType;

	private List<AttributeFieldsProcessTypeOptionBasedDto> options;

	private String valueType;
	private String direct_cummulative;

	private String formula;

	private String minValue;

	private String maxValue;

	private Long score;

	private String unit;

//	private Long onTimeScore;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String purpose_benefit;
	private String workinst_saftmeasure;

	public AttributesDto(Attributes model) {

		this.id = model.getId();
		this.positionNumber = model.getPositionNumber();
		this.attributeType = model.getAttributeType();
		this.inputType = model.getInputType();
		this.valueType = model.getValueType();
		this.direct_cummulative = model.getDirect_cummulative();
		this.formula = model.getFormula();

		this.options = model.getProcessType() != null ? model.getProcessType().stream()
				.map(data -> new AttributeFieldsProcessTypeOptionBasedDto(data)).collect(Collectors.toList())
				: null;

		this.name = model.getName();
		if (model.getTaskNameDet() != null) {
			this.taskName = model.getTaskNameDet().getTaskName();
			this.taskNameId = model.getTaskNameDet().getId();
		}

		if (model.getTaskCategory() != null) {
			this.taskCategory = model.getTaskCategory().getTaskCategoryName();
			this.taskCategoryId = model.getTaskCategory().getId();
		}


		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
	}
}
