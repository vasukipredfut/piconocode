package com.aria.dto;

import com.aria.model.InterviewerAssignDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewerAssignDetailsDto {
	private Long id;

	private Long interviewerId;

	private Long interviewId;

	private String candidateName;

	private Long userId;

	private String examinerName;

	private Long designationId;

	private String examinerDesignation;

	private Long usersId;

	private String interviewerName;

	private Long desgnId;

	private String interviewerDesignation;

	public InterviewerAssignDetailsDto(InterviewerAssignDetails interviewerAssignDetails) {
		this.id = interviewerAssignDetails.getId();
		this.id = interviewerAssignDetails.getInterviewerId();
		if (interviewerAssignDetails.getCandidateName() != null) {
			this.interviewId = interviewerAssignDetails.getCandidateName().getId();
			this.candidateName = interviewerAssignDetails.getCandidateName().getCandidateName();
		}
		if (interviewerAssignDetails.getExaminerName() != null) {
			this.userId = interviewerAssignDetails.getExaminerName().getId();
			this.examinerName = interviewerAssignDetails.getExaminerName().getEmployeename();
		}

		if (interviewerAssignDetails.getExaminerDesignation() != null) {
			this.designationId = interviewerAssignDetails.getExaminerDesignation().getId();
			this.examinerDesignation = interviewerAssignDetails.getExaminerDesignation().getName();
		}

		if (interviewerAssignDetails.getInterviewerName() != null) {
			this.usersId = interviewerAssignDetails.getInterviewerName().getId();
			this.interviewerName = interviewerAssignDetails.getInterviewerName().getEmployeename();
		}

		if (interviewerAssignDetails.getInterviewerDesignation() != null) {
			this.desgnId = interviewerAssignDetails.getInterviewerDesignation().getId();
			this.interviewerDesignation = interviewerAssignDetails.getInterviewerDesignation().getName();
		}

	}
}
