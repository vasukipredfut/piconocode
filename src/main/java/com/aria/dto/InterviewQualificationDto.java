package com.aria.dto;

import com.aria.model.InterviewQualification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewQualificationDto {
	private Long id;

	private Long interviewId;

	private String previousCompany;

	private String designation;

	private String experienceFrom;

	private String experienceTo;

	public InterviewQualificationDto(InterviewQualification interviewQualification) {
		this.id = interviewQualification.getId();
		this.interviewId = interviewQualification.getInterviewId();
		this.previousCompany = interviewQualification.getPreviousCompany();
		this.designation = interviewQualification.getDesignation();

	}

}
