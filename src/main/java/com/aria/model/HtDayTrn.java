package com.aria.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "htdaytrn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HtDayTrn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shiftdate")
    private LocalDate shiftdate;

    @Column(name = "servid")
    private Long servid;

    @Column(name = "tsid")
    private Long tsid;

    @Column(name = "bid")
    private Long bid;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "panelid")
    private Long panelid;

    @Column(name = "atid")
    private Long atid;

    @Column(name = "atname", length = 50)
    private String atname;

    @Column(name = "mopening")
    private Double mopening;

    @Column(name = "mclosing")
    private Double mclosing;

    @Column(name = "cunits")
    private Double cunits;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "usecid", length = 50)
    private String usecid;

    @Column(name = "entposition", length = 1)
    private String entposition;

    @Column(name = "updatedon")
    private LocalDateTime updatedon;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "cby")
    private Long cby;

    @Column(name = "con", length = 50)
    private String con;

    @Column(name = "trnno", length = 50)
    private String trnno;

    @Column(name = "vouid")
    private Long vouid;

    @Column(name = "voutrnno", length = 100)
    private String voutrnno;

    @Column(name = "cid")
    private Long cid;

    @Column(name = "resbid")
    private Long resbid;

    @Column(name = "strday", length = 50)
    private String strday;

    @Column(name = "upatstatus", length = 1)
    private String upatstatus;

    @Column(name = "eord")
    private Long eord;
}
