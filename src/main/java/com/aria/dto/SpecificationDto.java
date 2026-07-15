package com.aria.dto;

import java.util.List;

import com.aria.model.SpecificationDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecificationDto {

	private Long id;
	
	private String name;
	
	private Long refId;
	
	private String  refName;
		
	private Long specType;

	private Long branch;
	
	private Long department;
	
	private Long org;
	
	private List<SpecificationParameterDTO> parameters;

	public SpecificationDto(SpecificationDetails field) {
	
		
		this.id =field.getId();
		this.name=field.getName();
		this.refId=field.getRefid();
		this.refName=field.getRefname();
		this.specType=field.getSpectype();
		this.branch=field.getBranch();
		this.department=field.getDepartment();
		this.org=field.getOrg();
	}
	
	
	
}
