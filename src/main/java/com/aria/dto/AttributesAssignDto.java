package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.AttributesAssign;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttributesAssignDto {

	private Long id;
	private String screenName;
	private String fieldName;
	private String process;
	private String taskType;
	
	
	private Long departmentId;
	
	
	
	private String department;
	
	private String modeOfEntry;
	private String kpiName;
	private Long kpiNameId;
	
	private String productName;
	private Long productId;
	
	
	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;
	
	//private List<AttributesStepsDto> attributesSteps;

	public AttributesAssignDto(AttributesAssign attributesAssign) {
		this.id = attributesAssign.getId();

		this.screenName = attributesAssign.getScreenName();

		this.fieldName = attributesAssign.getFieldName();
		this.taskType = attributesAssign.getTaskType();
		this.process = attributesAssign.getProcess();
		
		
		
		if (attributesAssign.getDept() != null) {
			this.department = attributesAssign.getDept().getName();
			this.departmentId = attributesAssign.getDept().getId();
		}
		
		if (attributesAssign.getKpiName() != null) {
			this.kpiName = attributesAssign.getKpiName().getName();
			this.kpiNameId = attributesAssign.getKpiName().getId();
		}
		
		if (attributesAssign.getProduct() != null) {
			this.productName = attributesAssign.getProduct().getProductName();
			this.productId = attributesAssign.getProduct().getId();
		}
		this.modeOfEntry = attributesAssign.getModeOfEntry();
		this.createdBy = attributesAssign.getCreatedBy();
		this.updatedBy = attributesAssign.getUpdatedBy();
		this.created_date = attributesAssign.getCreated_date();
		this.updated_date = attributesAssign.getUpdated_date();
		
		/*this.attributesSteps = attributesAssign.getAttributesSteps() != null
	                ? attributesAssign.getAttributesSteps().stream().map(field -> new AttributesStepsDto(field)).collect(Collectors.toList()) : null;
*/
	}

}
