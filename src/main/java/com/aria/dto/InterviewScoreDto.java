package com.aria.dto;

import java.util.Date;

import com.aria.model.InterviewScore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewScoreDto {
	private Long id;
	
	private Long scoreId;

	private Long branchId;

	private String branchName;

	private Long interviewId;

	private Date interviewDate;
	
	private Long interviewerId;
	
	private String interviewer;
	
	private Long technicalKnowledge;

	private Long communication;

	private Long averageScore;

	public InterviewScoreDto(InterviewScore interviewScore) {
		this.id = interviewScore.getId();
		this.scoreId = interviewScore.getScoreId();
		if (interviewScore.getInterviewDate() != null) {
			this.interviewId = interviewScore.getInterviewDate().getId();
			this.interviewDate = interviewScore.getInterviewDate().getInterviewDate();
		}
		if (interviewScore.getInterviewer() != null) {
			this.interviewerId = interviewScore.getInterviewer().getId();
			this.interviewer = interviewScore.getInterviewer().getEmployeename();
		}
		
		if (interviewScore.getInterviewHeldPlace() != null) {
			this.branchId = interviewScore.getInterviewHeldPlace().getId();
			this.branchName = interviewScore.getInterviewHeldPlace().getBranchName();
		}
		this.technicalKnowledge = interviewScore.getTechnicalKnowledge();
		this.communication = interviewScore.getCommunication();
		this.averageScore = interviewScore.getAverageScore();

	}
}
