package com.aria.dto;

import java.time.LocalDate;

public interface VoucherGrpList {
	Long getId();
    String getGrpNo();
    LocalDate getFmDate();
    LocalDate getToDate();
    String getStatus();
    String getBName();
    String getDName();
    String getCName();
}
