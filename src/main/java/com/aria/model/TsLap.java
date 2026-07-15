package com.aria.model;

import com.aria.dto.TsLapDto;
import com.aria.utils.DateUtil;
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
@Table(name = "tslap")
public class TsLap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @DateTimeFormat(pattern = "hh:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "btime")
    private Date btime;

    @DateTimeFormat(pattern = "hh:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "etime")
    private Date etime;

    @Column
    private String colid;

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
    private String hol;

    @Column
    private int pshift;

    @Column
    private String pspos;

    @Column
    private int bday;

    @Column
    private int aday;

    @Column
    private Date edtime;

    @Column
    private String edtvar;

    @Column
    private String dispcolor;

    @Column
    private int shour;

    @Column(name = "additional_Time")
    private String additionalTime;

    public TsLap(TsLapDto tsLapDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = tsLapDto.getId();
        this.name = tsLapDto.getName();
        this.btime = dateUtil.getStringTimeTOTime(tsLapDto.getBtime());
        this.etime = dateUtil.getStringTimeTOTime(tsLapDto.getEtime());
        this.colid = tsLapDto.getColid();
        this.sun = tsLapDto.getSun();
        this.mon = tsLapDto.getMon();
        this.tue = tsLapDto.getTue();
        this.wed = tsLapDto.getWed();
        this.thu = tsLapDto.getThu();
        this.fri = tsLapDto.getFri();
        this.sat = tsLapDto.getSat();
        this.hol = tsLapDto.getHol();
        this.pshift = tsLapDto.getPshift();
        this.pspos = tsLapDto.getPspos();
        this.bday = tsLapDto.getBday();
        this.aday = tsLapDto.getAday();
        this.edtime = dateUtil.getStringTODateWk(tsLapDto.getEdtime());;
        this.edtvar = tsLapDto.getEdtvar();
        this.dispcolor = tsLapDto.getDispcolor();
        this.shour = tsLapDto.getShour();
        this.additionalTime = tsLapDto.getAdditionalTime();
    }
}
