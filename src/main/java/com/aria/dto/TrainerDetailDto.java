package com.aria.dto;

import com.aria.model.TrainerDetail;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainerDetailDto {

	private Long id;

	private Long departmentId;

	private String departmentName;

	private Long trainerId;

	private String trainerName;

	private String externalEmployeeName;

	private String externalOrganisationName;

//	private Integer totalTrainingsDone;

	private Long scheduleTrainingId;

//	private Long lastScheduleTrainingId;
//
//	private String lastTrainingOn;
//
//	private Long trainerFeedbackId;

	private Float averageScore;

	public TrainerDetailDto(TrainerDetail trainerDetail) {

		this.id = trainerDetail.getId();

		if (trainerDetail.getDepartment() != null) {
			this.departmentId = trainerDetail.getDepartment().getId();
			this.departmentName = trainerDetail.getDepartment().getName();
		}

		if (trainerDetail.getTrainer() != null) {
			this.trainerId = trainerDetail.getTrainer().getId();
			this.trainerName = trainerDetail.getTrainer().getEmployeename();
		}

		this.externalEmployeeName = trainerDetail.getExternalEmployeeName();

		this.externalOrganisationName = trainerDetail.getExternalOrganisationName();

//		this.totalTrainingsDone = trainerDetail.getTotalTrainingsDone();

		if (trainerDetail.getScheduleTraining() != null) {
			this.scheduleTrainingId = trainerDetail.getScheduleTraining().getId();
		}

//		if (trainerDetail.getLastScheduleTraining() != null) {
//			this.lastScheduleTrainingId = trainerDetail.getLastScheduleTraining().getId();
////			this.lastTrainingOn = trainerDetail.getLastScheduleTraining().getTrainingDate();
//		}
//
//		if (trainerDetail.getTrainerFeedback() != null) {
//			this.trainerFeedbackId = trainerDetail.getTrainerFeedback().getId();
//			this.score = trainerDetail.getTrainerFeedback().getAverageScore();
//		}

		this.averageScore = trainerDetail.getAverageScore();
	}
}
