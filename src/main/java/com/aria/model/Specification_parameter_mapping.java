package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


import com.aria.dto.PanelDto;
import com.aria.dto.SpecificationDto;

import com.aria.dto.SpecificationParameterDTO;
import com.aria.dto.SpecificationParameterMappingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "specification_parameter_mapping")
public class Specification_parameter_mapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@OneToOne
	@JoinColumn(name = "specification_id", referencedColumnName = "id")
	private Specification specification;
	
	@OneToOne
	@JoinColumn(name = "parameter_id", referencedColumnName = "id")
	private Specification_parameter paramter;
	
	
	//1-Mandatory , 2 - NonMandatory
	@Column
	private Long parameter_type;
	
		
	@Column
	private String value;
	
	@Column
	private Long active;
	
	//0-Option, 1- Valuebased
	@Column
	private Long parameter_option_type;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(updatable = false)
	private LocalDateTime created_date;


	public Specification_parameter_mapping(SpecificationParameterMappingDto field, SpecificationParameterDTO specificationParameter) {
		
		this.id = field.getId();
		this.active=field.getActive();
		Specification_parameter parameter = new Specification_parameter();
		parameter.setId(specificationParameter.getId());
		this.paramter = parameter;
		Specification spec = new Specification();
		spec.setId(field.getSpecification_id());
		this.specification = spec;
		this.value = field.getValue();
		this.active=1L;
		this.parameter_type=field.getParamter_type();
		this.parameter_option_type=field.getParamter_option_type();

	}
	
	
}
