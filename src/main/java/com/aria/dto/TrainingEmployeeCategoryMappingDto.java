package com.aria.dto;

import com.aria.model.TrainingEmployeeCategoryMapping;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class TrainingEmployeeCategoryMappingDto {

	private Long id;

	private String employeeCategory;

	private Long scheduleTrainingId;

	public TrainingEmployeeCategoryMappingDto(TrainingEmployeeCategoryMapping trainingEmployeeCategoryMapping) {

		this.id = trainingEmployeeCategoryMapping.getId();

		this.employeeCategory = trainingEmployeeCategoryMapping.getEmployeeCategory();

		this.scheduleTrainingId = trainingEmployeeCategoryMapping.getScheduleTrainingId();
	}
}
