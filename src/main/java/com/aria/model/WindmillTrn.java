package com.aria.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillTrnDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "windmilltrn")
@Getter
@Setter
@NoArgsConstructor
public class WindmillTrn {
   
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "windmillid")
    private Long windmillId;

    @Column(name = "fromdate")
    private LocalDate fromDate;

    @Column(name = "attributeid")
    private Long attributeId;

    @Column(name = "timmingid")
    private Long timmingId;

    @Column(name = "openingreading")
    private Double openingReading;

    @Column(name = "closingreading")
    private Double closingReading;

    @Column(name = "difference")
    private Double difference;

    @Column(name = "entposition", length = 1)
    private String entPosition;

    @Column(name = "todate")
    private LocalDate toDate;
    
    @Column(name="createddate")
    private LocalDate createdDt;

    @Column(name = "updatedby")
    private Long updatedBy;

    @Column(name = "workingdays")
    private Long workingDays;
    
    @Column
    private Long month;
    @Column
    private Long year;

	public WindmillTrn(WindmillTrnDto request) {
		super();
		this.id = request.getId();
		this.windmillId = request.getWindmillId();
		this.fromDate = request.getFromDate();
		this.attributeId = request.getAttributeId();
		this.timmingId = request.getTimmingId();
		this.openingReading = request.getOpeningReading();
		this.closingReading = request.getClosingReading();
		this.difference = request.getDifference();
		this.entPosition = request.getEntPosition();
		this.toDate = request.getToDate();
		this.updatedBy = request.getUpdatedBy();
		this.workingDays = request.getWorkingDays();
	}
 
    
	
}
