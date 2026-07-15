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
public class EBTableDetailsDto {
	
	private LocalDate Readingdate;
	
	private LocalDate todate;
	
	private Long serviceid;
	
	private String servicename;
	
	private Long timmingid;
	
	private String timmingSlap;
	
	private Long month;
	
	private Long year;
	
	private List<EBReadingDetailsDto> readings;

}
