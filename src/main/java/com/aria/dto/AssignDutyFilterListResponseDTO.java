package com.aria.dto;

import com.aria.model.AssignDuty;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class AssignDutyFilterListResponseDTO {

	private Long allotmentId;
	private String employeeName;
	private Long employeeId;
	private String assignedShift;
	private String assignedDate;
	private String assignedDuty;
	private String currentDuty;
	private String status;


	public AssignDutyFilterListResponseDTO(IAssignDutyFilterListResponse iAssignDutyFilterListResponse) {
		DateUtil dateUtil = new DateUtil();
		this.employeeName = iAssignDutyFilterListResponse.getEmployeeName();
		this.employeeId = iAssignDutyFilterListResponse.getEmployeeId();
		this.assignedShift = iAssignDutyFilterListResponse.getAssignedShift();
		this.assignedDate = dateUtil.getDateTOStringWk(iAssignDutyFilterListResponse.getAssignedDate());
		this.assignedDuty = iAssignDutyFilterListResponse.getAssignedDuty();
		this.allotmentId = iAssignDutyFilterListResponse.getAllotmentId();
		this.currentDuty = iAssignDutyFilterListResponse.getCurrentDuty();
		if(iAssignDutyFilterListResponse.getStatus() != null) {
			this.status = iAssignDutyFilterListResponse.getStatus();
		}else {
			this.status = "N";
		}
	}
}
