package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDailyLogDto {
	private String fingerid;
	private LocalDate date;
	private LocalDateTime dateandtime;
	private Long loggedid;
	private String fingermechineid;
	private String location;
	public EmployeeDailyLogDto(EmployeeDailyLogList request) {
		super();
		this.fingerid = request.getFingerid();
		this.date = request.getDate();
		this.dateandtime = request.getDateandtime();
		this.loggedid = request.getLoggedid();
		this.fingermechineid = request.getFingermechineid();
		this.location = request.getLocation();
	}
	
	
}
