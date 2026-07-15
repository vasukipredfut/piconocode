package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.DutyUnplanned;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DutyUnplannedDto {
	private Long id;

	private Long userId;

    private String userName;

	private Long deptId;

    private String deptName;

	private String employeeCategory;

	private String dutyFrom;

	private String dutyTo;

	private String dutyName;

	private String dutyDescription;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public DutyUnplannedDto(DutyUnplanned dutyUnplanned) {
		this.id = dutyUnplanned.getId();
		if(dutyUnplanned.getDepartmentDetails() != null) {
            this.deptId = dutyUnplanned.getDepartmentDetails().getId();
            this.deptName = dutyUnplanned.getDepartmentDetails().getName();
        }
		if (dutyUnplanned.getUser() != null) {
			this.userId = dutyUnplanned.getUser().getId();
			this.userName = dutyUnplanned.getUser().getEmployeename();
		}
		this.employeeCategory = dutyUnplanned.getEmployeeCategory();
	    this.dutyName = dutyUnplanned.getDutyname();
		this.dutyDescription = dutyUnplanned.getDutydescription();
		this.createdBy = dutyUnplanned.getCreatedBy();
		this.updatedBy = dutyUnplanned.getUpdatedBy();
		this.created_date = dutyUnplanned.getCreated_date();
		this.updated_date = dutyUnplanned.getUpdated_date();
	}
}
