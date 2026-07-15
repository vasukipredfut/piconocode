package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.TrainingUserRegistrationMapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainingUserRegistrationMappingDto {

	private Long id;

//	private ScheduleTraining scheduleTraining;

	private Long scheduleTrainingId;

//	private User user;

	private Long userId;

	private String registrationStatus;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public TrainingUserRegistrationMappingDto(TrainingUserRegistrationMapping trainingUserRegistrationMapping) {

		this.id = trainingUserRegistrationMapping.getId();
		if (trainingUserRegistrationMapping.getScheduleTraining() != null) {
			this.scheduleTrainingId = trainingUserRegistrationMapping.getScheduleTraining().getId();
		}

		if (trainingUserRegistrationMapping.getUser() != null) {
			this.userId = trainingUserRegistrationMapping.getUser().getId();
		}
		this.registrationStatus = trainingUserRegistrationMapping.getRegistrationStatus();
		this.createdBy = trainingUserRegistrationMapping.getCreatedBy();
		this.updatedBy = trainingUserRegistrationMapping.getUpdatedBy();
		this.created_date = trainingUserRegistrationMapping.getCreated_date();
		this.updated_date = trainingUserRegistrationMapping.getUpdated_date();
	}

}
