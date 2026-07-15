package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.TrainerFeedbackScore;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainerFeedbackScoreDto {

	private Long id;

	private Long scheduleTrainingId;

//	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	private Long traineeId;

	private String traineeName;

//	TrainerDetail trainerDetail;

	private Long trainerId;

	private String trainerName;

	private Long userIdTrainee;

	private Long userIdTrainer;

	private Float technicalKnowledgeScore;

	private Float communicationScore;

	private Float interactionScore;

	private Float averageScore;

	private String submittedDate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public TrainerFeedbackScoreDto(TrainerFeedbackScore trainerFeedbackScore, DateUtil dateUtil) {

		this.id = trainerFeedbackScore.getId();

		this.scheduleTrainingId = trainerFeedbackScore.getScheduleTrainingId();

//		 TrainingUserRegistrationMapping trainingUserRegistrationMapping;

		if (trainerFeedbackScore.getTrainingUserRegistrationMapping() != null
				&& trainerFeedbackScore.getTrainingUserRegistrationMapping().getUser() != null) {
			this.traineeId = trainerFeedbackScore.getTrainingUserRegistrationMapping().getUser().getId();
			this.traineeName = trainerFeedbackScore.getTrainingUserRegistrationMapping().getUser().getEmployeename();
		}

//		TrainerDetail trainerDetail;

		if (trainerFeedbackScore.getTrainerDetail() != null && trainerFeedbackScore.getTrainerDetail() != null) {
			this.trainerId = trainerFeedbackScore.getTrainerDetail().getTrainer().getId();
			this.trainerName = trainerFeedbackScore.getTrainerDetail().getTrainer().getEmployeename();
		}

		this.technicalKnowledgeScore = trainerFeedbackScore.getTechnicalKnowledgeScore();

		this.communicationScore = trainerFeedbackScore.getCommunicationScore();

		this.interactionScore = trainerFeedbackScore.getInteractionScore();

		this.averageScore = trainerFeedbackScore.getAverageScore();

		this.submittedDate = dateUtil.getDateToStringDDMMYYYY(trainerFeedbackScore.getSubmittedDate());

		this.createdBy = trainerFeedbackScore.getCreatedBy();

		this.updatedBy = trainerFeedbackScore.getUpdatedBy();

		this.created_date = trainerFeedbackScore.getCreated_date();

		this.updated_date = trainerFeedbackScore.getUpdated_date();
	}
}
