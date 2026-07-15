package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Branch;
import com.aria.model.MachineClassification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineClassificationDto {

    private Long id;

    private String name;
    
    private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;


    public MachineClassificationDto(MachineClassification field) {
        this.id = field.getId();
        this.name = field.getName();
        this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();	
    }
}
