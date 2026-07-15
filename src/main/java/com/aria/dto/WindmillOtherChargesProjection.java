package com.aria.dto;

import java.time.LocalDate;

public interface WindmillOtherChargesProjection {

    Long getWindmillid();

    Double getAmount();
    
    String getName();

    Long getOtherchargesid();

    LocalDate getTrndate();
}
