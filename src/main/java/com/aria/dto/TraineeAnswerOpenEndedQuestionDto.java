package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.TraineeAnswerOpenEndedQuestion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TraineeAnswerOpenEndedQuestionDto {

	private Long id;

//	private OpenEndedQuestion openEndedQuestion;

	private Long openEndedQuestioId;

	private Integer serialNo;

	private String question;

	private String rightAnswer;

	private Float score;

	private Long testExamScoreId;

//	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	private Long traineeId;

	private String traineeName;

	private String traineeAnswer;

	private Float traineeScore;

	private Date submittedDate;

	private Date evaluationDate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public TraineeAnswerOpenEndedQuestionDto(TraineeAnswerOpenEndedQuestion traineeAnswerOpenEndedQuestion) {

		this.id = traineeAnswerOpenEndedQuestion.getId();

//		 OpenEndedQuestion openEndedQuestion;

		if (traineeAnswerOpenEndedQuestion.getOpenEndedQuestion() != null) {
			this.openEndedQuestioId = traineeAnswerOpenEndedQuestion.getOpenEndedQuestion().getId();
			this.serialNo = traineeAnswerOpenEndedQuestion.getOpenEndedQuestion().getSerialNo();
			this.question = traineeAnswerOpenEndedQuestion.getOpenEndedQuestion().getQuestion();
			this.rightAnswer = traineeAnswerOpenEndedQuestion.getOpenEndedQuestion().getRightAnswer();
			this.score = traineeAnswerOpenEndedQuestion.getOpenEndedQuestion().getScore();
		}

//		 TrainingUserRegistrationMapping trainingUserRegistrationMapping;

		this.testExamScoreId = traineeAnswerOpenEndedQuestion.getTestExamScoreId();

		if (traineeAnswerOpenEndedQuestion.getTrainee() != null
				&& traineeAnswerOpenEndedQuestion.getTrainee().getUser() != null) {
			this.traineeId = traineeAnswerOpenEndedQuestion.getTrainee().getUser().getId();
			this.traineeName = traineeAnswerOpenEndedQuestion.getTrainee().getUser().getEmployeename();
		}

		this.traineeAnswer = traineeAnswerOpenEndedQuestion.getTraineeAnswer();

		this.traineeScore = traineeAnswerOpenEndedQuestion.getTraineeScore();

		this.submittedDate = traineeAnswerOpenEndedQuestion.getSubmittedDate();

		this.evaluationDate = traineeAnswerOpenEndedQuestion.getEvaluationDate();

		this.createdBy = traineeAnswerOpenEndedQuestion.getCreatedBy();

		this.updatedBy = traineeAnswerOpenEndedQuestion.getUpdatedBy();

		this.created_date = traineeAnswerOpenEndedQuestion.getUpdated_date();

		this.updated_date = traineeAnswerOpenEndedQuestion.getUpdated_date();
	}
}
