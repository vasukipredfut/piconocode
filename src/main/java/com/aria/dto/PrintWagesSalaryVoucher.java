package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

public interface PrintWagesSalaryVoucher {

    Long getId();

    Date getShiftDate();

    String getTrnNo();

    Long getBranch();

    String getBranchName();

    Long getDept();

    String getDeptName();

    Long getPshiftId();

    String getPshiftName();

    Date getTrnDate();

    Double getTrnQty();

    Double getAmount();

    String getTransactionTable();
    String getCreatedByName();
    Long getCreatedBy();
    LocalDateTime getCreatedDate();
}
