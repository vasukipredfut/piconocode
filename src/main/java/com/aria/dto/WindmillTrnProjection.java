package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface WindmillTrnProjection {

    Long getTimmingid();
    String getTimmingname();

    Long getAttributeid();
    String getAttributename();

    Double getOpening();
    Double getClosing();
    Double getDifference();

    Long getWindmillid();

    String getPlan();
    String getSourcetype();

    LocalDate getFromdate();
    LocalDate getTodate();
    Long getMonth();
    Long getYear();
    Long getWorkingdays();

    LocalDate getCreateddate();
    String getUpdatedby();
}

