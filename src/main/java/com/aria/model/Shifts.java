package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.ShiftsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shifts")
public class Shifts {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String shiftName;

    @Column(name = "category")
    private String shiftType;
    
    @Column
    private String hours;
    
    @Column
    private String sun;
    
    @Column
    private String mon;
    
    @Column
    private String tue;
    
    @Column
    private String wed;
    
    @Column
    private String thu;
    
    @Column
    private String fri;
    
    @Column
    private String sat;
    
    @Column
    private String intime;
    
    @Column
    private String outtime;
    
    @Column
    private String lunchtime;
    
    @Column
    private String delaytime;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @Column
    private Integer shiftnumber;

    @Column(name = "delay_Duration")
    private String delayDuration;

    @Column(name = "pshift_id")
    private Long pShiftId;



    public Shifts(ShiftsDto shifts) {
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
