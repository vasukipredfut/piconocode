package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Generator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeneratorDto {

	private Long id;
	private String name;
	private String capacity;
	private String machineId;
	private String status;

	private Long createdBy;

	private Long updatedBy;
	
	private String createdByName;

	private String updatedByName;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public GeneratorDto(Generator gen) {
		this.id = gen.getId();

		this.name = gen.getName();
		this.status=gen.getStatus();
		this.capacity = gen.getCapacity();
		this.machineId = gen.getMachineId();

		if(gen.getCreatedBy()!=null) {
			this.createdBy = gen.getCreatedBy().getId();
			this.createdByName = gen.getCreatedBy().getEmployeename();
		}
		
		if(gen.getUpdatedBy()!=null) {
			this.updatedBy = gen.getUpdatedBy().getId();
			this.updatedByName = gen.getUpdatedBy().getEmployeename();
		}
		
		
		this.created_date = gen.getCreated_date();
		this.updated_date = gen.getUpdated_date();

	}

}
