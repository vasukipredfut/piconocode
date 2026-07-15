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

import com.aria.dto.TraineeAnswerOpenEndedQuestionDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "trainee_answer_open_ended_question")
public class TraineeAnswerOpenEndedQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "open_ended_question_id", referencedColumnName = "id", nullable = false)
	private OpenEndedQuestion openEndedQuestion;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
	private TrainingUserRegistrationMapping trainee;

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

	public TraineeAnswerOpenEndedQuestion(TraineeAnswerOpenEndedQuestionDto traineeAnswerOpenEndedQuestionDto) {
		this.id = traineeAnswerOpenEndedQuestionDto.getId();

		if (traineeAnswerOpenEndedQuestionDto.getOpenEndedQuestioId() != null) {
			OpenEndedQuestion openEndedQuestion = new OpenEndedQuestion();
			openEndedQuestion.setId(traineeAnswerOpenEndedQuestionDto.getOpenEndedQuestioId());
			this.openEndedQuestion = openEndedQuestion;
		}

//		this.trainingUserRegistrationMapping;

		this.testExamScoreId = traineeAnswerOpenEndedQuestionDto.getTestExamScoreId();

		if (traineeAnswerOpenEndedQuestionDto.getTraineeId() != null) {
			TrainingUserRegistrationMapping trainee = new TrainingUserRegistrationMapping();
			trainee.setId(traineeAnswerOpenEndedQuestionDto.getTraineeId());
			this.trainee = trainee;
		}

		this.traineeAnswer = traineeAnswerOpenEndedQuestionDto.getTraineeAnswer();

		this.traineeScore = traineeAnswerOpenEndedQuestionDto.getTraineeScore();

		this.submittedDate = traineeAnswerOpenEndedQuestionDto.getSubmittedDate();

		this.evaluationDate = traineeAnswerOpenEndedQuestionDto.getEvaluationDate();

		this.createdBy = traineeAnswerOpenEndedQuestionDto.getCreatedBy();

		this.updatedBy = traineeAnswerOpenEndedQuestionDto.getUpdatedBy();

		this.created_date = traineeAnswerOpenEndedQuestionDto.getCreated_date();

		this.updated_date = traineeAnswerOpenEndedQuestionDto.getUpdated_date();
	}
}
