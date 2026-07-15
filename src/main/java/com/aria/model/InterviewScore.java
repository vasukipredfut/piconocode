package com.aria.model;

import com.aria.dto.InterviewScoreDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview_score")
public class InterviewScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "score_Id")
	private Long scoreId;
  
	@OneToOne
	@JoinColumn(name = "interview_id", referencedColumnName = "id")
	private Interview interviewDate ;
	
	@OneToOne
	@JoinColumn(name = "interviewer_id", referencedColumnName = "id")
	private User interviewer; 
	

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch interviewHeldPlace;


	@Column(name = "technical_Knowledge")
	private Long technicalKnowledge;

	@Column
	private Long communication;

	@Column(name = "average_Score")
	private Long averageScore;

	public InterviewScore(InterviewScoreDto interviewScore) {
		this.id = interviewScore.getId();
		this.scoreId = interviewScore.getScoreId();
		
		if (interviewScore.getInterviewId() != null) {
			Interview interviewDate = new Interview();
			interviewDate.setId(interviewScore.getInterviewId());
			this.interviewDate = interviewDate;
		}
		if (interviewScore.getInterviewerId() != null) {
			User user = new User();
		    user.setId(interviewScore.getInterviewerId());
			this.interviewer = user;
		}
		if (interviewScore.getBranchId() != null) {
			Branch interviewHeldPlace = new Branch();
			interviewHeldPlace.setId(interviewScore.getBranchId());
			this.interviewHeldPlace = interviewHeldPlace;
		}
		
		this.technicalKnowledge = interviewScore.getTechnicalKnowledge();
		this.communication = interviewScore.getCommunication();
		this.averageScore = interviewScore.getAverageScore();

	}
}
