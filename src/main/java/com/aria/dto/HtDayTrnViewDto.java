package com.aria.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HtDayTrnViewDto {

    private LocalDate readingdate;
    private String servicename;
    private String timeslap;
    private Integer month;
    private Integer year;
    private String status;
    private LocalDate createddate;
    private Long servid;
    private Long tsid;
    private Long id;
	public HtDayTrnViewDto(HtDayTrnViewProjection request) {
		
		this.readingdate = request.getReadingdate();
		this.servicename = request.getServicename();
		this.timeslap = request.getTimeslap();
		this.month = request.getMonth();
		this.year = request.getYear();
		this.servid = request.getServid();
		this.tsid =request.getTsid() ;
		this.id = request.getId();
	}
    
    

}
