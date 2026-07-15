package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.TrainingEmployeeCategoryMappingDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "training_employee_category_mapping")
public class TrainingEmployeeCategoryMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "employee_Category")
	private String employeeCategory;

	@Column(name = "schedule_Training_Id")
	private Long scheduleTrainingId;

	public TrainingEmployeeCategoryMapping(TrainingEmployeeCategoryMappingDto trainingEmployeeCategoryMappingDto) {

		this.id = trainingEmployeeCategoryMappingDto.getId();

		this.employeeCategory = trainingEmployeeCategoryMappingDto.getEmployeeCategory();

		this.scheduleTrainingId = trainingEmployeeCategoryMappingDto.getScheduleTrainingId();
	}
}
