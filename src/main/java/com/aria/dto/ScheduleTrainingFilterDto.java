package com.aria.dto;

import com.aria.model.IScheduleTrainingFilter;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleTrainingFilterDto {

	private Long id;
	private String trainingType;
	private Long departmentId;
	private String departmentName;
	private Long designationId;
	private String designationName;
	private Long skillId;
	private String skillName;
	private Integer totalSeats;
	private Integer totalRegisteredEmployees;
	private String status;
	private String trainingDate;
	private String lastRegistrationDate;

	public ScheduleTrainingFilterDto(IScheduleTrainingFilter iScheduleTrainingFilter) {
		DateUtil dateUtil = new DateUtil();
		this.id = iScheduleTrainingFilter.getId();
		this.trainingType = iScheduleTrainingFilter.getTrainingTypeName();
		this.departmentId = iScheduleTrainingFilter.getDepartmentId();
		this.departmentName = iScheduleTrainingFilter.getDepartmentName();
		this.designationId = iScheduleTrainingFilter.getDesignationId();
		this.designationName = iScheduleTrainingFilter.getDesignationName();
		this.skillId = iScheduleTrainingFilter.getSkillId();
		this.skillName = iScheduleTrainingFilter.getSkillName();
		this.totalSeats = iScheduleTrainingFilter.getTotalSeats();
		this.totalRegisteredEmployees = iScheduleTrainingFilter.getTotalRegisteredEmployees();
		this.status = iScheduleTrainingFilter.getStatus();
		if(iScheduleTrainingFilter.getTrainingDate() != null){
			this.trainingDate = dateUtil.getDateTOStringWk(iScheduleTrainingFilter.getTrainingDate());
		}
		if(iScheduleTrainingFilter.getLastRegistrationDate() != null){
			this.lastRegistrationDate = dateUtil.getDateTOStringWk(iScheduleTrainingFilter.getLastRegistrationDate());
		}
	}

}
