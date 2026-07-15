package com.aria.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeOverViewDto {
	
	private LocalDate attendancedate;
    private String intime;
    private String outime;
    List<ReasonDto> reason;
	public EmployeeOverViewDto(EmployeeOverViewList request) {
		this.attendancedate = request.getAttendancedate();
		this.intime = request.getIntime();
		
	}
    
    

}
