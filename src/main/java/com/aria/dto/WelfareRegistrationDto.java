package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.WelfareRegistration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WelfareRegistrationDto {

	private Long id;

	private String deptName;

	private Long deptID;

	private Long branchId;

	private String branchName;

	private String welfareName;

	private String fromDate;
	
	private String toDate;

	private String employeeType;

	private String selectEmployee;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<WelfareEmployeeDto> welfareEmployee;

	public WelfareRegistrationDto(WelfareRegistration welfareRegistration) {
		this.id = welfareRegistration.getId();
		if (welfareRegistration.getDepartmentDetails() != null) {
			this.deptID = welfareRegistration.getDepartmentDetails().getId();
			this.deptName = welfareRegistration.getDepartmentDetails().getName();
		}
		if (welfareRegistration.getBranch() != null) {
			this.branchId = welfareRegistration.getBranch().getId();
			this.branchName = welfareRegistration.getBranch().getBranchName();
		}
		this.welfareName = welfareRegistration.getWelfareName();
		this.employeeType = welfareRegistration.getEmployeeType();
		this.selectEmployee = welfareRegistration.getSelectEmployee();
		this.createdBy = welfareRegistration.getCreatedBy();
		this.updatedBy = welfareRegistration.getUpdatedBy();
		this.created_date = welfareRegistration.getCreated_date();
		this.created_date = welfareRegistration.getCreated_date();
        if (welfareRegistration.getUpdated_date() != null) {
            this.updated_date = welfareRegistration.getUpdated_date();
        } else {
            this.updated_date = welfareRegistration.getCreated_date();
        }
        this.welfareEmployee = welfareRegistration.getWelfareEmployee() != null
                ? welfareRegistration.getWelfareEmployee().stream().map(field -> new WelfareEmployeeDto(field)).collect(Collectors.toList()) : null;
    }
    }
	

