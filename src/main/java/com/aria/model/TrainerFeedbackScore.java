package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.TrainerFeedbackScoreDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trainer_feedback_score")
public class TrainerFeedbackScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "schedule_Training_Id")
	private Long scheduleTrainingId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id", referencedColumnName = "id", nullable = false)
	TrainerDetail trainerDetail;

	@Column(name = "user_Id_Trainee")
	private Long userIdTrainee;

	@Column(name = "user_Id_Trainer")
	private Long userIdTrainer;

	@Column(name = "technical_Knowledge_Score")
	private Float technicalKnowledgeScore;

	@Column(name = "communication_Score")
	private Float communicationScore;

	@Column(name = "interaction_Score")
	private Float interactionScore;

	@Column(name = "average_Score")
	private Float averageScore;

	@Column(name = "submitted_Date")
	private Date submittedDate;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public TrainerFeedbackScore(TrainerFeedbackScoreDto trainerFeedbackScoreDto) {

		this.id = trainerFeedbackScoreDto.getId();

		this.scheduleTrainingId = trainerFeedbackScoreDto.getScheduleTrainingId();

//		 TrainingUserRegistrationMapping trainingUserRegistrationMapping;

//		if (trainerFeedbackScoreDto.getTraineeId() != null) {
//			TrainingUserRegistrationMapping trainingUserRegistrationMapping = new TrainingUserRegistrationMapping();
//			trainingUserRegistrationMapping.setId(trainerFeedbackScoreDto.getTraineeId());
//			this.trainingUserRegistrationMapping = trainingUserRegistrationMapping;
//		}

//		TrainerDetail trainerDetail;

//		if (trainerFeedbackScoreDto.getTrainerId() != null) {
//			TrainerDetail trainerDetail = new TrainerDetail();
//			trainerDetail.setId(trainerFeedbackScoreDto.getTrainerId());
//			this.trainerDetail = trainerDetail;
//		}

		this.userIdTrainee = trainerFeedbackScoreDto.getUserIdTrainee();

		this.userIdTrainer = trainerFeedbackScoreDto.getUserIdTrainer();

		this.technicalKnowledgeScore = trainerFeedbackScoreDto.getTechnicalKnowledgeScore();

		this.communicationScore = trainerFeedbackScoreDto.getCommunicationScore();

		this.interactionScore = trainerFeedbackScoreDto.getInteractionScore();

		this.averageScore = trainerFeedbackScoreDto.getAverageScore();

//		this.submittedDate = dateUtil.getDateToStringDDMMYYYY(trainerFeedbackScoreDto.getSubmittedDate());

		this.createdBy = trainerFeedbackScoreDto.getCreatedBy();

		this.updatedBy = trainerFeedbackScoreDto.getUpdatedBy();

		this.created_date = trainerFeedbackScoreDto.getCreated_date();

		this.updated_date = trainerFeedbackScoreDto.getUpdated_date();
	}
}
