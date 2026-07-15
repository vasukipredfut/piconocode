package com.aria.dto;

import com.aria.model.ShiftsList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftsListDto {

    private Long shiftId;
    private String shiftName;
    private String shiftType;
    private String lunchBreak;
    private String workingHours;


    public ShiftsListDto(ShiftsList shiftsList) {
        this.shiftId = shiftsList.getShiftId();
        this.shiftName = shiftsList.getShiftName();
        this.shiftType = shiftsList.getShiftType();
        this.lunchBreak = shiftsList.getLunchBreak();
        this.workingHours = shiftsList.getWorkingHours();
    }
}
