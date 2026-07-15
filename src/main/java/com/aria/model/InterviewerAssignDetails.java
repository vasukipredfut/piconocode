package com.aria.model;

import com.aria.dto.InterviewerAssignDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "interviewer_assign_details")
public class InterviewerAssignDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "interviewer_id")
	private Long interviewerId;

	@OneToOne
	@JoinColumn(name = "interview_id", referencedColumnName = "id")
	private Interview candidateName;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User examinerName;

	@OneToOne
	@JoinColumn(name = "designation_id", referencedColumnName = "id", nullable = false)
	private Designation examinerDesignation;

	@OneToOne
	@JoinColumn(name = "users_id", referencedColumnName = "id", nullable = false)
	private User interviewerName;

	@OneToOne
	@JoinColumn(name = "desgn_id", referencedColumnName = "id", nullable = false)
	private Designation interviewerDesignation;

	public InterviewerAssignDetails(InterviewerAssignDetailsDto interviewerAssignDetails) {
		this.id = interviewerAssignDetails.getId();
		this.interviewerId = interviewerAssignDetails.getInterviewerId();
		if (interviewerAssignDetails.getInterviewId() != null) {
			Interview interview = new Interview();
			interview.setId(interviewerAssignDetails.getInterviewId());
			this.candidateName = interview;
		}

		if (interviewerAssignDetails.getUserId() != null) {
			User user = new User();
			user.setId(interviewerAssignDetails.getUserId());
			this.examinerName = user;
		}
		if (interviewerAssignDetails.getDesignationId() != null) {
			Designation designation = new Designation();
			designation.setId(interviewerAssignDetails.getDesignationId());
			this.examinerDesignation = designation;
		}
		if (interviewerAssignDetails.getUsersId() != null) {
			User user = new User();
			user.setId(interviewerAssignDetails.getUsersId());
			this.interviewerName = user;
		}
		if (interviewerAssignDetails.getDesgnId() != null) {
			Designation designation = new Designation();
			designation.setId(interviewerAssignDetails.getDesgnId());
			this.interviewerDesignation = designation;
		}
	}
}
