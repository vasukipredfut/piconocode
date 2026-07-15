package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.Specification_parameter;
import com.aria.model.Specification_parameter_mapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecificationParameterDTO {

	
	private Long id;

	
	private String name;

	
	
	private Long specification_type;
	
	
	private Long parameter_type;
	
	private Long paramter_option_type;
	
	
	private Long branch;
	
	
	private Long department;
	
	
	private Long organisation;
	
	
	private Long active;
	

	
	private Long createdBy;



	private LocalDateTime created_date;
	
	private List<SpecificationParameterMappingDto> parameterMapping;


	public SpecificationParameterDTO(Specification_parameter_mapping field) {
		
		this.id = field.getParamter().getId();
		this.name =field.getParamter().getName();
		this.specification_type = field.getSpecification().getSpecification_type();
		this.parameter_type=field.getParameter_type();
		this.paramter_option_type=field.getParameter_option_type();
		this.active=field.getActive();
		this.branch=field.getSpecification().getBranch();
		this.department=field.getSpecification().getDepartment();
		this.organisation=field.getSpecification().getOrganisation();

	}
	
	
	public SpecificationParameterDTO(Specification_parameter field) {
		
		this.id = field.getId();
		this.name =field.getName();
		this.specification_type = field.getSpecification_type();
		this.active=field.getActive();
		this.branch=field.getBranch();
		this.department=field.getDepartment();
		this.organisation=field.getOrganisation();

	}


	public SpecificationParameterDTO(SpecificationParameterList field) {

		this.id = field.getId();
		this.name =field.getName();
		this.specification_type = field.getSpecification_type();
		this.parameter_type=field.getParameter_type();
		this.paramter_option_type=field.getParamter_option_type();
		this.active=field.getActive();
		this.branch=field.getBranch();
		this.department=field.getDepartment();
		this.organisation=field.getOrganisation();

	}


	
}
