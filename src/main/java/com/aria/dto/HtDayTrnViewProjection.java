package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface HtDayTrnViewProjection {

    LocalDate getReadingdate();

    String getServicename();

    String getTimeslap();

    Integer getMonth();

    Integer getYear();

    String getStatus();

    String getCreateddate();

    Long getServid();

    Long getTsid();

    Long getId();
}
