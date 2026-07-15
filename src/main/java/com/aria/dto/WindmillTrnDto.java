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
public class WindmillTrnDto {

	private Long id;
    private Long windmillId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long attributeId;
    private Long timmingId;
    private Double openingReading;
    private Double closingReading;
    private Double difference;
    private String entPosition;
    private Long updatedBy;
    private Long workingDays;

	
}
