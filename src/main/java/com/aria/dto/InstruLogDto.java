package com.aria.dto;

import com.aria.model.InstruLog;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class InstruLogDto {



    private Long id;

    private Long bioId;

    private String attendanceState;

    private String attendanceTime;

    private String sysIp;

    private String uStatus;

    private String aStatus;

    private String aType;

    public InstruLogDto(InstruLog instruLog) {
        DateUtil dateUtil = new DateUtil();
        this.id = instruLog.getId();
        this.bioId = instruLog.getBioId();
        this.attendanceState = instruLog.getAttendanceState();
        this.attendanceTime = dateUtil.getDateToStringDateTime(instruLog.getAttendanceTime());
        this.sysIp = instruLog.getSysIp();
        this.uStatus = instruLog.getUStatus();
        this.aStatus = instruLog.getAStatus();
        this.aType = instruLog.getAType();
    }
}
