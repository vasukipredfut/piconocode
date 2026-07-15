package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.ScoreAssessment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScoreAssessmentDto {

	private Long id;

	private String branch;
	
	private String interviewCommunicationSkill;
	
	private String interviewTechnicalSkill;
	
	private int industrialScore;

	private int foodSafteyScore;

	private int interviewScore;
	
	private int overallScore;
	
	private Long interviewId;
	
	private Long interviewAssignId;

	private String batchno;

	private String phoneNumber;

	private String status;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String interviewDate;
	
	private String interviewTime;

	private String candidateName;
	
	private String interviewerName;
	
	private String examinerName;

	public ScoreAssessmentDto(ScoreAssessment scoreAssessment) {
		this.id = scoreAssessment.getId();
		this.batchno = scoreAssessment.getBatchno();
		this.status = scoreAssessment.getStatus();
		this.createdBy = scoreAssessment.getCreatedBy();
		this.updatedBy = scoreAssessment.getUpdatedBy();
		this.created_date = scoreAssessment.getCreated_date();
		this.updated_date = scoreAssessment.getUpdated_date();
		
//		if (scoreAssessment.getInterviewerAssign().getInterviewer() != null) {
//			this.interviewAssignId = scoreAssessment.getInterviewerAssign().getId();
//			this.interviewerName = scoreAssessment.getInterviewerAssign().getInterviewer().getEmployeename();
//			this.examinerName = scoreAssessment.getInterviewerAssign().getExaminer().getEmployeename();
//			
//			this.interviewId = scoreAssessment.getInterviewerAssign().getInterview().getId();
//			this.interviewTime = scoreAssessment.getInterviewerAssign().getInterview().getInterviewTime();
//			this.candidateName = scoreAssessment.getInterviewerAssign().getInterview().getCandidateName();
//			this.phoneNumber = scoreAssessment.getInterviewerAssign().getInterview().getPhoneNumber();
//			this.branch = scoreAssessment.getInterviewerAssign().getInterview().getBranch().getBranchName();
//		}
		
		if (scoreAssessment.getInterview() != null) {
//			this.interviewAssignId = scoreAssessment.getInterviewerAssign().getId();
//			this.interviewerName = scoreAssessment.getInterview().getInterviewer().getEmployeename();
//			this.examinerName = scoreAssessment.getInterviewerAssign().getExaminer().getEmployeename();
			
			this.interviewId = scoreAssessment.getInterview().getId();
			this.interviewTime = scoreAssessment.getInterview().getInterviewTime();
			this.candidateName = scoreAssessment.getInterview().getCandidateName();
			this.phoneNumber = scoreAssessment.getInterview().getPhoneNumber();
			this.branch = scoreAssessment.getInterview().getBranch().getBranchName();
		}
		
		this.industrialScore = scoreAssessment.getIndustrial_score();
		this.foodSafteyScore = scoreAssessment.getFood_saftey_score();
		this.interviewScore = scoreAssessment.getInterview_score();
		this.interviewCommunicationSkill = scoreAssessment.getInterview_communication_skill();
		this.interviewTechnicalSkill = scoreAssessment.getInterview_technical_skill();
		this.overallScore = scoreAssessment.getOverall_score();

	}
}
