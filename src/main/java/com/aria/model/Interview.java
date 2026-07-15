
package com.aria.model;

import com.aria.dto.InterviewDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interview")
public class Interview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "candidate_name")
	private String candidateName;

	@Column
	private String gender;

	@Column
	private String email;

	@Column
	private String caste;

	@Column(name = "date_Of_Birth")
	private Date dateOfBirth;

	@Column(name = "aadhar_Number")
	private String aadharNumber;

	@Column(name = "phone_Number")
	private String phoneNumber;

	@Column(name = "home_Town")
	private String homeTown;

	@Column
	private String qualification;

	@Column(name = "marks_Percentage")
	private String marksPercentage;

	@Column(name = "job_Type")
	private String jobType;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails departmentDetails;

	@Column(name = "interview_Date")
	private Date interviewDate;

	@Column(name = "interview_Time")
	private String interviewTime;

	@Column(name = "employee_Type")
	private String employeeType;

	@Column(name = "interview_Position")
	private String interviewPosition;

	@Column
	private String referance;

	@Column(name = "batch_No")
	private String batchNo;


	@Column(name = "created_By", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToMany(mappedBy = "interviewId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<InterviewQualification> qualifications;
	
	@Column
	private String status;

	public Interview(InterviewDto interview) {
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

		if (interview.getBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(interview.getBranchId());
			this.branch = branch;
		}
		if (interview.getDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(interview.getDeptId());
			this.departmentDetails = departmentDetails;
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
	}

}