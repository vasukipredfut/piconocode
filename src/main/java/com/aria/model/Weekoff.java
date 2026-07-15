package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.WeekOffDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "weekoff")
public class Weekoff {

    @Id
    @Column(name="emp_id")
    private Long empId;

    @OneToOne
    @JoinColumn(name = "weekoffday", referencedColumnName = "id")
    private WeekDay weekOffDay;

    @Column
    private Date shiftassigndate;

    @Column(updatable = false)
    private Long created_by;

    @Column
    private Long updated_by;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private int emp_type;


    public Weekoff(WeekOffDto weekOff) {

        this.empId = weekOff.getEmpId();
        this.weekOffDay.setId(weekOff.getWeekOffDay());
        this.shiftassigndate = weekOff.getShiftassigndate();
        this.created_by = weekOff.getCreatedBy();
        this.updated_by = weekOff.getUpdatedBy();
        this.created_date = weekOff.getCreated_dt();
        this.updated_date = weekOff.getUpdated_dt();
        this.emp_type = weekOff.getEmp_type();
    }
}
