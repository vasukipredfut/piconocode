package com.aria.model;

import com.aria.dto.InstruLogDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "instrulog")
public class InstruLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slno")
    private Long id;

    @Column(name = "enrollnumber")
    private Long bioId;

    @Column(name = "attstate")
    private String attendanceState;

    @Column(name = "attdatetime")
    private Date attendanceTime;

    @Column(name = "sysip")
    private String sysIp;

    @Column(name = "ustatus")
    private String uStatus;

    @Column(name = "astatus")
    private String aStatus;

    @Column(name = "atype")
    private String aType;

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "created_date")
    private LocalDateTime created_date;


    public InstruLog(InstruLogDto instruLog) {
        DateUtil dateUtil = new DateUtil();
        this.id = instruLog.getId();
        this.bioId = instruLog.getBioId();
        this.attendanceState = instruLog.getAttendanceState();
        this.attendanceTime = dateUtil.getStringToDateTime(instruLog.getAttendanceTime());
        this.sysIp = instruLog.getSysIp();
        this.uStatus = instruLog.getUStatus();
        this.aStatus = instruLog.getAStatus();
        this.aType = instruLog.getAType();
    }
}
