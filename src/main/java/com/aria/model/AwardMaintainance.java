package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

import com.aria.dto.AwardMaintainanceDto;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "award_maintainance")
public class AwardMaintainance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column(name = "award_category")
	private String awardCategory;

	@Column(name = "award_date")
	private Date awardDate;

	@Column(name = "employee_category")
	private String employeeCategory;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	@OneToMany(mappedBy = "empId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "active=0")
	private List<MaintainanceDetails> maintainanceDetails;

	public AwardMaintainance(AwardMaintainanceDto awardMaintainance) {
		this.id = awardMaintainance.getId();
		if (awardMaintainance.getBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(awardMaintainance.getBranchId());
			this.branch = branch;
		}
		if (awardMaintainance.getEmpId() != null) {
			User user = new User();
			user.setId(awardMaintainance.getEmpId());
			this.user = user;
		}

		this.awardCategory = awardMaintainance.getAwardCategory();

		this.employeeCategory = awardMaintainance.getEmployeeCategory();

		this.active = awardMaintainance.getActive();
		this.createdBy = awardMaintainance.getCreatedBy();
		this.updatedBy = awardMaintainance.getUpdatedBy();
		this.created_date = awardMaintainance.getCreated_date();
		this.updated_date = awardMaintainance.getCreated_date();

	}
}
