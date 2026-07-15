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
public class WindmillTrnViewDto {

	private Long timmingid;
    private String timmingname;

    private Long attributeid;
    private String attributename;

    private Double opening;
    private Double closing;
    private Double difference;

    private Long windmillid;

    private String plan;
    private String sourcetype;

    private LocalDate fromdate;
    private LocalDate todate;

    private Long workingdays;

    private LocalDate createddate;
    private String updatedby;

	public WindmillTrnViewDto(WindmillTrnProjection request) {
		this.fromdate = request.getFromdate();
		this.attributeid = request.getAttributeid();
		this.attributename = request.getAttributename();
		this.timmingid = request.getTimmingid();
		this.timmingname = request.getTimmingname();
		this.opening = request.getOpening();
		this.closing = request.getClosing();
		this.difference = request.getDifference();
		this.todate = request.getTodate();
		this.updatedby = request.getUpdatedby();
		this.workingdays = request.getWorkingdays();
		this.windmillid = request.getWindmillid();
		this.plan = request.getPlan();
		this.sourcetype=request.getSourcetype();
		this.createddate = request.getCreateddate();
	}

    
}
