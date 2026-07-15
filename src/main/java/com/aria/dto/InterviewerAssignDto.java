package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.InterviewerAssign;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewerAssignDto {

	private Long id;

	private Long interviewId;

	private String interviewDate;

	private String interviewTime;

	private String candidateName;

	private Long interviewerId;
	
	private Long interviewerDesignationId;
	
	private String interviewerName;
	
	private String interviewerDesigName;

	private Long examinerId;
	
	private Long examinerDesignationId;
	
	private String examinerName;
	
	private String examinerDesigName;

	private Long branchId;

	private String place;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public InterviewerAssignDto(InterviewerAssign interviewerAssign) {
		this.id = interviewerAssign.getId();

		if (interviewerAssign.getInterview() != null) {
			this.interviewId = interviewerAssign.getInterview().getId();
			this.interviewTime = interviewerAssign.getInterview().getInterviewTime();
			this.candidateName = interviewerAssign.getInterview().getCandidateName();
		}

		if (interviewerAssign.getInterviewer() != null) {
			this.interviewerId = interviewerAssign.getInterviewer().getId();
			this.interviewerName = interviewerAssign.getInterviewer().getEmployeename();
			this.interviewerDesigName = interviewerAssign.getInterviewer().getDesignation().getName();
		}
		if (interviewerAssign.getInterviewerDesignation() != null) {
			this.interviewerDesignationId = interviewerAssign.getInterviewerDesignation().getId();
		}
		if (interviewerAssign.getExaminer() != null) {
			this.examinerId = interviewerAssign.getExaminer().getId();
			this.examinerName = interviewerAssign.getExaminer().getEmployeename();
			this.examinerDesigName = interviewerAssign.getExaminer().getDesignation().getName();
		}
		if (interviewerAssign.getExaminerDesignation() != null) {
			this.examinerDesignationId = interviewerAssign.getExaminerDesignation().getId();
		}
		if (interviewerAssign.getPlace() != null) {
			this.branchId = interviewerAssign.getPlace().getId();
			this.place = interviewerAssign.getPlace().getBranchName();
		}

		this.createdBy = interviewerAssign.getCreatedBy();
		this.updatedBy = interviewerAssign.getUpdatedBy();
		this.created_date = interviewerAssign.getCreated_date();
		this.updated_date = interviewerAssign.getUpdated_date();

	}
}
