package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Interview;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewDto {
	private Long id;

	private Long branchId;

	private String branchName;

	private Long deptId;

	private String departmentName;

	private String candidateName;

	private String gender;

	private String email;

	private String caste;

	private String dateOfBirth;

	private String aadharNumber;

	private String phoneNumber;

	private String homeTown;

	private String qualification;

	private String marksPercentage;

	private String jobType;

	private String interviewDate;

	private String interviewTime;

	private String employeeType;

	private String interviewPosition;

	private String referance;

	private String batchNo;
	
	private String status;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	List<InterviewQualificationDto> qualifications;
	private List<QualificationDto> qualificationList;

	private int empType ;

	public InterviewDto(Interview interview) {
		this.id = interview.getId();
		this.candidateName = interview.getCandidateName();
		this.gender = interview.getGender();
		this.caste = interview.getCaste();
		this.aadharNumber = interview.getAadharNumber();
		this.phoneNumber = interview.getPhoneNumber();
		this.homeTown = interview.getHomeTown();
		this.qualification = interview.getQualification();
		this.marksPercentage = interview.getMarksPercentage();
		this.jobType = interview.getJobType();
		this.email = interview.getEmail();

		if (interview.getBranch() != null) {
			this.branchId = interview.getBranch().getId();
			this.branchName = interview.getBranch().getBranchName();
		}
		if (interview.getDepartmentDetails() != null) {
			this.deptId = interview.getDepartmentDetails().getId();
			this.departmentName = interview.getDepartmentDetails().getName();
		}
		this.employeeType = interview.getEmployeeType();
		this.interviewPosition = interview.getInterviewPosition();
		this.interviewTime = interview.getInterviewTime();
		this.referance = interview.getReferance();
		this.batchNo = interview.getBatchNo();
		this.status = interview.getStatus();
		this.createdBy = interview.getCreatedBy();
		this.updatedBy = interview.getUpdatedBy();
		this.created_date = interview.getCreated_date();
		this.updated_date = interview.getUpdated_date();
		this.qualifications = interview.getQualifications() != null ? interview.getQualifications().stream()
				.map(obj -> new InterviewQualificationDto(obj)).collect(Collectors.toList()) : null;

	}
}
