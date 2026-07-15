package com.aria.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import com.aria.model.TraineeExamScore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TraineeExamScoreDto {

	private Long id;

//	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	private Long traineeId;

	private String traineeName;

//	private TrainerDetail trainerDetail;

	private Long trainerId;

	private String trainerName;

	private LinkedHashSet<TraineeAnswerMultipleChoiceQuestionDto> traineeAnswerMultipleChoiceQuestionDtos;

	private LinkedHashSet<TraineeAnswerOpenEndedQuestionDto> traineeAnswerOpenEndedQuestionDtos;

	private Long scheduleTrainingId;

	private Boolean isAnswerSheetAttached;

	private String answerSheetUploadPath;

	private String answerSheetFileName;

	private String answerSheetFileType;

	private Float testScore;

	private Float interactionScore;

	private Float ontimeScore;

	private Float totalScore;

	private String evaluationStatus;

	private String evaluationDate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public TraineeExamScoreDto(TraineeExamScore traineeExamScore) {

		this.id = traineeExamScore.getId();

//		 this.trainingUserRegistrationMapping;

		if (traineeExamScore.getTrainingUserRegistrationMapping() != null
				&& traineeExamScore.getTrainingUserRegistrationMapping().getUser() != null) {
			this.traineeId = traineeExamScore.getTrainingUserRegistrationMapping().getUser().getId();
			this.traineeName = traineeExamScore.getTrainingUserRegistrationMapping().getUser().getEmployeename();
		}

//		 this.trainerDetail;

		if (traineeExamScore.getTrainerDetail() != null && traineeExamScore.getTrainerDetail().getTrainer() != null) {
			this.trainerId = traineeExamScore.getTrainerDetail().getTrainer().getId();
			this.trainerName = traineeExamScore.getTrainerDetail().getTrainer().getEmployeename();
		}

		this.traineeAnswerMultipleChoiceQuestionDtos = traineeExamScore.getTraineeAnswerMultipleChoiceQuestions()
				.stream().map(data -> {
					return new TraineeAnswerMultipleChoiceQuestionDto(data);
				}).collect(Collectors.toCollection(LinkedHashSet::new));

		this.traineeAnswerOpenEndedQuestionDtos = traineeExamScore.getTraineeAnswerOpenEndedQuestions().stream()
				.map(data -> {
					return new TraineeAnswerOpenEndedQuestionDto(data);
				}).collect(Collectors.toCollection(LinkedHashSet::new));

		this.scheduleTrainingId = traineeExamScore.getScheduleTrainingId();

		this.isAnswerSheetAttached = traineeExamScore.getIsAnswerSheetAttached();

		this.answerSheetUploadPath = traineeExamScore.getAnswerSheetUploadPath();

		this.answerSheetFileName = traineeExamScore.getAnswerSheetFileName();

		this.answerSheetFileType = traineeExamScore.getAnswerSheetFileType();

		this.testScore = traineeExamScore.getTestScore();

		this.interactionScore = traineeExamScore.getInteractionScore();

		this.ontimeScore = traineeExamScore.getOntimeScore();

		this.totalScore = traineeExamScore.getTotalScore();

		this.evaluationStatus = traineeExamScore.getEvaluationStatus();

//		  this.evaluationDate = traineeExamScore.getEvaluationDate();

		this.createdBy = traineeExamScore.getCreatedBy();

		this.updatedBy = traineeExamScore.getUpdatedBy();

		this.created_date = traineeExamScore.getCreated_date();

		this.updated_date = traineeExamScore.getUpdated_date();
	}
}
