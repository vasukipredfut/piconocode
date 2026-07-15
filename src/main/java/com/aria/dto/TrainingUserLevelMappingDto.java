package com.aria.dto;

import com.aria.model.TrainingUserLevelMapping;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class TrainingUserLevelMappingDto {

	private Long id;

	private String userLevel;

	private Long scheduleTrainingId;

	public TrainingUserLevelMappingDto(TrainingUserLevelMapping trainingUserLevelMapping) {

		this.id = trainingUserLevelMapping.getId();

		this.userLevel = trainingUserLevelMapping.getUserLevel();

		this.scheduleTrainingId = trainingUserLevelMapping.getScheduleTrainingId();
	}
}
