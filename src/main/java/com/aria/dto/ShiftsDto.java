package com.aria.dto;

import com.aria.model.Shifts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftsDto {

    private Long id;
    private String shiftName;
    private String shiftType;
    private String hours;
    private String mon;
    private String tue;
    private String wed;
    private String thu;
    private String fri;
    private String sat;
    private String sun;
    private String delaytime;
    private String intime;
    private String outtime;
    private String lunchtime;
    private String delayDuration;
    private Integer shiftnumber;
    private Long pShiftId;


    public ShiftsDto(Shifts shifts) {
        this.id = shifts.getId();
        this.shiftName = shifts.getShiftName();
        this.shiftType = shifts.getShiftType();
        this.hours = shifts.getHours();
        this.mon = shifts.getMon();
        this.tue = shifts.getTue();
        this.wed = shifts.getWed();
        this.thu = shifts.getThu();
        this.fri = shifts.getFri();
        this.sat = shifts.getSat();
        this.sun = shifts.getSun();
        this.delaytime = shifts.getDelaytime();
        this.intime = shifts.getIntime();
        this.outtime = shifts.getOuttime();
        this.lunchtime = shifts.getLunchtime();
        this.delayDuration = shifts.getDelayDuration();
        this.shiftnumber = shifts.getShiftnumber();
        this.pShiftId = shifts.getPShiftId();
    }
}
