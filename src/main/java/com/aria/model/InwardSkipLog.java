package com.aria.model;

import com.aria.dto.PatlogDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inward_skip_log")
public class InwardSkipLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String batchno;

    @Column
    private Long batchid;

    @Column
    private Long tgid;

    @Column
    private String tno;

    @Column
    @Temporal(TemporalType.DATE)
    private Date entry;

    @Column
    private Long kpi;

    @Column
    private String skipflag;

    public InwardSkipLog(PatlogDto inwardSkipLog) {
        DateUtil dateUtil = new DateUtil();
        this.id = inwardSkipLog.getId();
        this.batchno = inwardSkipLog.getBatchno();
        this.batchid = inwardSkipLog.getBatchid();
        this.tgid = inwardSkipLog.getTgid();
        this.tno = inwardSkipLog.getTno();
        this.kpi = inwardSkipLog.getKpi();
        this.skipflag = inwardSkipLog.getSkipflag();
    }
}
