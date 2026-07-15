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
public class WindmillPrintDto {

	private Long windmillId;
    private String windmillName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long workingDays;
    private Long month;
    private Long year;
    private String sourcetype;
    
    List<TimeReadingPrintDto> timing; 
}
