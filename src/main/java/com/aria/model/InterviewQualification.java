
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.aria.dto.InterviewQualificationDto;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview_qualification")
public class InterviewQualification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "interview_id")
	private Long interviewId;

	@Column(name = "previous_company")
	private String previousCompany;

	@Column
	private String designation;

	@Column(name = "experience_from")
	private Date experienceFrom;

	@Column(name = "experience_to")
	private Date experienceTo;

	public InterviewQualification(InterviewQualificationDto interviewQualification) {
		this.id = interviewQualification.getId();
		this.interviewId = interviewQualification.getInterviewId();
		this.previousCompany = interviewQualification.getPreviousCompany();
		this.designation = interviewQualification.getDesignation();
	}
}