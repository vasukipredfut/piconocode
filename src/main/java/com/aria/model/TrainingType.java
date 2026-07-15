package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.TrainingTypeDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "training_type")
public class TrainingType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "training_Type_Name")
	private String trainingTypeName;

	public TrainingType(TrainingTypeDto trainingTypeDto) {

		this.id = trainingTypeDto.getId();

		this.trainingTypeName = trainingTypeDto.getTrainingTypeName();
	}
}
