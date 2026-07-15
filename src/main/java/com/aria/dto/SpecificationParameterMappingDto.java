package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Specification_parameter_mapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SpecificationParameterMappingDto {

	
	private Long id;


	private Long specification_id;
	

	private Long paramter_id;
	
	
	//1-Mandatory , 2 - NonMandatory

	private Long paramter_type;
	
		
	
	private String value;
	
	
	private Long active;
	
	//0-Option, 1- Valuebased
	
	private Long paramter_option_type;

	private Long createdBy;


	private LocalDateTime created_date;


	public SpecificationParameterMappingDto(Specification_parameter_mapping field) {
		
		this.id=field.getId();
		this.active=field.getActive();
		this.paramter_id=field.getParamter().getId();
		this.paramter_option_type=field.getParameter_option_type();
		this.paramter_type=field.getParameter_type();
		this.specification_id=field.getSpecification().getId();
		this.value=field.getValue();
		this.created_date=field.getCreated_date();
		this.createdBy=field.getCreatedBy();
		

	}
	
	
}
