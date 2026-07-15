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

import com.aria.dto.TraineeAnswerMultipleChoiceQuestionDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "trainee_answer_multiple_choice_question")
public class TraineeAnswerMultipleChoiceQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
	private TrainingUserRegistrationMapping trainee;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "multiple_choice_question_id", referencedColumnName = "id", nullable = false)
	private MultipleChoiceQuestion multipleChoiceQuestion;

	@Column(name = "test_Exam_Score_Id")
	private Long testExamScoreId;

	@Column(name = "trainee_Answer")
	private String traineeAnswer;

	@Column(name = "trainee_Score")
	private Float traineeScore;

	@Column(name = "submitted_Date")
	private Date submittedDate;

	@Column(name = "evaluation_Date")
	private Date evaluationDate;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public TraineeAnswerMultipleChoiceQuestion(
			TraineeAnswerMultipleChoiceQuestionDto traineeAnswerMultipleChoiceQuestionDto) {

		this.id = traineeAnswerMultipleChoiceQuestionDto.getId();

		if (traineeAnswerMultipleChoiceQuestionDto.getMultipleChoiceQuestionId() != null) {
			MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion();
			multipleChoiceQuestion.setId(traineeAnswerMultipleChoiceQuestionDto.getMultipleChoiceQuestionId());
			this.multipleChoiceQuestion = multipleChoiceQuestion;
		}

		this.testExamScoreId = traineeAnswerMultipleChoiceQuestionDto.getTestExamScoreId();

//		this.trainingUserRegistrationMapping;

		if (traineeAnswerMultipleChoiceQuestionDto.getTraineeId() != null) {
			TrainingUserRegistrationMapping trainee = new TrainingUserRegistrationMapping();
			trainee.setId(traineeAnswerMultipleChoiceQuestionDto.getTraineeId());
			this.trainee = trainee;
		}

		this.traineeAnswer = traineeAnswerMultipleChoiceQuestionDto.getTraineeAnswer();

		this.traineeScore = traineeAnswerMultipleChoiceQuestionDto.getTraineeScore();

//		this.submittedDate = traineeAnswerMultipleChoiceQuestionDto.getSubmittedDate();

//		this.evaluationDate = traineeAnswerMultipleChoiceQuestionDto.getEvaluationDate();

		this.createdBy = traineeAnswerMultipleChoiceQuestionDto.getCreatedBy();

		this.updatedBy = traineeAnswerMultipleChoiceQuestionDto.getUpdatedBy();

		this.created_date = traineeAnswerMultipleChoiceQuestionDto.getCreated_date();

		this.updated_date = traineeAnswerMultipleChoiceQuestionDto.getUpdated_date();
	}
}
