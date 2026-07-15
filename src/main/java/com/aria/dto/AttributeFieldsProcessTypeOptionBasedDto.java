package com.aria.dto;

import com.aria.model.AttributeFieldsProcessTypeOptionBased;

import com.aria.model.MaintenanceAttributeFieldsOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttributeFieldsProcessTypeOptionBasedDto {

	private Long id;
	private String options;
	private String score;
	
	public AttributeFieldsProcessTypeOptionBasedDto(AttributeFieldsProcessTypeOptionBased field){
		this.id = field.getId();
		this.options = field.getOptions();
	}

	public AttributeFieldsProcessTypeOptionBasedDto(MaintenanceAttributeFieldsOptions field){
		this.id = field.getId();
		this.options = field.getOptions();
		this.score = field.getScore();
	}
}
