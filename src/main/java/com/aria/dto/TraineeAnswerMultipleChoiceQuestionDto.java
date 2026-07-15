package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.TraineeAnswerMultipleChoiceQuestion;
import com.aria.utils.DateUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class TraineeAnswerMultipleChoiceQuestionDto {

	private Long id;

//	MultipleChoiceQuestion multipleChoiceQuestion;

	private Long multipleChoiceQuestionId;

	private String multipleChoiceQuestionName;

	private Long testExamScoreId;

	private Integer serialNo;

	private String question;

	private String optionA;

	private String optionB;

	private String optionC;

	private String optionD;

	private String rightAnswer;

	private Float score;

//	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	private Long traineeId;

	private String traineeName;

	private String traineeAnswer;

	private Float traineeScore;

	private String submittedDate;

	private String evaluationDate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public TraineeAnswerMultipleChoiceQuestionDto(
			TraineeAnswerMultipleChoiceQuestion traineeAnswerMultipleChoiceQuestion) {

		this.id = traineeAnswerMultipleChoiceQuestion.getId();

		//
		if (traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion() != null) {
			this.multipleChoiceQuestionId = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getId();
			this.multipleChoiceQuestionName = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion()
					.getQuestion();
			this.serialNo = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getSerialNo();
			this.question = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getQuestion();
			this.optionA = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getOptionA();
			this.optionB = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getOptionB();
			this.optionC = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getOptionC();
			this.optionD = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getOptionD();
			this.rightAnswer = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getRightAnswer();
			this.score = traineeAnswerMultipleChoiceQuestion.getMultipleChoiceQuestion().getScore();
		}

		this.testExamScoreId = traineeAnswerMultipleChoiceQuestion.getTestExamScoreId();

//		 TrainingUserRegistrationMapping trainingUserRegistrationMapping;
		if (traineeAnswerMultipleChoiceQuestion.getTrainee() != null
				&& traineeAnswerMultipleChoiceQuestion.getTrainee().getUser() != null) {
			this.traineeId = traineeAnswerMultipleChoiceQuestion.getTrainee().getUser().getId();
			this.traineeName = traineeAnswerMultipleChoiceQuestion.getTrainee().getUser().getEmployeename();
		}

		this.traineeAnswer = traineeAnswerMultipleChoiceQuestion.getTraineeAnswer();

		this.traineeScore = traineeAnswerMultipleChoiceQuestion.getTraineeScore();

		DateUtil dateUtil = new DateUtil();

		this.submittedDate = dateUtil.getDateToStringDDMMYYYY(traineeAnswerMultipleChoiceQuestion.getSubmittedDate());

		this.evaluationDate = dateUtil.getDateToStringDDMMYYYY(traineeAnswerMultipleChoiceQuestion.getEvaluationDate());

		this.createdBy = traineeAnswerMultipleChoiceQuestion.getCreatedBy();

		this.updatedBy = traineeAnswerMultipleChoiceQuestion.getUpdatedBy();

	}
}
