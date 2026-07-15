package com.aria.dto;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceTimelineResDto {
	private Long id;
	private LocalDateTime shiftdate;
	private LocalDateTime intime;
	private LocalDateTime outtime;
	private String totalWorkHours;
	private String reason;
	private Long bioid;
	private String employeename;
	private String fingerprintstation;
	private String shift;
	public AttendanceTimelineResDto(AttendanceTimelineProjection request) {
		super();
		this.id = request.getId();
		this.shiftdate = request.getShiftdate();
		this.intime = request.getIntime();
		this.outtime = request.getOuttime();
		this.totalWorkHours = request.getTotalWorkHours();
		this.reason = request.getReason();
		this.bioid = request.getBioid();
		this.employeename = request.getEmployeename();
		this.fingerprintstation = request.getFingerprintstation();
		this.shift = request.getShift();
	}
	
	

}
