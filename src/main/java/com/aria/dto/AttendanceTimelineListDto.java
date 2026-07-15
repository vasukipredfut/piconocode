package com.aria.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceTimelineListDto {
	
	private LocalDate createddate;
    private String location;
    private String ipaddress;
    private String intime;
    private String outtime;
    private String totalworkingdays;
    private String employeename;
    private String reason;
    private String shiftname;
	public AttendanceTimelineListDto(AttendanceTimelineList request) {
		super();
		this.createddate = request.getCreateddate();
		this.location = request.getLocation();
		this.ipaddress=request.getIpaddress();
		this.intime = request.getIntime();
		this.outtime = request.getOuttime();
		this.totalworkingdays=request.getTotalworkingdays();
		this.employeename = request.getEmployeename();
		this.reason = request.getReason();
		this.shiftname=request.getShiftname();
	}
    
    

}
