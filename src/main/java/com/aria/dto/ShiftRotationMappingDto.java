package com.aria.dto;

import com.aria.model.ShiftRotationMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftRotationMappingDto {

    private Long empId;
    private String designation;
    private String employeeName;
    private Long weekOff;
    private String weekOffName;
    private String currentWeekOff;
    private int emp_type;


    public ShiftRotationMappingDto(ShiftRotationMappingList shiftRotationMapping) {
        this.empId = shiftRotationMapping.getEmpId();
        this.designation = shiftRotationMapping.getDesignation();
        this.employeeName = shiftRotationMapping.getEmployeeName();
        this.weekOff = shiftRotationMapping.getWeekOff();
        this.weekOffName = shiftRotationMapping.getWeekOffName();
        this.currentWeekOff = shiftRotationMapping.getCurrentWeekOff();
    }


    public ShiftRotationMappingDto(ShiftRotationMapping shiftRotationMapping) {
        this.empId = shiftRotationMapping.getEmpId();
        this.designation = shiftRotationMapping.getDesignation();
        this.employeeName = shiftRotationMapping.getEmployeeName();
        this.weekOff = shiftRotationMapping.getWeekOff();
        this.weekOffName = shiftRotationMapping.getWeekOffName();
        this.currentWeekOff = shiftRotationMapping.getCurrentWeekOff();
    }


}
