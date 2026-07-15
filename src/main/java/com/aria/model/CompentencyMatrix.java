package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import com.aria.dto.CompentencyMatrixDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "compentency_matrix")
public class CompentencyMatrix {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "desgination_id", referencedColumnName = "id")
	private Designation designation;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails department;

	@Column
	private Date date;

	@Column(name = "created_by",updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	 @OneToMany(mappedBy = "competencyId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	 private List<SkillDetails> skillDetails;

	public CompentencyMatrix(CompentencyMatrixDto compentencyMatrix) {
		this.id = compentencyMatrix.getId();
		if (compentencyMatrix.getDesignationId() != null) {
			Designation designation = new Designation();
			designation.setId(compentencyMatrix.getDesignationId());
			this.designation = designation;
		}

		if (compentencyMatrix.getDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(compentencyMatrix.getDeptId());
			this.department = departmentDetails;
		}

		this.createdBy = compentencyMatrix.getCreatedBy();
		this.updatedBy = compentencyMatrix.getUpdatedBy();
		this.created_date = compentencyMatrix.getCreated_date();
		this.updated_date = compentencyMatrix.getUpdated_date();
	}
}
