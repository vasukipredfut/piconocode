package com.aria.dto;

import com.aria.model.TrainingType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainingTypeDto {

	private Long id;

	private String trainingTypeName;

	public TrainingTypeDto(TrainingType trainingType) {

		this.id = trainingType.getId();
		this.trainingTypeName = trainingType.getTrainingTypeName();
	}

}
