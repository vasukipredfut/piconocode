package com.aria.dto;

import com.aria.model.Dwlog;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class DwlogDto {
    private Long id;
    private Long slno;
    private Long bioId;
    private String attendanceState;
    private String attendanceDateTime;
    private String sysIp;
    private String uStatus;
    private String aStatus;
    private String aType;
    private Double temp;

    public DwlogDto(Dwlog dwlog) {
        DateUtil dateUtil = new DateUtil();
        this.id = dwlog.getId();
        this.bioId = dwlog.getBioId();
        this.attendanceState = dwlog.getAttstate();
        this.attendanceDateTime = dateUtil.getDateToStringDateTime(dwlog.getAttDateTime());
        this.sysIp = dwlog.getSysIp();
        this.uStatus = dwlog.getUStatus();
        this.aStatus = dwlog.getAStatus();
        this.aType = dwlog.getAType();
        this.temp = dwlog.getTemp();
    }
}