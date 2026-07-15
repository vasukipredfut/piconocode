package com.aria.dto;

import com.aria.model.IScheduleTrainingFilterRegistration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleTrainingFilterRegistrationDto {

	private Long id;

//	private skillDetails;

	private Long skillId;

	private String skillName;

//	private trainingType;

	private Long trainingTypeId;

	private String trainingTypeName;

	private String trainingDate;

	private String conductedBy;

	private String lastRegistrationDate;

	private Integer seatsLeft;

	public ScheduleTrainingFilterRegistrationDto(
			IScheduleTrainingFilterRegistration iScheduleTrainingFilterRegistration) {

		this.id = iScheduleTrainingFilterRegistration.getId();

//		 skillDetails;

		this.skillId = iScheduleTrainingFilterRegistration.getSkillId();

		this.skillName = iScheduleTrainingFilterRegistration.getSkillName();

//		 trainingType;

		this.trainingTypeId = iScheduleTrainingFilterRegistration.getTrainingTypeId();

		this.trainingTypeName = iScheduleTrainingFilterRegistration.getTrainingTypeName();

//		this.trainingDate = iScheduleTrainingFilterRegistration.getTrainingDate();

		this.conductedBy = iScheduleTrainingFilterRegistration.getConductedBy();

//		this.lastRegistrationDate = iScheduleTrainingFilterRegistration.getLastRegistrationDate();

		this.seatsLeft = iScheduleTrainingFilterRegistration.getSeatsLeft();
	}
}
