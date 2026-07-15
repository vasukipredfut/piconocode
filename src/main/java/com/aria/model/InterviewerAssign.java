
package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.InterviewerAssignDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interviewer_assign")
public class InterviewerAssign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "interview_id", referencedColumnName = "id")
	private Interview interview;
	
	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch place;
	
	@OneToOne
	@JoinColumn(name = "interviewer_id", referencedColumnName = "id")
	private User interviewer;
	
	@OneToOne
	@JoinColumn(name = "interviewer_designation_id", referencedColumnName = "id")
	private Designation interviewerDesignation;
	
	@OneToOne
	@JoinColumn(name = "examiner_id", referencedColumnName = "id")
	private User examiner;
	
	@OneToOne
	@JoinColumn(name = "examiner_designation_id", referencedColumnName = "id")
	private Designation examinerDesignation;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
//	@OneToMany(mappedBy = "interviewerId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	private List<InterviewerAssignDetails> interviewerAssignDetails;
	
	public InterviewerAssign(InterviewerAssignDto interviewerAssign) {
		this.id = interviewerAssign.getId();

		if (interviewerAssign.getInterviewId() != null) {
			Interview interview = new Interview();
			interview.setId(interviewerAssign.getInterviewId());
			this.interview = interview;
		}
		if (interviewerAssign.getInterviewerId() != null) {
			User user = new User();
			user.setId(interviewerAssign.getInterviewerId());
			this.interviewer = user;
		}
		if (interviewerAssign.getInterviewerDesignationId() != null) {
			Designation desig = new Designation();
			desig.setId(interviewerAssign.getInterviewerDesignationId());
			this.interviewerDesignation = desig;
		}
		if (interviewerAssign.getExaminerId() != null) {
			User user = new User();
			user.setId(interviewerAssign.getExaminerId());
			this.examiner = user;
		}
		if (interviewerAssign.getExaminerDesignationId() != null) {
			Designation desig = new Designation();
			desig.setId(interviewerAssign.getExaminerDesignationId());
			this.examinerDesignation = desig;
		}
		if (interviewerAssign.getBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(interviewerAssign.getBranchId());
			this.place = branch;
		}
		this.createdBy = interviewerAssign.getCreatedBy();
		this.updatedBy = interviewerAssign.getUpdatedBy();
		this.created_date = interviewerAssign.getCreated_date();
		this.updated_date = interviewerAssign.getUpdated_date();

	}
}
