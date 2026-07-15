package com.aria.dto;

import com.aria.model.IRegisteredUserListScheduleTraining;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisteredUserListScheduleTrainingDto {

	private Long employeeId;

	private String employeename;

	private Long departmentId;

	private String departmentName;

	private Long designationId;

	private String designationName;

	private Long scheduleTrainingId;

	private String registrationDate;

	public RegisteredUserListScheduleTrainingDto(
			IRegisteredUserListScheduleTraining iRegisteredUserListScheduleTraining) {

		this.employeeId = iRegisteredUserListScheduleTraining.getEmployeeId();
		this.employeename = iRegisteredUserListScheduleTraining.getEmployeename();

		this.departmentId = iRegisteredUserListScheduleTraining.getDepartmentId();
		this.departmentName = iRegisteredUserListScheduleTraining.getDepartmentName();
		this.designationId = iRegisteredUserListScheduleTraining.getDesignationId();
		this.designationName = iRegisteredUserListScheduleTraining.getDesignationName();
		this.scheduleTrainingId = iRegisteredUserListScheduleTraining.getScheduleTrainingId();
//		this.registrationDate = iRegisteredUserListScheduleTraining.getRegistrationDate();
	}
}
