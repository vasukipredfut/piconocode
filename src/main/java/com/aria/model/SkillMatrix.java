package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SkillMatrixDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "skill_matrix")
public class SkillMatrix {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User user;

	@Column
	private Date date;

	@Column
	private Date updated_date;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;


	@OneToOne
	@JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
	private DepartmentDetails department;

	@OneToOne
	@JoinColumn(name = "designation_id", referencedColumnName = "id", nullable = false)
	private Designation designation;

	@OneToMany(mappedBy = "skillMatrixId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SkillMatrixGradeLevel> skillMatrixGradeLevels;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_exam_score_id", referencedColumnName = "id")
	private TraineeExamScore traineeExamScore;

	public SkillMatrix(SkillMatrixDto skillMatrixDto) {
		this.id = skillMatrixDto.getId();

		if (skillMatrixDto.getUserId() != null) {
			User user = new User();
			user.setId(skillMatrixDto.getUserId());
			this.user = user;
		}

		if (skillMatrixDto.getDepartmentId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(skillMatrixDto.getDepartmentId());
			this.department = departmentDetails;
		}

		if (skillMatrixDto.getDesignationId() != null) {
			Designation designation = new Designation();
			designation.setId(skillMatrixDto.getDesignationId());
			this.designation = designation;
		}

		this.createdBy = skillMatrixDto.getCreatedBy();
		this.updatedBy = skillMatrixDto.getUpdatedBy();
		this.created_date = skillMatrixDto.getCreated_date();
		this.updated_date = skillMatrixDto.getUpdated_date();
	}
}
