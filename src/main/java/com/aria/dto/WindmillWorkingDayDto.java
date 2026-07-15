package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WindmillWorkingDayDto {

	private Long id;
	private LocalDate fromdate;
    private LocalDate todate;
    private String plan;
    private String sourcetype;
    private Long month;
    private Long year;
    private LocalDate createddate;
    private Integer numberofworkingday;

    public WindmillWorkingDayDto(WindmillWorkingDayProjection request) {
    	this.id=request.getWindmillid();
    	    this.fromdate = request.getFromdate();
        this.todate = request.getTodate();
        this.plan = request.getPlan();
        this.sourcetype = request.getSourcetype();
        this.month =request.getMonth();
        this.year=request.getYear();
        this.createddate=request.getCreateddate();
        this.numberofworkingday = request.getWorkingdays();
    }

    
}
