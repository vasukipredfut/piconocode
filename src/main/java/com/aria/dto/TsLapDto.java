package com.aria.dto;

import com.aria.model.TsLap;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class TsLapDto {

    private Long id;

    private String name;

    private String btime;

    private String etime;

    private String colid;
    
    private String sun;

    private String mon;

    private String tue;

    private String wed;

    private String thu;

    private String fri;
    
    private String sat;

    private String hol;

    private int pshift;

    private String pspos;

    private int bday;
    
    private int aday;

    private String edtime;

    private String edtvar;
    
    private String dispcolor;

    private int shour;

    private String additionalTime;

    public TsLapDto(TsLap tsLapDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = tsLapDto.getId();
        this.name = tsLapDto.getName();
        this.btime = dateUtil.getTimeToStringTime(tsLapDto.getBtime());
        this.etime = dateUtil.getTimeToStringTime(tsLapDto.getEtime());
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
        this.edtime = dateUtil.getDateTOStringWk(tsLapDto.getEdtime());;
        this.edtvar = tsLapDto.getEdtvar();
        this.dispcolor = tsLapDto.getDispcolor();
        this.shour = tsLapDto.getShour();
        this.additionalTime = tsLapDto.getAdditionalTime();
    }
}
