package com.aria.model;

import com.aria.dto.ShiftRotationMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shift_rotation_mapping")
public class ShiftRotationMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String shift;

    @Column(name = "shift_Rotation_Id")
    private Long shiftRotationId;

    @Column(name = "emp_Id")
    private Long empId;

    @Column
    private String designation;

    @Column(name = "employee_Name")
    private String employeeName;

    @Column(name = "week_Off")
    private Long weekOff;

    @Column(name = "week_Off_Name")
    private String weekOffName;

    @Column(name = "current_Week_Off")
    private String currentWeekOff;

    @Column
    private int emp_type;


    public ShiftRotationMapping(ShiftRotationMappingDto rotationMappingDto) {
        this.empId = rotationMappingDto.getEmpId();
        this.designation = rotationMappingDto.getDesignation();
        this.employeeName = rotationMappingDto.getEmployeeName();
        this.weekOff = rotationMappingDto.getWeekOff();
        this.weekOffName = rotationMappingDto.getWeekOffName();
        this.currentWeekOff = rotationMappingDto.getCurrentWeekOff();
        this.emp_type = rotationMappingDto.getEmp_type();
    }
}
