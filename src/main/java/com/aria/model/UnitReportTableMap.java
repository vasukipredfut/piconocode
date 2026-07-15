package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ureptplate")
public class UnitReportTableMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    @Column
    private String ygroup;

    @Column
    private String name;

    @Column
    private Long sjid;


    @Column
    private String syield;

    @Column
    private Long pbyeid;

    @Column
    private Long proid;

    @Column
    private Long proid2;

    @Column
    private Long sbyeid;

    @Column
    private Long sprocess;

    @Column
    private Long delitCleaningProcess;

    @Column
    private Long delitProcess;

    @Column
    private double bprodlot;

    @Column
    private String exop;
    @Column
    private Long packsjid;

}
