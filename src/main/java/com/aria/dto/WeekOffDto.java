package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.WeekDay;
import com.aria.model.Weekoff;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class WeekOffDto {

    private Long empId;

    private Long weekOffDay;
    
    private LocalDateTime updated_dt;
    
    private LocalDateTime created_dt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    private Date shiftassigndate;

    private int emp_type;
  

    public WeekOffDto(Weekoff weekOff) {
     this.empId = weekOff.getEmpId();
     this.weekOffDay = weekOff.getWeekOffDay().getId();
     this.updated_dt = weekOff.getUpdated_date();
     this.created_dt = weekOff.getCreated_date();
     this.createdBy = weekOff.getCreated_by();
     this.updatedBy = weekOff.getUpdated_by();
     this.shiftassigndate = weekOff.getShiftassigndate();
     this.emp_type = weekOff.getEmp_type();

     
    }
}
