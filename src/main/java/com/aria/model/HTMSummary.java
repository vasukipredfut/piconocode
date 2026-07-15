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
@Table(name = "htmsummary", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HTMSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long servid;
    private Long tsid;
    private Long bid;

    @Column(name = "name", length = 50)
    private String name;

    private Long panelid;
    private Long atid;

    @Column(length = 50)
    private String atname;

    private Double mopening;
    private Double mclosing;
    private Double cunits;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(length = 50)
    private String usecid;

    @Column(length = 1)
    private String entposition;

    private LocalDateTime updatedon;

    @Column(length = 1)
    private String status;

    private Long cby;

    @Column(length = 50)
    private String con;

    @Column(length = 50)
    private String trnno;

    private Long vouid;

    @Column(length = 100)
    private String voutrnno;

    private Long cid;
    private Long resbid;

    @Column(length = 50)
    private String strmonth;

    @Column(length = 15)
    private String dispname;

    private LocalDate ldate;
}
