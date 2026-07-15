package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.MaintainanceDetailsDto;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "maintainance_details ")
public class MaintainanceDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long empId;
	
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column
	private String department;

	@Column
	private String machineAlloted;

	@Column
	private String maintainanceReq;

	@Column
	private String maintainanceDone;

	@Column
	private String maintainanceBreakdown;

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

	public MaintainanceDetails(MaintainanceDetailsDto maintainanceDetails) {
		this.id = maintainanceDetails.getId();
		this.empId = maintainanceDetails.getEmpId();
		this.department = maintainanceDetails.getDepartment();
        this.machineAlloted = maintainanceDetails.getMachineAlloted();
		this.maintainanceReq = maintainanceDetails.getMaintainanceReq();
		this.maintainanceDone = maintainanceDetails.getMaintainanceDone();
		this.maintainanceBreakdown = maintainanceDetails.getMaintainanceBreakdown();
		this.active = maintainanceDetails.getActive();
		this.createdBy = maintainanceDetails.getCreatedBy();
		this.updatedBy = maintainanceDetails.getUpdatedBy();
		this.created_date = maintainanceDetails.getCreated_date();
		this.updated_date = maintainanceDetails.getUpdated_date();
	}
}
