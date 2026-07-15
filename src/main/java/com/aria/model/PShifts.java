package com.aria.model;

import com.aria.dto.PShiftsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pshifts")
public class PShifts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Double hours;

    @DateTimeFormat(pattern = "hh:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "intime")
    private Date inTime;

    @DateTimeFormat(pattern = "hh:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "outtime")
    private Date outTime;

    public PShifts(PShiftsDto pShifts) {
        this.id = pShifts.getId();
        this.name = pShifts.getName();
        this.hours = pShifts.getHours();
        this.inTime = pShifts.getInTime();
        this.outTime = pShifts.getOutTime();
    }
}
