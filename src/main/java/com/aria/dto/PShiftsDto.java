package com.aria.dto;

import com.aria.model.PShifts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PShiftsDto {

    private Long id;

    private String name;

    private Double hours;

    private Date inTime;

    private Date outTime;


    public PShiftsDto(PShifts pShifts) {
        this.id = pShifts.getId();
        this.name = pShifts.getName();
        this.hours = pShifts.getHours();
        this.inTime = pShifts.getInTime();
        this.outTime = pShifts.getOutTime();
    }
}
