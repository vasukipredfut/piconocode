package com.aria.model;

import com.aria.dto.ScoreAssessmentDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "score_assessment")
public class ScoreAssessment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;


//	@OneToOne
//	@JoinColumn(name = "interview_assign_id", referencedColumnName = "id")
//	private InterviewerAssign interviewerAssign;
	
	@OneToOne
	@JoinColumn(name = "interview_id", referencedColumnName = "id")
	private Interview interview;

	@Column
	private Integer food_saftey_score;

	@Column
	private Integer industrial_score;

	@Column
	private Integer interview_score;
	
	@Column
	private Integer overall_score;

	@Column
	private String interview_communication_skill;

	@Column
	private String interview_technical_skill;

	@Column
	private String batchno;

	@Column(name = "phone_Number")
	private String phoneNumber;

	@Column
	private String status;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;





	public ScoreAssessment(ScoreAssessmentDto scoreAssessment) {
		this.id = scoreAssessment.getId();
		this.batchno = scoreAssessment.getBatchno();
		this.phoneNumber = scoreAssessment.getPhoneNumber();
		this.status = scoreAssessment.getStatus();
		this.industrial_score = scoreAssessment.getIndustrialScore();
		this.food_saftey_score = scoreAssessment.getFoodSafteyScore();
		this.interview_score = scoreAssessment.getInterviewScore();
		this.interview_communication_skill = scoreAssessment.getInterviewCommunicationSkill();
		this.interview_technical_skill = scoreAssessment.getInterviewTechnicalSkill();

//		if (scoreAssessment.getInterviewAssignId() != null) {
//			InterviewerAssign interview = new InterviewerAssign();
//			interview.setId(scoreAssessment.getInterviewAssignId());
//			this.interviewerAssign = interview;
//		}
		if (scoreAssessment.getInterviewId() != null) {
			Interview interview = new Interview();
			interview.setId(scoreAssessment.getInterviewId());
			this.interview = interview;
		}
		this.overall_score = scoreAssessment.getOverallScore();
		this.createdBy = scoreAssessment.getCreatedBy();
		this.updatedBy = scoreAssessment.getUpdatedBy();
		this.created_date = scoreAssessment.getCreated_date();
		this.updated_date = scoreAssessment.getUpdated_date();
	}
}
