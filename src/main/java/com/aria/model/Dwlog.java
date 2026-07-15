package com.aria.model;

import com.aria.dto.DwlogDto;
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
@Table(name = "dwlog")
public class Dwlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="slno", nullable = true)
    private Long slno;

    @Column(name ="bioid", nullable = false)
    private Long bioId;

    @Column(length = 10)
    private String attstate;

    @Column(name = "attdatetime")
    private Date attDateTime;

    @Column(name = "sysip", length = 20)
    private String sysIp;

    @Column(name = "ustatus")
    private String uStatus ;

    @Column(name = "astatus")
    private String aStatus ;

    @Column(name = "atype")
    private String aType;

    @Column(name = "temp")
    private Double temp;

    @Temporal(TemporalType.DATE)
    @Column
    private Date date;

    public Dwlog(DwlogDto dto) {
        DateUtil dateUtil = new DateUtil();
        this.id = dto.getId();
        this.slno = dto.getSlno();
        this.bioId = dto.getBioId();
        this.attstate = dto.getAttendanceState();
        this.attDateTime = dateUtil.getStringToDateTime(dto.getAttendanceDateTime());
        this.sysIp = dto.getSysIp();
        this.uStatus = dto.getUStatus();
        this.aStatus = dto.getAStatus();
        this.aType = dto.getAType();
        this.temp = dto.getTemp();
    }
}