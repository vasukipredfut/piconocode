package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.MaintainanceDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintainanceDetailsDto {

	private Long id;

	private Long empId;

	private String department;

	private String machineAlloted;

	private String maintainanceReq;

	private String maintainanceDone;

	private String maintainanceBreakdown;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public MaintainanceDetailsDto(MaintainanceDetails maintainanceDetails) {
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
