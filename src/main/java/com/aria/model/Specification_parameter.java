package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import com.aria.dto.PanelDto;
import com.aria.dto.SpecificationDto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "specification_parameter")
public class Specification_parameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	
	@Column
	private Long specification_type;
	
	
	@Column
	private Long branch;
	
	@Column
	private Long department;
	
	@Column
	private Long organisation;
	
	@Column
	private Long active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;


	@Column(updatable = false)
	private LocalDateTime created_date;


	
	
	
}
