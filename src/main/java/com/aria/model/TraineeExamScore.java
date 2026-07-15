package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.TraineeExamScoreDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "trainee_exam_score")
public class TraineeExamScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
	private TrainingUserRegistrationMapping trainingUserRegistrationMapping;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_detail_id", referencedColumnName = "id")
	private TrainerDetail trainerDetail;

	@OneToMany(mappedBy = "testExamScoreId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<TraineeAnswerMultipleChoiceQuestion> traineeAnswerMultipleChoiceQuestions;

	@OneToMany(mappedBy = "testExamScoreId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<TraineeAnswerOpenEndedQuestion> traineeAnswerOpenEndedQuestions;

	@Column(name = "schedule_Training_Id")
	private Long scheduleTrainingId;

	@Column(name = "is_Answer_Sheet_Attached")
	private Boolean isAnswerSheetAttached;

	@Column(name = "answer_Sheet_Upload_Path")
	private String answerSheetUploadPath;

	@Column(name = "answer_Sheet_File_Name")
	private String answerSheetFileName;

	@Column(name = "answer_Sheet_File_Type")
	private String answerSheetFileType;

	@Column(name = "test_Score")
	private Float testScore;

	@Column(name = "interaction_Score")
	private Float interactionScore;

	@Column(name = "ontime_Score")
	private Float ontimeScore;

	@Column(name = "total_Score")
	private Float totalScore;

	@Column(name = "evaluation_Status")
	private String evaluationStatus;

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

	public TraineeExamScore(TraineeExamScoreDto traineeExamScoreDto) {

		this.id = traineeExamScoreDto.getId();

//		 TrainingUserRegistrationMapping trainingUserRegistrationMapping;
//		if (traineeExamScoreDto.getTraineeId() != null) {
//			TrainingUserRegistrationMapping trainingUserRegistrationMapping = new TrainingUserRegistrationMapping();
//			trainingUserRegistrationMapping.setId(traineeExamScoreDto.getTraineeId());
//			this.trainingUserRegistrationMapping = trainingUserRegistrationMapping;
//		}

//		 TrainerDetail trainerDetail;
//		if (traineeExamScoreDto.getTrainerId() != null) {
//			TrainerDetail trainerDetail = new TrainerDetail();
//			trainerDetail.setId(traineeExamScoreDto.getTrainerId());
//			this.trainerDetail = trainerDetail;
//		}

		this.scheduleTrainingId = traineeExamScoreDto.getScheduleTrainingId();

		this.isAnswerSheetAttached = traineeExamScoreDto.getIsAnswerSheetAttached();

		this.answerSheetUploadPath = traineeExamScoreDto.getAnswerSheetUploadPath();

		this.answerSheetFileName = traineeExamScoreDto.getAnswerSheetFileName();

		this.answerSheetFileType = traineeExamScoreDto.getAnswerSheetFileType();

		this.testScore = traineeExamScoreDto.getTestScore();

		this.interactionScore = traineeExamScoreDto.getInteractionScore();

		this.ontimeScore = traineeExamScoreDto.getOntimeScore();

		this.totalScore = traineeExamScoreDto.getTotalScore();

		this.evaluationStatus = traineeExamScoreDto.getEvaluationStatus();

//		  this.evaluationDate = traineeExamScoreDto.getEvaluationDate();

		this.createdBy = traineeExamScoreDto.getCreatedBy();

		this.updatedBy = traineeExamScoreDto.getUpdatedBy();

		this.created_date = traineeExamScoreDto.getCreated_date();

		this.updated_date = traineeExamScoreDto.getUpdated_date();
	}
}
