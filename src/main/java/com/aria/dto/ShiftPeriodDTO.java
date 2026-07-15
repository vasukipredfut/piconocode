package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftPeriodDTO {

    String shiftDate;
    Long empId;
    int empType;
    Long shiftId;
}
