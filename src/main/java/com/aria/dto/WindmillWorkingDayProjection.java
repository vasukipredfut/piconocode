package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface WindmillWorkingDayProjection {
	
	Long getWindmillid();
	
	LocalDate getFromdate();

    LocalDate getTodate();

    String getPlan();

    String getSourcetype();
    
    Long getMonth();
    
    Long getYear();
    
    LocalDate getCreateddate();

    Integer getWorkingdays();
}
